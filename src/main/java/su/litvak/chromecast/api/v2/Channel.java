/*
 * Copyright 2014 Vitaly Litvak (vitavaque@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package su.litvak.chromecast.api.v2;

import com.google.protobuf.InvalidProtocolBufferException;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Internal class for low-level communication with ChromeCast device. Should
 * never be used directly, use {@link su.litvak.chromecast.api.v2.ChromeCast}
 * methods instead
 */
class Channel implements Closeable {
	private static final Logger LOG = LoggerFactory.getLogger(Channel.class);
	/**
	 * Period for sending ping requests (in ms)
	 */
	private static final long PING_PERIOD = 30 * 1000;
	/**
	 * How much time to wait until request is processed
	 */
	private static final long REQUEST_TIMEOUT = 30 * 1000;

	private final static String DEFAULT_RECEIVER_ID = "receiver-0";

	/**
	 * Single socket instance for transfers
	 */
	private final Socket socket;
	/**
	 * Name of sender used in this channel
	 */
	private final String name;
	/**
	 * Timer for PING requests
	 */
	private Timer pingTimer;
	/**
	 * Thread for processing incoming requests
	 */
	private ReadThread reader;
	/**
	 * Counter for producing request numbers
	 */
	private AtomicLong requestCounter = new AtomicLong(0);
	/**
	 * Processors of requests by their identifiers
	 */
	private Map<Long, ResultProcessor> requests = new ConcurrentHashMap<Long, ResultProcessor>();
	/**
	 * Single mapper object for marshalling JSON
	 */
	private final ObjectMapper jsonMapper = new ObjectMapper();
	/**
	 * Destination ids of sessions opened within this channel
	 */
	private Set<String> sessions = new HashSet<String>();

	private class PingThread extends TimerTask {
		@Override
		public void run() {
			try {
				write("urn:x-cast:com.google.cast.tp.heartbeat",
						Message.ping(), DEFAULT_RECEIVER_ID);
			} catch (IOException ioex) {
				LOG.warn("Error while sending 'PING': {}",
						ioex.getLocalizedMessage());
			}
		}
	}

	private class ReadThread extends Thread {
		volatile boolean stop;

		@Override
		public void run() {
			while (!stop) {
				try {
					CastChannel.CastMessage message = read();
					if (message.getPayloadType() == CastChannel.CastMessage.PayloadType.STRING) {
						LOG.debug(" <-- {}", message.getPayloadUtf8());
						final String jsonMSG = message.getPayloadUtf8()
								.replaceFirst("\"type\"", "\"responseType\"");
						Response parsed = jsonMapper.readValue(jsonMSG,
								Response.class);
						if (parsed.requestId != null) {
							ResultProcessor rp = requests
									.remove(parsed.requestId);
							if (rp != null) {
								rp.put(parsed);
							} else {
								LOG.warn(
										"Unable to process request ID = {}, data: {}",
										parsed.requestId, jsonMSG);
							}
						} else if (parsed instanceof Response.Ping) {
							write("urn:x-cast:com.google.cast.tp.heartbeat",
									Message.pong(), DEFAULT_RECEIVER_ID);
						}
					} else {
						LOG.warn("Received unexpected {} message",
								message.getPayloadType());
					}
				} catch (InvalidProtocolBufferException ipbe) {
					LOG.debug("Error while processing protobuf: {}",
							ipbe.getLocalizedMessage());
				} catch (IOException ioex) {
					LOG.warn("Error while reading: {}",
							ioex.getLocalizedMessage());
					System.out.println(ioex.getClass() + " :: "
							+ ioex.getLocalizedMessage());
				}
			}
		}
	}

	private class ResultProcessor<T extends Response> {
		T result;

		public void put(Response result) {
			synchronized (this) {
				this.result = (T) result;
				this.notify();
			}
		}

		public T get() {
			synchronized (this) {
				if (result != null) {
					return result;
				}
				try {
					this.wait(REQUEST_TIMEOUT);
				} catch (InterruptedException ie) {
					ie.printStackTrace();
				}
				return result;
			}
		}
	}

	Channel(String host) throws IOException, GeneralSecurityException {
		this(host, 8009);
	}

