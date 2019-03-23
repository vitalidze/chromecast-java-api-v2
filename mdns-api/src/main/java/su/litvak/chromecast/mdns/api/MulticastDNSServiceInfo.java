/**
 * <p>Title: FundCount, LLC</p>
 * <p>Description: FundCount project</p>
 * <p>Copyright: Copyright (c) 2001-2013 Fundcount, LLC</p>
 * <p>Company: FundCount, LLC</p>
 */
package su.litvak.chromecast.mdns.api;

import java.net.Inet4Address;

/**
 * Information for service discovered by Multicast DNS.
 */
public interface MulticastDNSServiceInfo {
    Inet4Address[] getInet4Addresses();
    int getPort();
    String[] getURLs();
    String getApplication();
    String getPropertyString(String propertyName);
}
