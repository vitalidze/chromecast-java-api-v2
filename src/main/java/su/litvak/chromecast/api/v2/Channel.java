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

import static su.litvak.chromecast.api.v2.Util.fromArray;
import static su.litvak.chromecast.api.v2.Util.toArray;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.InvalidProtocolBufferException;

/**
 * Internal class for low-level communication with ChromeCast device.
 * Should never be used directly, use {@link su.litvak.chromecast.api.v2.ChromeCast} methods instead
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
    private Socket socket;
    /**
     * Address of ChromeCast
     */
    private final InetSocketAddress address;
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
    private AtomicLong requestCounter = new AtomicLong(1);
    /**
     * Processors of requests by their identifiers
     */
    private Map<Long, ResultProcessor<?>> requests = new ConcurrentHashMap<Long, ResultProcessor<?>>();
    /**
     * Single mapper object for marshalling JSON
     */
    private final ObjectMapper jsonMapper = new ObjectMapper();
    /**
     * Destination ids of sessions opened within this channel
     */
    private Set<String> sessions = new HashSet<String>();
    /**
     * Indicates that this channel was closed (explicitly, by remote host or for some connectivity issue)
     */
    private volatile boolean closed;

    private class PingThread extends TimerTask {
        @Override
        public void run() {
            try {
                write("urn:x-cast:com.google.cast.tp.heartbeat", Message.ping(), DEFAULT_RECEIVER_ID);
            } catch (IOException ioex) {
                LOG.warn("Error while sending 'PING': {}", ioex.getLocalizedMessage());
            }
        }
    }

    private class ReadThread extends Thread {
        volatile boolean stop;

        @Override
        public void run() {
            while (!stop) {
                CastChannel.CastMessage message = null;
                try {
                    message = read();
                    if (message.getPayloadType() == CastChannel.CastMessage.PayloadType.STRING) {
                        LOG.debug(" <-- {}",  message.getPayloadUtf8());
                        final String jsonMSG = message.getPayloadUtf8().replaceFirst("\"type\"", "\"responseType\"");
                        if (!jsonMSG.contains("responseType")) {
                            LOG.warn(" <-- {Skipping}", jsonMSG);
                            continue;
                        }

                        Response parsed = jsonMapper.readValue(jsonMSG, Response.class);
                        if (parsed.requestId != null) {
                            ResultProcessor<?> rp = requests.remove(parsed.requestId);
                            if (rp != null) {
                                rp.put(parsed);
                            } else {
                                if (parsed.requestId != 0) {
                                    // Status events are sent with a requestid of zero
                                    // https://developers.google.com/cast/docs/reference/messages
                                    LOG.warn("Unable to process request ID = {}, data: {}", parsed.requestId, jsonMSG);
                                }
                            }
                        } else if (parsed instanceof Response.Ping) {
                            write("urn:x-cast:com.google.cast.tp.heartbeat", Message.pong(), DEFAULT_RECEIVER_ID);
                        }
                    } else {
                        LOG.warn("Received unexpected {} message", message.getPayloadType());
                    }
                } catch (InvalidProtocolBufferException ipbe) {
                    LOG.debug("Error while processing protobuf: {}", ipbe.getLocalizedMessage());
                } catch (IOException ioex) {
                    LOG.warn("Error while reading: {}", ioex.getLocalizedMessage());
                    String temp;
                    if (message != null &&  message.getPayloadUtf8() != null) {
                        temp = message.getPayloadUtf8();
                    } else {
                        temp = " null payload in message ";
                    }
                    LOG.warn(" <-- {}", temp);
                    try {
                        close();
                    } catch (IOException e) {
                        LOG.warn("Error while closing channel: {}", ioex.getLocalizedMessage());
                    }
                }
            }
        }
    }

    private class ResultProcessor<T extends Response> {
        T result;

        @SuppressWarnings("unchecked")
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
        this.address = new InetSocketAddress(host, port);
        this.name = "sender-" + new RandomString(10).nextString();
        connect();
    }

    /**
     * Establish connection to the ChromeCast device
     */
    private void connect() throws IOException, GeneralSecurityException {
        if (socket == null || socket.isClosed()) {
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, new TrustManager[] { new X509TrustAllManager() }, new SecureRandom());
            socket = sc.getSocketFactory().createSocket();
            socket.connect(address);
        }
        /**
         * Authenticate
         */
        CastChannel.DeviceAuthMessage authMessage = CastChannel.DeviceAuthMessage.newBuilder()
                .setChallenge(CastChannel.AuthChallenge.newBuilder().build())
                .build();

        CastChannel.CastMessage msg = CastChannel.CastMessage.newBuilder()
                .setDestinationId(DEFAULT_RECEIVER_ID)
                .setNamespace("urn:x-cast:com.google.cast.tp.deviceauth")
                .setPayloadType(CastChannel.CastMessage.PayloadType.BINARY)
                .setProtocolVersion(CastChannel.CastMessage.ProtocolVersion.CASTV2_1_0)
                .setSourceId(name)
                .setPayloadBinary(authMessage.toByteString())
                .build();

        write(msg);
        CastChannel.CastMessage response = read();
        CastChannel.DeviceAuthMessage authResponse = CastChannel.DeviceAuthMessage.parseFrom(response.getPayloadBinary());
        if (authResponse.hasError()) {
            throw new ChromeCastException("Authentication failed: " + authResponse.getError().getErrorType().toString());
        }

        /**
         * Send 'PING' message
         */
        PingThread pingThread = new PingThread();
        pingThread.run();

        /**
         * Send 'CONNECT' message to start session
         */
        write("urn:x-cast:com.google.cast.tp.connection", Message.connect(), DEFAULT_RECEIVER_ID);

        /**
         * Start ping/pong and reader thread
         */
        pingTimer = new Timer(name + " PING");
        pingTimer.schedule(pingThread, 1000, PING_PERIOD);

        reader = new ReadThread();
        reader.start();

        closed = false;
    }

    private <T extends Response> T send(String namespace, Request message, String destinationId) throws IOException {
        /**
         * Try to reconnect
         */
        if (isClosed()) {
            try {
                connect();
            } catch (GeneralSecurityException gse) {
                throw new ChromeCastException("Unexpected security exception", gse);
            }
        }

        message.requestId = requestCounter.getAndIncrement();
        ResultProcessor<T> rp = new ResultProcessor<T>();
        requests.put(message.requestId, rp);

        write(namespace, message, destinationId);
        try {
            T response = rp.get();
            if (response instanceof Response.Invalid) {
                Response.Invalid invalid = (Response.Invalid) response;
                throw new ChromeCastException("Invalid request: " + invalid.reason);
            } else if (response instanceof Response.LoadFailed) {
                throw new ChromeCastException("Unable to load media");
            } else if (response instanceof Response.LaunchError) {
                Response.LaunchError launchError = (Response.LaunchError) response;
                throw new ChromeCastException("Application launch error: " + launchError.reason);
            }
            return response;
        } finally {
            requests.remove(message.requestId);
        }
    }

    private void write(String namespace, Message message, String destinationId) throws IOException {
        write(namespace, jsonMapper.writeValueAsString(message), destinationId);
    }

    private void write(String namespace, String message, String destinationId) throws IOException {
        LOG.debug(" --> {}", message);
        CastChannel.CastMessage msg = CastChannel.CastMessage.newBuilder()
                .setProtocolVersion(CastChannel.CastMessage.ProtocolVersion.CASTV2_1_0)
                .setSourceId(name)
                .setDestinationId(destinationId)
                .setNamespace(namespace)
                .setPayloadType(CastChannel.CastMessage.PayloadType.STRING)
                .setPayloadUtf8(message)
                .build();
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
            int nextByte = is.read();
            if (nextByte == -1) {
                throw new ChromeCastException("Remote socket closed");
            }
            buf[read++] = (byte) nextByte;
        }

        int size = fromArray(buf);
        buf = new byte[size];
        read = 0;
        while (read < size) {
            int nowRead = is.read(buf, read, buf.length - read);
            if (nowRead == -1) {
                throw new ChromeCastException("Remote socket closed");
            }
            read += nowRead;
        }

        return CastChannel.CastMessage.parseFrom(buf);
    }

    public Status getStatus() throws IOException {
        Response.Status status = send("urn:x-cast:com.google.cast.receiver", Request.status(), "receiver-0");
        return status == null ? null : status.status;
    }

    public boolean isAppAvailable(String appId) throws IOException {
        Response.AppAvailability availability = send("urn:x-cast:com.google.cast.receiver", Request.appAvailability(appId), "receiver-0");
        return availability != null && "APP_AVAILABLE".equals(availability.availability.get(appId));
    }

    public Status launch(String appId) throws IOException {
        Response.Status status = send("urn:x-cast:com.google.cast.receiver", Request.launch(appId), DEFAULT_RECEIVER_ID);
        return status == null ? null : status.status;
    }

    public Status stop(String sessionId) throws IOException {
        Response.Status status = send("urn:x-cast:com.google.cast.receiver", Request.stop(sessionId), DEFAULT_RECEIVER_ID);
        return status == null ? null : status.status;
    }

    private void startSession(String destinationId) throws IOException {
        if (!sessions.contains(destinationId)) {
            write("urn:x-cast:com.google.cast.tp.connection", Message.connect(), destinationId);
            sessions.add(destinationId);
        }
    }

    public MediaStatus load(String destinationId, String sessionId, Media media, boolean autoplay, double currentTime, Map<String, String> customData) throws IOException {
        startSession(destinationId);
        Response.MediaStatus status = send("urn:x-cast:com.google.cast.media", Request.load(sessionId, media, autoplay, currentTime, customData), destinationId);
        return status == null || status.statuses.length == 0 ? null : status.statuses[0];
    }

    public MediaStatus play(String destinationId, String sessionId, long mediaSessionId) throws IOException {
        startSession(destinationId);
        Response.MediaStatus status = send("urn:x-cast:com.google.cast.media", Request.play(sessionId, mediaSessionId), destinationId);
        return status == null || status.statuses.length == 0 ? null : status.statuses[0];
    }

    public MediaStatus pause(String destinationId, String sessionId, long mediaSessionId) throws IOException {
        startSession(destinationId);
        Response.MediaStatus status = send("urn:x-cast:com.google.cast.media", Request.pause(sessionId, mediaSessionId), destinationId);
        return status == null || status.statuses.length == 0 ? null : status.statuses[0];
    }

    public MediaStatus seek(String destinationId, String sessionId, long mediaSessionId, double currentTime) throws IOException {
        startSession(destinationId);
        Response.MediaStatus status = send("urn:x-cast:com.google.cast.media", Request.seek(sessionId, mediaSessionId, currentTime), destinationId);
        return status == null || status.statuses.length == 0 ? null : status.statuses[0];
    }

    public Status setVolume(Volume volume) throws IOException {
        Response.Status status = send("urn:x-cast:com.google.cast.receiver", Request.setVolume(volume), DEFAULT_RECEIVER_ID);
        return status == null ? null : status.status;
    }

    public MediaStatus getMediaStatus(String destinationId) throws IOException {
        startSession(destinationId);
        Response.MediaStatus status = send("urn:x-cast:com.google.cast.media", Request.status(), destinationId);
        return status == null || status.statuses.length == 0 ? null : status.statuses[0];
    }

    @Override
    public void close() throws IOException {
        closed = true;
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

    public boolean isClosed() {
        return closed;
    }

	 public Response castUrl(String destinationId, String url, boolean force, boolean reload, int reloadTimeMs) throws IOException {
        startSession(destinationId);
        Response status = send(ChromeCast.DASHCAST_NS, Request.castUrl(url, force, reload, reloadTimeMs), destinationId);
        return status;
	 }
}
