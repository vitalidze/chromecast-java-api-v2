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

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Contains utility methods.
 */
final class Util {
    private Util() {
    }

    /**
     * Converts specified byte array in Big Endian to int.
     */
    static int fromArray(byte[] payload) {
        return payload[0] << 24 | (payload[1] & 0xFF) << 16 | (payload[2] & 0xFF) << 8 | (payload[3] & 0xFF);
    }

    /**
     * Converts specified int to byte array in Big Endian.
     */
    static byte[] toArray(int value) {
        return new byte[] {
            (byte) (value >> 24),
            (byte) (value >> 16),
            (byte) (value >> 8),
            (byte) value };
    }

    static String getContentType(String url) {
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

    static String getMediaTitle(String url) {
        try {
            URL urlObj = new URL(url);
            String mediaTitle;
            String path = urlObj.getPath();
            int lastIndexOfSlash = path.lastIndexOf('/');
            if (lastIndexOfSlash >= 0 && lastIndexOfSlash + 1 < url.length()) {
                mediaTitle = path.substring(lastIndexOfSlash + 1);
                int lastIndexOfDot = mediaTitle.lastIndexOf('.');
                if (lastIndexOfDot > 0) {
                    mediaTitle = mediaTitle.substring(0, lastIndexOfDot);
                }
            } else {
                mediaTitle = path;
            }
            return mediaTitle.isEmpty() ? url : mediaTitle;
        } catch (MalformedURLException mfu) {
            return url;
        }
    }
}
