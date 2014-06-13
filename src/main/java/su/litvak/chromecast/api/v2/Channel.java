package su.litvak.chromecast.api.v2;

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
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

class Channel implements Closeable {
    private static final Logger LOG = LoggerFactory.getLogger(Channel.class);

    private final Socket socket;
    private final String name;
    private Timer pingTimer;
    private ReadThread reader;
    private AtomicLong requestCounter = new AtomicLong(0);
    private Map<Long, ResultProcessor> requests = new ConcurrentHashMap<Long, ResultProcessor>();
    private final ObjectMapper jsonMapper = new ObjectMapper();

    private class PingThread extends TimerTask {
        @Override
        public void run() {
            try {
                write("urn:x-cast:com.google.cast.tp.heartbeat", Message.ping(), "receiver-0");
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
                try {
                    CastChannel.CastMessage message = read();
                    if (message.getPayloadType() == CastChannel.CastMessage.PayloadType.STRING) {
                        System.out.println(" <-- " + message.getPayloadUtf8());
                        final String jsonMSG = message.getPayloadUtf8().replaceFirst("\"type\"", "\"responseType\"");
                        Response parsed = jsonMapper.readValue(jsonMSG, Response.class);
                        if (parsed.requestId != null) {
                            ResultProcessor rp = requests.remove(parsed.requestId);
                            if (rp != null) {
                                rp.put(parsed);
                            } else {
                                LOG.warn("Unable to process request ID = {}, data: {}", parsed.requestId, jsonMSG);
                            }
                        }
                    } else {
                        LOG.warn("Received unexpected {} message", message.getPayloadType());
                    }
                } catch (IOException ioex) {
                    LOG.warn("Error while reading: {}", ioex.getLocalizedMessage());
                    System.out.println(ioex.getLocalizedMessage());
                }
            }
        }
    }

    private class ResultProcessor<T extends Response> {
        AtomicReference<T> result = new AtomicReference<T>();

        private ResultProcessor() {
        }

        public void put(Response result) {
            synchronized (this) {
                this.result.set((T) result);
                this.notify();
            }
        }

        public T get() {
            if (result.get() != null) {
                return result.get();
            }
            synchronized (this) {
                // TODO put timeout to constant
                try {
                    this.wait(5 * 1000);
                } catch (InterruptedException ie) {
                    // TODO either move to the 'throws' or put some logging here
                    // TODO remove this from requests (add timer or so)
                    ie.printStackTrace();
                }
                return result.get();
            }
        }
    }

    Channel(String host) throws IOException, GeneralSecurityException {
        this(host, 8009);
    }

    Channel(String host, int port) throws IOException, GeneralSecurityException {
        SSLContext sc = SSLContext.getInstance("SSL");
        sc.init(null, new TrustManager[] { new X509TrustAllManager() }, new SecureRandom());
        this.socket = sc.getSocketFactory().createSocket(host, port);
        this.name = "sender-" + new RandomString(10).nextString();
        connect();
    }

    private void connect() throws IOException {
        CastChannel.DeviceAuthMessage authMessage = CastChannel.DeviceAuthMessage.newBuilder()
                .setChallenge(CastChannel.AuthChallenge.newBuilder().build())
                .build();

        CastChannel.CastMessage msg = CastChannel.CastMessage.newBuilder()
                .setDestinationId("receiver-0")
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
            throw new IOException("Authentication failed: " + authResponse.getError().getErrorType().toString());
        }

        /**
         * Send 'PING' message
         */
        PingThread pingThread = new PingThread();
        pingThread.run();

        /**
         * Send 'CONNECT' message to start session
         */
        write("urn:x-cast:com.google.cast.tp.connection", Message.connect(), "receiver-0");

        /**
         * Start ping/pong and reader thread
         */
        pingTimer = new Timer(name + " PING");
        // TODO move PING interval to constants
        pingTimer.schedule(pingThread, 5 * 1000, 30 * 1000);

        reader = new ReadThread();
        reader.start();
    }

    private <T extends Response> T send(String namespace, Request message, String destinationId) throws IOException {
        message.requestId = requestCounter.getAndIncrement();
        ResultProcessor<T> rp = new ResultProcessor<T>();
        requests.put(message.requestId, rp);
        write(namespace, message, destinationId);
        return rp.get();
    }

    private void write(String namespace, Message message, String destinationId) throws IOException {
        System.out.println(" --> " + jsonMapper.writeValueAsString(message));
        write(namespace, jsonMapper.writeValueAsString(message), destinationId);
    }

    private void write(String namespace, String message, String destinationId) throws IOException {
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
        is.read(buf);
        int size = fromArray(buf);
        buf = new byte[size];
        is.read(buf);
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
        Response.Status status = send("urn:x-cast:com.google.cast.receiver", Request.launch(appId), "receiver-0");
        return status == null ? null : status.status;
    }

//    public JSONObject play(String sessionId, String url, String destinationId) throws IOException {
//        JSONObject jMSG = new JSONObject();
//        jMSG.put("type", "CONNECT");
//        jMSG.put("origin", new JSONObject());
//        write("urn:x-cast:com.google.cast.tp.connection", jMSG, destinationId);
//
//        JSONObject msg = new JSONObject();
//        msg.put("type", "LOAD");
//        msg.put("sessionId", sessionId);
//
//        JSONObject media = new JSONObject();
//        media.put("contentId", url);
//        media.put("streamType", "buffered");
//        media.put("contentType", "video/mp4");
//
//        msg.put("media", media);
//        msg.put("autoplay", true);
//        msg.put("currentTime", 0);
//
//        JSONObject customData = new JSONObject();
//        JSONObject payload = new JSONObject();
//        payload.put("title:", "Big Buck Bunny");
//        payload.put("thumb", "images/BigBuckBunny.jpg");
//        customData.put("payload", payload);
//
//        msg.put("customData", customData);
//
//        return send("urn:x-cast:com.google.cast.media", msg, destinationId);
//    }

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

    private static int fromArray(byte[] payload){
        ByteBuffer buffer = ByteBuffer.wrap(payload);
        buffer.order(ByteOrder.BIG_ENDIAN);
        return buffer.getInt();
    }

    private static byte[] toArray(int value){
        ByteBuffer buffer = ByteBuffer.allocate(4);
        buffer.order(ByteOrder.BIG_ENDIAN);
        buffer.putInt(value);
        buffer.flip();
        return buffer.array();
    }
}
