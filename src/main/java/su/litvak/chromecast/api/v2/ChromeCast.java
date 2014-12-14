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
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.Map;

/**
 * ChromeCast device - main object used for interaction with ChromeCast dongle.
 */
public class ChromeCast {
    public final static String SERVICE_TYPE = "_googlecast._tcp.local.";

    private String name;
    private final String address;
    private final int port;
    private String appsURL;
    private String application;
    private Channel channel;

    public ChromeCast(JmDNS mDNS, String name) {
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public int getPort() {
        return port;
    }

    public String getAppsURL() {
        return appsURL;
    }

    public void setAppsURL(String appsURL) {
        this.appsURL = appsURL;
    }

    public String getApplication() {
        return application;
    }

    public void setApplication(String application) {
        this.application = application;
    }

    public synchronized void connect() throws IOException, GeneralSecurityException {
        if (channel == null) {
            channel = new Channel(getAddress(), getPort());
        }
    }

    public synchronized void disconnect() throws IOException {
        if (channel == null) {
            return;
        }

        channel.close();
        channel = null;
    }

    public boolean isConnected() {
        return (channel != null && !channel.isClosed());
    }

    /**
     * @return current chromecast status - volume, running applications, etc.
     * @throws IOException
     */
    public Status getStatus() throws IOException {
        return channel.getStatus();
    }

    /**
     * @return descriptor of currently running application
     * @throws IOException
     */
    public Application getRunningApp() throws IOException {
        Status status = getStatus();
        return status.getRunningApp();
    }

    /**
     * @param appId    application identifier
     * @return  true if application is available to this chromecast device, false otherwise
     * @throws IOException
     */
    public boolean isAppAvailable(String appId) throws IOException {
        return channel.isAppAvailable(appId);
    }

    /**
     * @param appId application identifier
     * @return true if application with specified identifier is running now
     * @throws IOException
     */
    public boolean isAppRunning(String appId) throws IOException {
        Status status = getStatus();
        return status.getRunningApp() != null && appId.equals(status.getRunningApp().id);
    }

    /**
     * @param appId    application identifier
     * @return application descriptor if app successfully launched, null otherwise
     * @throws IOException
     */
    public Application launchApp(String appId) throws IOException {
        Status status = channel.launch(appId);
        return status == null ? null : status.getRunningApp();
    }

    /**
     * Stops currently running application
     *
     * @throws IOException
     */
    public void stopApp() throws IOException {
        channel.stop(getRunningApp().sessionId);
    }

    /**
     * @param level volume level from 0 to 1 to set
     */
    public void setVolume(float level) throws IOException {
        channel.setVolume(new Volume(level, false));
    }

    /**
     * @param muted is to mute or not
     */
    public void setMuted(boolean muted) throws IOException {
        channel.setVolume(new Volume(null, muted));
    }

    /**
     * @return current media status, state, time, playback rate, etc.
     * @throws IOException
     */
    public MediaStatus getMediaStatus() throws IOException {
        return channel.getMediaStatus(getRunningApp().transportId);
    }

    /**
     * Resume paused media playback
     * 
     * @throws IOException
     */
    public void play() throws IOException {
        Status status = getStatus();
        MediaStatus mediaStatus = channel.getMediaStatus(status.getRunningApp().transportId);
        if (mediaStatus == null) {
            throw new ChromeCastException("ChromeCast has invalid state to resume media playback");
        }
        channel.play(status.getRunningApp().transportId, status.getRunningApp().sessionId, mediaStatus.mediaSessionId);
    }

    /**
     * Pause current playback
     * 
     * @throws IOException
     */
    public void pause() throws IOException {
        Status status = getStatus();
        MediaStatus mediaStatus = channel.getMediaStatus(status.getRunningApp().transportId);
        if (mediaStatus == null) {
            throw new ChromeCastException("ChromeCast has invalid state to pause media playback");
        }
        channel.pause(status.getRunningApp().transportId, status.getRunningApp().sessionId, mediaStatus.mediaSessionId);
    }

    /**
     * Moves current playback time point to specified value
     * 
     * @param time time point between zero and media duration
     * @throws IOException
     */
    public void seek(double time) throws IOException {
        Status status = getStatus();
        MediaStatus mediaStatus = channel.getMediaStatus(status.getRunningApp().transportId);
        if (mediaStatus == null) {
            throw new ChromeCastException("ChromeCast has invalid state to seek media playback");
        }
        channel.seek(status.getRunningApp().transportId, status.getRunningApp().sessionId, mediaStatus.mediaSessionId, time);
    }

    /**
     * Loads and starts playing media in specified URL
     *
     * @param url    media url
     * @throws IOException
     */
    public void load(String url) throws IOException {
        load(url.substring(url.lastIndexOf('/') + 1, url.lastIndexOf('.')), null, url, null);
    }

    /**
     * Loads and starts playing specified media
     *
     * @param title name to be displayed
     * @param thumb url of video thumbnail to be displayed, relative to media url
     * @param url   media url
     * @param contentType    MIME content type
     * @throws IOException
     */
    public void load(String title, String thumb, String url, String contentType) throws IOException {
        Status status = getStatus();
        Map<String, String> customData = new HashMap<String, String>(2);
        customData.put("title:", title);
        customData.put("thumb", thumb);
        channel.load(status.getRunningApp().transportId, status.getRunningApp().sessionId, new Media(url, contentType), true, 0d, customData);
    }
}