	Channel(String host, int port) throws IOException, GeneralSecurityException {
		SSLContext sc = SSLContext.getInstance("SSL");
		sc.init(null, new TrustManager[] { new X509TrustAllManager() },
				new SecureRandom());
		this.socket = sc.getSocketFactory().createSocket(host, port);
		this.name = "sender-" + new RandomString(10).nextString();
		connect();
	}

	/**
	 * Establish connection to the ChromeCast device
	 */
	private void connect() throws IOException {
		/**
		 * Authenticate
		 */
		CastChannel.DeviceAuthMessage authMessage = CastChannel.DeviceAuthMessage
				.newBuilder()
				.setChallenge(CastChannel.AuthChallenge.newBuilder().build())
				.build();

		CastChannel.CastMessage msg = CastChannel.CastMessage
				.newBuilder()
				.setDestinationId(DEFAULT_RECEIVER_ID)
				.setNamespace("urn:x-cast:com.google.cast.tp.deviceauth")
				.setPayloadType(CastChannel.CastMessage.PayloadType.BINARY)
				.setProtocolVersion(
						CastChannel.CastMessage.ProtocolVersion.CASTV2_1_0)
				.setSourceId(name).setPayloadBinary(authMessage.toByteString())
				.build();

		write(msg);
		CastChannel.CastMessage response = read();
		CastChannel.DeviceAuthMessage authResponse = CastChannel.DeviceAuthMessage
				.parseFrom(response.getPayloadBinary());
		if (authResponse.hasError()) {
			throw new IOException("Authentication failed: "
					+ authResponse.getError().getErrorType().toString());
		}

		/**
		 * Send 'PING' message
		 */
		PingThread pingThread = new PingThread();
		pingThread.run();

		/**
		 * Send 'CONNECT' message to start session
		 */
		write("urn:x-cast:com.google.cast.tp.connection", Message.connect(),
				DEFAULT_RECEIVER_ID);

		/**
		 * Start ping/pong and reader thread
		 */
		pingTimer = new Timer(name + " PING");
		pingTimer.schedule(pingThread, 1000, PING_PERIOD);

		reader = new ReadThread();
		reader.start();
	}

	private <T extends Response> T send(String namespace, Request message,
			String destinationId) throws IOException {
		message.requestId = requestCounter.getAndIncrement();
		ResultProcessor<T> rp = new ResultProcessor<T>();
		requests.put(message.requestId, rp);
		write(namespace, message, destinationId);
		try {
			T response = rp.get();
			if (response instanceof Response.Invalid) {
				Response.Invalid invalid = (Response.Invalid) response;
				throw new IOException("Invalid request: " + invalid.reason);
			} else if (response instanceof Response.LoadFailed) {
				throw new IOException("Unable to load media");
			} else if (response instanceof Response.LaunchError) {
				Response.LaunchError launchError = (Response.LaunchError) response;
				throw new IOException("Application launch error: "
						+ launchError.reason);
			}
			return response;
		} finally {
			requests.remove(message.requestId);
		}
	}

	private void write(String namespace, Message message, String destinationId)
			throws IOException {
		write(namespace, jsonMapper.writeValueAsString(message), destinationId);
	}

	private void write(String namespace, String message, String destinationId)
			throws IOException {
		LOG.debug(" --> {}", message);
		CastChannel.CastMessage msg = CastChannel.CastMessage
				.newBuilder()
				.setProtocolVersion(
						CastChannel.CastMessage.ProtocolVersion.CASTV2_1_0)
				.setSourceId(name).setDestinationId(destinationId)
				.setNamespace(namespace)
				.setPayloadType(CastChannel.CastMessage.PayloadType.STRING)
				.setPayloadUtf8(message).build();
		write(msg);
	}

	private void write(CastChannel.CastMessage message) throws IOException {
		socket.getOutputStream().write(toArray(message.getSerializedSize()));
		message.writeTo(socket.getOutputStream());
	}

	private CastChannel.CastMessage read() throws IOException {
		InputStream is = socket.getInputStream();
		byte[] buf = new byte[4];

		int read = 0;
		while (read < buf.length) {
			buf[read++] = (byte) is.read();
		}

		int size = fromArray(buf);
		buf = new byte[size];
		read = 0;
		while (read < size) {
			int nowRead = is.read(buf, read, buf.length - read);
			read += nowRead;
		}

		return CastChannel.CastMessage.parseFrom(buf);
	}

