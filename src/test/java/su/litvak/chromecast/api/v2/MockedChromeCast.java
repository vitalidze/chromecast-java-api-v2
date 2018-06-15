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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.protobuf.MessageLite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import su.litvak.chromecast.api.v2.CastChannel.CastMessage;
import su.litvak.chromecast.api.v2.CastChannel.CastMessage.PayloadType;
import su.litvak.chromecast.api.v2.CastChannel.DeviceAuthMessage;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static su.litvak.chromecast.api.v2.Util.fromArray;
import static su.litvak.chromecast.api.v2.Util.toArray;

final class MockedChromeCast {
    final Logger logger = LoggerFactory.getLogger(MockedChromeCast.class);

    final ServerSocket socket;
    final ClientThread clientThread;
    List<Application> runningApplications = new ArrayList<Application>();
    CustomHandler customHandler;

    interface CustomHandler {
        Response handle(JsonNode json);
    }

    MockedChromeCast() throws IOException, GeneralSecurityException {
        SSLContext sc = SSLContext.getInstance("SSL");
        KeyStore keyStore = KeyStore.getInstance("JKS");
        keyStore.load(getClass().getResourceAsStream("/keystore.jks"), "changeit".toCharArray());

        KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        keyManagerFactory.init(keyStore, "changeit".toCharArray());

        sc.init(keyManagerFactory.getKeyManagers(), new TrustManager[] {new X509TrustAllManager()}, new SecureRandom());
        socket = sc.getServerSocketFactory().createServerSocket(8009);

        clientThread = new ClientThread();
        clientThread.start();
    }

    class ClientThread extends Thread {
        volatile boolean stop;
        Socket clientSocket;
        ObjectMapper jsonMapper = JacksonHelper.createJSONMapper();

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

        void handle(CastMessage message) throws IOException {
            logger.info("Received message: ");
            logger.info("   sourceId: " + message.getSourceId());
            logger.info("   destinationId: " + message.getDestinationId());
            logger.info("   namespace: " + message.getNamespace());
            logger.info("   payloadType: " + message.getPayloadType());
            if (message.getPayloadType() == PayloadType.STRING) {
                logger.info("   payload: " + message.getPayloadUtf8());
            }

            if (message.getPayloadType() == PayloadType.BINARY) {
                MessageLite response = handleBinary(DeviceAuthMessage.parseFrom(message.getPayloadBinary()));
                logger.info("Sending response message: ");
                logger.info("   sourceId: " + message.getDestinationId());
                logger.info("   destinationId: " + message.getSourceId());
                logger.info("   namespace: " + message.getNamespace());
                logger.info("   payloadType: " + PayloadType.BINARY);
                write(clientSocket,
                        CastMessage.newBuilder()
                                .setProtocolVersion(message.getProtocolVersion())
                                .setSourceId(message.getDestinationId())
                                .setDestinationId(message.getSourceId())
                                .setNamespace(message.getNamespace())
                                .setPayloadType(PayloadType.BINARY)
                                .setPayloadBinary(response.toByteString())
                                .build());
            } else {
                JsonNode json = jsonMapper.readTree(message.getPayloadUtf8());
                Response response = null;
                if (json.has("type")) {
                    StandardMessage standardMessage = jsonMapper.readValue(message.getPayloadUtf8(),
                            StandardMessage.class);
                    response = handleJSON(standardMessage);
                } else {
                    response = handleCustom(json);
                }

                if (response != null) {
                    if (json.has("requestId")) {
                        response.setRequestId(json.get("requestId").asLong());
                    }

                    logger.info("Sending response message: ");
                    logger.info("   sourceId: " + message.getDestinationId());
                    logger.info("   destinationId: " + message.getSourceId());
                    logger.info("   namespace: " + message.getNamespace());
                    logger.info("   payloadType: " + CastMessage.PayloadType.STRING);
                    logger.info("   payload: " + jsonMapper.writeValueAsString(response));
                    write(clientSocket,
                            CastMessage.newBuilder()
                                    .setProtocolVersion(message.getProtocolVersion())
                                    .setSourceId(message.getDestinationId())
                                    .setDestinationId(message.getSourceId())
                                    .setNamespace(message.getNamespace())
                                    .setPayloadType(CastMessage.PayloadType.STRING)
                                    .setPayloadUtf8(jsonMapper.writeValueAsString(response))
                                    .build());
                }
            }
        }

        MessageLite handleBinary(DeviceAuthMessage message) throws IOException {
            return message;
        }

        Response handleJSON(Message message) {
            if (message instanceof StandardMessage.Ping) {
                return new StandardResponse.Pong();
            } else if (message instanceof StandardRequest.Status) {
                return new StandardResponse.Status(status());
            } else if (message instanceof StandardRequest.Launch) {
                StandardRequest.Launch launch = (StandardRequest.Launch) message;
                runningApplications.add(new Application(launch.appId, launch.appId, "SESSION_ID", "",
                        false, false, "", Collections.<Namespace>emptyList()));
                StandardResponse response = new StandardResponse.Status(status());
                response.setRequestId(launch.getRequestId());
                return response;
            }
            return null;
        }

        Status status() {
            return new Status(new Volume(1f, false, Volume.DEFAULT_INCREMENT,
                        Volume.DEFAULT_INCREMENT.doubleValue(), Volume.DEFAULT_CONTROL_TYPE),
                    runningApplications, false, true);
        }

        Response handleCustom(JsonNode json) {
            if (customHandler == null) {
                logger.info("No custom handler set");
                return null;
            } else {
                return customHandler.handle(json);
            }
        }

        CastMessage read(Socket mySocket) throws IOException {
            InputStream is = mySocket.getInputStream();
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

            return CastMessage.parseFrom(buf);
        }

        void write(Socket mySocket, CastMessage message) throws IOException {
            mySocket.getOutputStream().write(toArray(message.getSerializedSize()));
            message.writeTo(mySocket.getOutputStream());
        }
    }

    void close() throws IOException {
        clientThread.stop = true;
        this.socket.close();
    }
}
