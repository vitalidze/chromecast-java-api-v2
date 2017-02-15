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

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceInfo;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.Map;

/**
 * ChromeCast device - main object used for interaction with ChromeCast dongle.
 */
public class ChromeCast {
    public static final String SERVICE_TYPE = "_googlecast._tcp.local.";

    private final EventListenerHolder eventListenerHolder = new EventListenerHolder();

    private String name;
    private final String address;
    private final int port;
    private String appsURL;
    private String application;
    private Channel channel;
    private boolean autoReconnect = true;

    ChromeCast(JmDNS mDNS, String name) {
        this.name = name;
        ServiceInfo serviceInfo = mDNS.getServiceInfo(SERVICE_TYPE, name);
        this.address = serviceInfo.getInet4Addresses()[0].getHostAddress();
        this.port = serviceInfo.getPort();
        this.appsURL = serviceInfo.getURLs().length == 0 ? null : serviceInfo.getURLs()[0];
        this.application = serviceInfo.getApplication();
    }

    public ChromeCast(String address) {
        this(address, 8009);
    }

    public ChromeCast(String address, int port) {
        this.address = address;
        this.port = port;
    }

    public final String getName() {
        return name;
    }

    public final void setName(String name) {
        this.name = name;
    }

    public final String getAddress() {
        return address;
    }

    public final int getPort() {
        return port;
    }

    public final String getAppsURL() {
        return appsURL;
    }

    public final void setAppsURL(String appsURL) {
        this.appsURL = appsURL;
    }

    public final String getApplication() {
        return application;
    }

    public final void setApplication(String application) {
        this.application = application;
    }

   /**
    * Returns the {@link #channel}. May open it if <code>autoReconnect</code> is set to "true" (default value)
    * and it's not yet or no longer open.
    * @return an open channel.
    */
    private synchronized Channel channel() throws IOException {
        if (autoReconnect) {
            try {
                connect();
            } catch (GeneralSecurityException e) {
                throw new IOException(e);
            }
        }

        return channel;
    }

    public final synchronized void connect() throws IOException, GeneralSecurityException {
        if (channel == null || channel.isClosed()) {
            channel = new Channel(this.address, this.port, this.eventListenerHolder);
            channel.open();
        }
    }

    public final synchronized void disconnect() throws IOException {
        if (channel == null) {
            return;
        }

        channel.close();
        channel = null;
    }

    public final boolean isConnected() {
        return channel != null && !channel.isClosed();
    }

    /**
     * Changes behaviour for opening/closing of connection with ChromeCast device. If set to "true" (default value)
     * then connection will be re-established on every request in case it is not present yet, or has been lost.
     * "false" value means manual control over connection with ChromeCast device, i.e. calling <code>connect()</code>
     * or <code>disconnect()</code> methods when needed.
     *
     * @param autoReconnect true means controlling connection with ChromeCast device automatically, false - manually
     * @see #connect()
     * @see #disconnect()
     */
    public void setAutoReconnect(boolean autoReconnect) {
        this.autoReconnect = autoReconnect;
    }

    /**
     * @return current value of <code>autoReconnect</code> setting, which controls opening/closing of connection
     * with ChromeCast device
     *
     * @see #setAutoReconnect(boolean)
     */
    public boolean isAutoReconnect() {
        return autoReconnect;
    }

    /**
     * @return current chromecast status - volume, running applications, etc.
     * @throws IOException
     */
    public final Status getStatus() throws IOException {
        return channel().getStatus();
    }

    /**
     * @return descriptor of currently running application
     * @throws IOException
     */
    public final Application getRunningApp() throws IOException {
        Status status = getStatus();
        return status.getRunningApp();
    }

    /**
     * @param appId    application identifier
     * @return  true if application is available to this chromecast device, false otherwise
     * @throws IOException
     */
    public final boolean isAppAvailable(String appId) throws IOException {
        return channel().isAppAvailable(appId);
    }

    /**
     * @param appId application identifier
     * @return true if application with specified identifier is running now
     * @throws IOException
     */
    public final boolean isAppRunning(String appId) throws IOException {
        Status status = getStatus();
        return status.getRunningApp() != null && appId.equals(status.getRunningApp().id);
    }