	public Status getStatus() throws IOException {
		Response.Status status = send("urn:x-cast:com.google.cast.receiver",
				Request.status(), "receiver-0");
		return status == null ? null : status.status;
	}

	public boolean isAppAvailable(String appId) throws IOException {
		Response.AppAvailability availability = send(
				"urn:x-cast:com.google.cast.receiver",
				Request.appAvailability(appId), "receiver-0");
		return availability != null
				&& "APP_AVAILABLE".equals(availability.availability.get(appId));
	}

	public Status launch(String appId) throws IOException {
		Response.Status status = send("urn:x-cast:com.google.cast.receiver",
				Request.launch(appId), DEFAULT_RECEIVER_ID);
		return status == null ? null : status.status;
	}

	public Status stop(String sessionId) throws IOException {
		Response.Status status = send("urn:x-cast:com.google.cast.receiver",
				Request.stop(sessionId), DEFAULT_RECEIVER_ID);
		return status == null ? null : status.status;
	}

	private void startSession(String destinationId) throws IOException {
		if (!sessions.contains(destinationId)) {
			write("urn:x-cast:com.google.cast.tp.connection",
					Message.connect(), destinationId);
			sessions.add(destinationId);
		}
	}

	public MediaStatus load(String destinationId, String sessionId,
			Media media, boolean autoplay, double currentTime,
			Map<String, String> customData) throws IOException {
		startSession(destinationId);
		Response.MediaStatus status = send("urn:x-cast:com.google.cast.media",
				Request.load(sessionId, media, autoplay, currentTime,
						customData), destinationId);
		return status == null || status.statuses.length == 0 ? null
				: status.statuses[0];
	}

	public MediaStatus play(String destinationId, String sessionId,
			long mediaSessionId) throws IOException {
		startSession(destinationId);
		Response.MediaStatus status = send("urn:x-cast:com.google.cast.media",
				Request.play(sessionId, mediaSessionId), destinationId);
		return status == null || status.statuses.length == 0 ? null
				: status.statuses[0];
	}

	public MediaStatus pause(String destinationId, String sessionId,
			long mediaSessionId) throws IOException {
		startSession(destinationId);
		Response.MediaStatus status = send("urn:x-cast:com.google.cast.media",
				Request.pause(sessionId, mediaSessionId), destinationId);
		return status == null || status.statuses.length == 0 ? null
				: status.statuses[0];
	}

	public MediaStatus seek(String destinationId, String sessionId,
			long mediaSessionId, double currentTime) throws IOException {
		startSession(destinationId);
		Response.MediaStatus status = send("urn:x-cast:com.google.cast.media",
				Request.seek(sessionId, mediaSessionId, currentTime),
				destinationId);
		return status == null || status.statuses.length == 0 ? null
				: status.statuses[0];
	}

	public Status setVolume(Volume volume) throws IOException {
		Response.Status status = send("urn:x-cast:com.google.cast.receiver",
				Request.setVolume(volume), DEFAULT_RECEIVER_ID);
		return status == null ? null : status.status;
	}

	public MediaStatus getMediaStatus(String destinationId) throws IOException {
		startSession(destinationId);
		Response.MediaStatus status = send("urn:x-cast:com.google.cast.media",
				Request.status(), destinationId);
		return status == null || status.statuses.length == 0 ? null
				: status.statuses[0];
	}

	public boolean isConnected() {
		// TODO: Verify if this check is sufficient
		return (socket != null && socket.isConnected() && !socket.isClosed());
	}

	@Override
	public void close() throws IOException {
		if (pingTimer != null) {
			pingTimer.cancel();
		}
		if (reader != null) {
			reader.stop = true;
		}
		if (socket != null) {
			socket.close();
		}
	}

	/**
	 * Converts specified byte array in Big Endian to int
	 */
	private static int fromArray(byte[] payload) {
		ByteBuffer buffer = ByteBuffer.wrap(payload);
		buffer.order(ByteOrder.BIG_ENDIAN);
		return buffer.getInt();
	}

	/**
	 * Converts specified int to byte array in Big Endian
	 */
	private static byte[] toArray(int value) {
		ByteBuffer buffer = ByteBuffer.allocate(4);
		buffer.order(ByteOrder.BIG_ENDIAN);
		buffer.putInt(value);
		buffer.flip();
		return buffer.array();
	}
}
