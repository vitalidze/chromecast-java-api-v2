package su.litvak.justdlna.chromecast.v2;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

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
import java.util.Timer;
import java.util.TimerTask;

public class Channel implements Closeable {
    private final Socket socket;
    private final String name;
    private Timer pingTimer;
    private ReadThread reader;
    private int requestCounter;

    private class PingThread extends TimerTask {
        JSONObject msg;

        PingThread()
        {
            msg = new JSONObject();
            msg.put("type", "PING");
        }


        @Override
        public void run() {
            try {
                write("urn:x-cast:com.google.cast.tp.heartbeat", msg);
            } catch (IOException ioex) {
                // TODO logging
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
                        JSONObject parsed = (JSONObject) JSONValue.parse(message.getPayloadUtf8());
                        if (parsed.get("type") != null && parsed.get("type").equals("PONG")) {
                            // TODO register heartbeat
                            continue;
                        }
                        System.out.println(parsed.toJSONString());
                    } else {
                        System.out.println(message.getPayloadType());
                    }
                } catch (IOException ioex) {
                    // TODO logging
                    ioex.printStackTrace();
                }
            }
        }
    }

    public Channel(String host) throws IOException, GeneralSecurityException {
        this(host, 8009);
    }

    public Channel(String host, int port) throws IOException, GeneralSecurityException {
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

        read();

        /**
         * Send 'CONNECT' message to start session
         */
        JSONObject jMSG = new JSONObject();
        jMSG.put("type", "CONNECT");
        jMSG.put("origin", new JSONObject());
        write("urn:x-cast:com.google.cast.tp.connection", jMSG);

        /**
         * Start ping/pong and reader thread
         */
        pingTimer = new Timer(name + " PING");
        // TODO move PING interval to constants
        pingTimer.schedule(pingThread, 5 * 1000, 30 * 1000);

        reader = new ReadThread();
        reader.start();
    }

    private int write(String namespace, JSONObject message) throws IOException {
        int requestId = -1;
        if (!message.get("type").equals("PING") || message.get("type").equals("CONNECT"))
        {
            requestId = requestCounter++;
            message.put("requestId", requestId);
        }
        write(namespace, message.toJSONString());
        return requestId;
    }

    private void write(String namespace, String message) throws IOException {
        CastChannel.CastMessage msg = CastChannel.CastMessage.newBuilder()
                .setProtocolVersion(CastChannel.CastMessage.ProtocolVersion.CASTV2_1_0)
                .setSourceId(name)
                .setDestinationId("receiver-0")
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

    public void deviceGetStatus() throws IOException {
        JSONObject msg = new JSONObject();
        msg.put("type", "GET_STATUS");

        write("urn:x-cast:com.google.cast.receiver", msg);

        // TODO create a dispatching reader in READ thread
    }

    public void deviceGetAppAvailability(String appId) throws IOException {
        JSONObject msg = new JSONObject();
        msg.put("type", "GET_APP_AVAILABILITY");
        JSONArray apps = new JSONArray();
        apps.add(appId);
        msg.put("appId", apps);

        write("urn:x-cast:com.google.cast.receiver", msg);

        // TODO create a dispatching reader in READ thread
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