    /**
     * @param appId    application identifier
     * @return application descriptor if app successfully launched, null otherwise
     * @throws IOException
     */
    public final Application launchApp(String appId) throws IOException {
        Status status = channel().launch(appId);
        return status == null ? null : status.getRunningApp();
    }

    /**
     * <p>Stops currently running application</p>
     *
     * <p>If no application is running at the moment then exception is thrown.</p>
     *
     * @throws IOException
     */
    public final void stopApp() throws IOException {
        Application runningApp = getRunningApp();
        if (runningApp == null) {
            throw new ChromeCastException("No application is running in ChromeCast");
        }
        channel().stop(runningApp.sessionId);
    }

    /**
     * @param level volume level from 0 to 1 to set
     */
    public final void setVolume(float level) throws IOException {
        channel().setVolume(new Volume(level, false, Volume.DEFAULT_INCREMENT,
            Volume.DEFAULT_INCREMENT.doubleValue(), Volume.DEFAULT_CONTROL_TYPE));
    }

    /**
     * @param muted is to mute or not
     */
    public final void setMuted(boolean muted) throws IOException {
        channel().setVolume(new Volume(null, muted, Volume.DEFAULT_INCREMENT,
            Volume.DEFAULT_INCREMENT.doubleValue(), Volume.DEFAULT_CONTROL_TYPE));
    }

    /**
     * <p>If no application is running at the moment then exception is thrown.</p>
     *
     * @return current media status, state, time, playback rate, etc.
     * @throws IOException
     */
    public final MediaStatus getMediaStatus() throws IOException {
        Application runningApp = getRunningApp();
        if (runningApp == null) {
            throw new ChromeCastException("No application is running in ChromeCast");
        }
        return channel().getMediaStatus(runningApp.transportId);
    }

    /**
     * <p>Resume paused media playback</p>
     *
     * <p>If no application is running at the moment then exception is thrown.</p>
     *
     * @throws IOException
     */
    public final void play() throws IOException {
        Status status = getStatus();
        Application runningApp = status.getRunningApp();
        if (runningApp == null) {
            throw new ChromeCastException("No application is running in ChromeCast");
        }
        MediaStatus mediaStatus = channel().getMediaStatus(runningApp.transportId);
        if (mediaStatus == null) {
            throw new ChromeCastException("ChromeCast has invalid state to resume media playback");
        }
        channel().play(runningApp.transportId, runningApp.sessionId, mediaStatus.mediaSessionId);
    }

    /**
     * <p>Pause current playback</p>
     *
     * <p>If no application is running at the moment then exception is thrown.</p>
     *
     * @throws IOException
     */
    public final void pause() throws IOException {
        Status status = getStatus();
        Application runningApp = status.getRunningApp();
        if (runningApp == null) {
            throw new ChromeCastException("No application is running in ChromeCast");
        }
        MediaStatus mediaStatus = channel().getMediaStatus(runningApp.transportId);
        if (mediaStatus == null) {
            throw new ChromeCastException("ChromeCast has invalid state to pause media playback");
        }
        channel().pause(runningApp.transportId, runningApp.sessionId, mediaStatus.mediaSessionId);
    }

    /**
     * <p>Moves current playback time point to specified value</p>
     *
     * <p>If no application is running at the moment then exception is thrown.</p>
     *
     * @param time time point between zero and media duration
     * @throws IOException
     */
    public final void seek(double time) throws IOException {
        Status status = getStatus();
        Application runningApp = status.getRunningApp();
        if (runningApp == null) {
            throw new ChromeCastException("No application is running in ChromeCast");
        }
        MediaStatus mediaStatus = channel().getMediaStatus(runningApp.transportId);
        if (mediaStatus == null) {
            throw new ChromeCastException("ChromeCast has invalid state to seek media playback");
        }
        channel().seek(runningApp.transportId, runningApp.sessionId, mediaStatus.mediaSessionId, time);
    }

