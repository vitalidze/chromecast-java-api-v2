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

import com.google.protobuf.MessageLite;
import org.codehaus.jackson.map.ObjectMapper;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

import static su.litvak.chromecast.api.v2.Util.fromArray;
import static su.litvak.chromecast.api.v2.Util.toArray;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.*;
import java.util.Collections;

public class MockedChromeCast {
    final ServerSocket socket;
    final ClientThread clientThread;

    MockedChromeCast() throws IOException, GeneralSecurityException {
        SSLContext sc = SSLContext.getInstance("SSL");
        KeyStore keyStore = KeyStore.getInstance("JKS");
        keyStore.load(getClass().getResourceAsStream("/keystore.jks"), "changeit".toCharArray());

        KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        keyManagerFactory.init(keyStore, "changeit".toCharArray());

        sc.init(keyManagerFactory.getKeyManagers(), new TrustManager[] { new X509TrustAllManager() }, new SecureRandom());
        socket = sc.getServerSocketFactory().createServerSocket(8009);

        clientThread = new ClientThread();
        clientThread.start();
    }

    class ClientThread extends Thread {
        volatile boolean stop;
        Socket clientSocket;
        ObjectMapper jsonMapper = new ObjectMapper();

        @Override
        public void run() {
            try {
                clientSocket = socket.accept();
                while (!stop) {
                    handle(read(clientSocket));
                }
            } catch (IOException ioex) {
                ioex.printStackTrace();
            } finally {
                if (clientSocket != null) {
                    try {
                        clientSocket.close();
                    } catch (IOException ioex) {
                        ioex.printStackTrace();
                    }
                }
            }
        }

        void handle(CastChannel.CastMessage message) throws IOException {
            System.out.println("Received message: ");
            System.out.println("   sourceId: " + message.getSourceId());
            System.out.println("   destinationId: " + message.getDestinationId());
            System.out.println("   namespace: " + message.getNamespace());
            System.out.println("   payloadType: " + message.getPayloadType());
            if (message.getPayloadType() == CastChannel.CastMessage.PayloadType.STRING) {
                System.out.println("   payload: " + message.getPayloadUtf8());
            }

            if (message.getPayloadType() == CastChannel.CastMessage.PayloadType.BINARY) {
                MessageLite response = handleBinary(CastChannel.DeviceAuthMessage.parseFrom(message.getPayloadBinary()));
                write(clientSocket,
                        CastChannel.CastMessage.newBuilder()
                                .setProtocolVersion(message.getProtocolVersion())
                                .setSourceId(message.getDestinationId())
                                .setDestinationId(message.getSourceId())
                                .setNamespace(message.getNamespace())
                                .setPayloadType(CastChannel.CastMessage.PayloadType.BINARY)
                                .setPayloadBinary(response.toByteString())
                                .build());
            } else {
                StandardMessage json = jsonMapper.readValue(message.getPayloadUtf8(), StandardMessage.class);
                StandardResponse response = handleJSON(json);
                if (response != null) {
                    if (json instanceof Request) {
                        Request request = (Request) json;
                        response.requestId = request.getRequestId();
                    }

                    write(clientSocket,
                            CastChannel.CastMessage.newBuilder()
                                    .setProtocolVersion(message.getProtocolVersion())
                                    .setSourceId(message.getDestinationId())
                                    .setDestinationId(message.getSourceId())
                                    .setNamespace(message.getNamespace())
                                    .setPayloadType(CastChannel.CastMessage.PayloadType.STRING)
                                    .setPayloadUtf8(jsonMapper.writeValueAsString(response))
                                    .build());
                }
            }
        }

        MessageLite handleBinary(CastChannel.DeviceAuthMessage message) throws IOException {
            return message;
        }

        StandardResponse handleJSON(Message message) {
            if (message instanceof StandardMessage.Ping) {
                return new StandardResponse.Pong();
            } else if (message instanceof StandardRequest.Status) {
                Status status = new Status(new Volume(1f, false, Volume.default_increment,
                        Volume.default_increment.doubleValue(), Volume.default_controlType), Collections.<Application>emptyList(), false, true);
                return new StandardResponse.Status(status);
            }
            return null;
        }

        CastChannel.CastMessage read(Socket socket) throws IOException {
            InputStream is = socket.getInputStream();
            byte[] buf = new byte[4];

            int read = 0;
            while (read < buf.length) {
                int nextByte = is.read();
                if (nextByte == -1) {
                    throw new ChromeCastException("Remote socket was closed");
                }
                buf[read++] = (byte) nextByte;
            }

            int size = fromArray(buf);
            buf = new byte[size];
            read = 0;
            while (read < size) {
                int nowRead = is.read(buf, read, buf.length - read);
                if (nowRead == -1) {
                    throw new ChromeCastException("Remote socket was closed");
                }
                read += nowRead;
            }

            return CastChannel.CastMessage.parseFrom(buf);
        }

        void write(Socket socket, CastChannel.CastMessage message) throws IOException {
            socket.getOutputStream().write(toArray(message.getSerializedSize()));
            message.writeTo(socket.getOutputStream());
        }
    }

    void close() throws IOException {
        clientThread.stop = true;
        this.socket.close();
    }
}
