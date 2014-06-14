package su.litvak.chromecast.api.v2;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceInfo;
import java.io.IOException;
import java.security.GeneralSecurityException;

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

    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
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

    /**
     * @return  current chromecast status - volume, running applications, etc.
     * @throws IOException
     */
    public Status getStatus() throws IOException {
        return channel.getStatus();
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
     * @param appId    application identifier
     * @return  application descriptor if app successfully launched, null otherwise
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
        Status status = getStatus();
        if (status == null) {
            return;
        }
        stopApp(status.getRunningApp().sessionId);
    }

    /**
     * @param sessionId application session identifier
     * @throws IOException
     */
    public void stopApp(String sessionId) throws IOException {
        channel.stop(sessionId);
    }
}
