package su.litvak.justdlna.chromecast.v2;

import javax.net.ssl.X509TrustManager;
import java.security.cert.X509Certificate;

/**
 * Google Cast's certificate cannot be validated against standard keystore,
 * so use a dummy trust-all manager
 */
class X509TrustAllManager implements X509TrustManager {
    @Override
    public X509Certificate[] getAcceptedIssuers() {
        return null;
    }
    @Override
    public void checkClientTrusted(X509Certificate[] certs, String authType) {
    }
    @Override
    public void checkServerTrusted(X509Certificate[] certs, String authType) {
    }
}
