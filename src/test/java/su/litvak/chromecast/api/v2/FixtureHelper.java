package su.litvak.chromecast.api.v2;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class FixtureHelper {

    public static String fixtureAsString(final String res) throws IOException {
        final InputStream is = FixtureHelper.class.getResourceAsStream(res);
        try {
            final BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            final StringBuilder sb = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            return sb.toString();
        }
        finally {
            is.close();
        }
    }

}