    private String getContentType(String url) {
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) new URL(url).openConnection();
            connection.connect();
            return connection.getContentType();
        } catch (IOException e) {
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
        return null;
    }

    /**
     * <p>Loads and starts playing media in specified URL</p>
     *
     * <p>If no application is running at the moment then exception is thrown.</p>
     *
     * @param url    media url
     * @return The new media status that resulted from loading the media.
     * @throws IOException
     */
    public final MediaStatus load(String url) throws IOException {
        return load(url.substring(url.lastIndexOf('/') + 1, url.lastIndexOf('.')), null, url, null);
    }

    /**
     * <p>Loads and starts playing specified media</p>
     *
     * <p>If no application is running at the moment then exception is thrown.</p>
     *
     * @param title name to be displayed
     * @param thumb url of video thumbnail to be displayed, relative to media url
     * @param url   media url
     * @param contentType    MIME content type
     * @return The new media status that resulted from loading the media.
     * @throws IOException
     */
    public final MediaStatus load(String title, String thumb, String url, String contentType) throws IOException {
        Status status = getStatus();
        Application runningApp = status.getRunningApp();
        if (runningApp == null) {
            throw new ChromeCastException("No application is running in ChromeCast");
        }
        Map<String, Object> metadata = new HashMap<String, Object>(2);
        metadata.put("title", title);
        metadata.put("thumb", thumb);
        return channel().load(runningApp.transportId, runningApp.sessionId, new Media(url,
                contentType == null ? getContentType(url) : contentType, null, null, null,
                metadata, null, null), true, 0d, null);
    }

    /**
     * <p>Loads and starts playing specified media</p>
     *
     * <p>If no application is running at the moment then exception is thrown.</p>
     *
     * @param media The media to load and play.
     *              See <a href="https://developers.google.com/cast/docs/reference/messages#Load">
     *                  https://developers.google.com/cast/docs/reference/messages#Load</a> for more details.
     * @return The new media status that resulted from loading the media.
     * @throws IOException
     */
    public final MediaStatus load(final Media media) throws IOException {
        Status status = getStatus();
        Application runningApp = status.getRunningApp();
        if (runningApp == null) {
            throw new ChromeCastException("No application is running in ChromeCast");
        }
        Media mediaToPlay;
        if (media.contentType == null) {
            mediaToPlay = new Media(media.url, getContentType(media.url), media.duration, media.streamType,
                    media.customData, media.metadata, media.textTrackStyle, media.tracks);
        } else {
            mediaToPlay = media;
        }
        return channel().load(runningApp.transportId, runningApp.sessionId, mediaToPlay, true, 0d, null);
    }

    /**
     * <p>Sends some generic request to the currently running application.</p>
     *
     * <p>If no application is running at the moment then exception is thrown.</p>
     *
     * @param namespace         request namespace
     * @param request           request object
     * @param responseClass     class of the response for proper deserialization
     * @param <T>               type of response
     * @return                  deserialized response
     * @throws IOException
     */
    public final <T extends Response> T send(String namespace, Request request, Class<T> responseClass)
            throws IOException {
        Status status = getStatus();
        Application runningApp = status.getRunningApp();
        if (runningApp == null) {
            throw new ChromeCastException("No application is running in ChromeCast");
        }
        return channel().sendGenericRequest(runningApp.transportId, namespace, request, responseClass);
    }

    /**
     * <p>Sends some generic request to the currently running application.
     * No response is expected as a result of this call.</p>
     *
     * <p>If no application is running at the moment then exception is thrown.</p>
     *
     * @param namespace     request namespace
     * @param request       request object
     * @throws IOException
     */
    public final void send(String namespace, Request request) throws IOException {
        send(namespace, request, null);
    }

    public final void registerListener(ChromeCastSpontaneousEventListener listener) {
        this.eventListenerHolder.registerListener(listener);
    }

    public final void unregisterListener(ChromeCastSpontaneousEventListener listener) {
        this.eventListenerHolder.unregisterListener(listener);
    }

    public final void registerConnectionListener(ChromeCastConnectionEventListener listener) {
        this.eventListenerHolder.registerConnectionListener(listener);
    }

    public final void unregisterConnectionListener(ChromeCastConnectionEventListener listener) {
        this.eventListenerHolder.unregisterConnectionListener(listener);
    }
}
