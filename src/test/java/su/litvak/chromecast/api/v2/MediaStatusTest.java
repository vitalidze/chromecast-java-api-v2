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

import static org.junit.Assert.*;

import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Test;

public class MediaStatusTest {
    final ObjectMapper jsonMapper = new ObjectMapper();

    @Test
    public void testDeserializationWithIdleReason() throws Exception {
        // Response.MediaStatus response = (Response.MediaStatus) jsonMapper.readValue("{\"responseType\":\"MEDIA_STATUS\",\"status\":[{\"mediaSessionId\":1,\"playbackRate\":1,\"playerState\":\"IDLE\",\"currentTime\":0,\"supportedMediaCommands\":15,\"volume\":{\"level\":1,\"muted\":false},\"media\":{\"contentId\":\"/public/Videos/Movies/FileB.mp4\",\"contentType\":\"video/transcode\",\"streamType\":\"buffered\",\"duration\":null},\"idleReason\":\"ERROR\"}],\"requestId\":28}", Response.class);
        // assertEquals(1, response.statuses.length);
        // MediaStatus mediaStatus = response.statuses[0];
        // assertEquals("ERROR", mediaStatus.idleReason);
    }

    @Test
    public void testDeserializationWithoutIdleReason() throws Exception {
        // Response.MediaStatus response = (Response.MediaStatus) jsonMapper.readValue("{\"responseType\":\"MEDIA_STATUS\",\"status\":[{\"mediaSessionId\":1,\"playbackRate\":1,\"playerState\":\"IDLE\",\"currentTime\":0,\"supportedMediaCommands\":15,\"volume\":{\"level\":1,\"muted\":false},\"media\":{\"contentId\":\"/public/Videos/Movies/FileB.mp4\",\"contentType\":\"video/transcode\",\"streamType\":\"buffered\",\"duration\":null}}],\"requestId\":28}", Response.class);
        // assertEquals(1, response.statuses.length);
        // MediaStatus mediaStatus = response.statuses[0];
        // assertNull(mediaStatus.idleReason);
    }
}
