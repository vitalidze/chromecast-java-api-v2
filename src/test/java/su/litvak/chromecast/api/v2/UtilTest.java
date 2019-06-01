/*
 * Copyright 2019 Vitaly Litvak (vitavaque@gmail.com)
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

import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static su.litvak.chromecast.api.v2.Util.getMediaTitle;

public class UtilTest {
    @Test
    public void testMediaTitle() throws IOException {
        assertEquals("stream", getMediaTitle("http://xxx.yyy.com:8054/stream"));
        assertEquals("stream", getMediaTitle("http://xxx.yyy.com/stream"));
        assertEquals("stream", getMediaTitle("http://zzz.aaa.com:8054/stream.mp3"));
        assertEquals("stream", getMediaTitle("http://zzz.aaa.com/stream.mp3"));
        assertEquals("stream.abc", getMediaTitle("http://zzz.aaa.com/stream.abc.mp3"));
        assertEquals("http://zzz.aaa.com/", getMediaTitle("http://zzz.aaa.com/"));
        assertEquals("http://zzz.aaa.com", getMediaTitle("http://zzz.aaa.com"));
        assertEquals("BigBuckBunny",
                getMediaTitle("http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"));
    }
}
