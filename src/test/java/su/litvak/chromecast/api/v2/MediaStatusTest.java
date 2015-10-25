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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Test;

import su.litvak.chromecast.api.v2.MediaStatus.PlayerState;
import su.litvak.chromecast.api.v2.MediaStatus.RepeatMode;

public class MediaStatusTest {
    final ObjectMapper jsonMapper = new ObjectMapper();

    @Test
    public void testDeserializationWithIdleReason() throws Exception {
        Response.MediaStatus response = (Response.MediaStatus) jsonMapper.readValue("{\"responseType\":\"MEDIA_STATUS\",\"status\":[{\"mediaSessionId\":1,\"playbackRate\":1,\"playerState\":\"IDLE\",\"currentTime\":0,\"supportedMediaCommands\":15,\"volume\":{\"level\":1,\"muted\":false,\"increment\":0.04},\"media\":{\"contentId\":\"/public/Videos/Movies/FileB.mp4\",\"contentType\":\"video/transcode\",\"streamType\":\"buffered\",\"duration\":null},\"idleReason\":\"ERROR\"}],\"requestId\":28}", Response.class);
        assertEquals(1, response.statuses.length);
        MediaStatus mediaStatus = response.statuses[0];
        assertEquals(MediaStatus.IdleReason.ERROR, mediaStatus.idleReason);
    }

    @Test
    public void testDeserializationWithoutIdleReason() throws Exception {
        Response.MediaStatus response = (Response.MediaStatus) jsonMapper.readValue("{\"responseType\":\"MEDIA_STATUS\",\"status\":[{\"mediaSessionId\":1,\"playbackRate\":1,\"playerState\":\"IDLE\",\"currentTime\":0,\"supportedMediaCommands\":15,\"volume\":{\"level\":1,\"muted\":false},\"media\":{\"contentId\":\"/public/Videos/Movies/FileB.mp4\",\"contentType\":\"video/transcode\",\"streamType\":\"buffered\",\"duration\":null}}],\"requestId\":28}", Response.class);
        assertEquals(1, response.statuses.length);
        MediaStatus mediaStatus = response.statuses[0];
        assertNull(mediaStatus.idleReason);
    }

    @Test
    public void testDeserializationWithChromeCastAudioFixture () throws Exception {
        final String jsonMSG = fixtureAsString("/mediaStatus-chromecast-audio.json").replaceFirst("\"type\"", "\"responseType\"");
        final Response.MediaStatus response = (Response.MediaStatus) jsonMapper.readValue(jsonMSG, Response.class);
        assertEquals(1, response.statuses.length);
        final MediaStatus mediaStatus = response.statuses[0];
        assertEquals((Integer) 1, mediaStatus.currentItemId);
        assertEquals(0f, mediaStatus.currentTime, 0f);

        final Media media = new Media("http://192.168.1.6:8192/audio-123-mp3", "audio/mpeg", 389.355102d, Media.StreamType.buffered);

        final Map<String, String> payload = new HashMap<String, String>();
        payload.put("thumb", null);
        payload.put("title", "Example Track Title");
        final Map<String, Object> customData = new HashMap<String, Object>();
        customData.put("payload", payload);
        assertEquals(Collections.singletonList(new Item(true, customData, 1, media)), mediaStatus.items);

        assertEquals(media, mediaStatus.media);
        assertEquals(1, mediaStatus.mediaSessionId);
        assertEquals(1, mediaStatus.playbackRate);
        assertEquals(PlayerState.BUFFERING, mediaStatus.playerState);
        assertEquals(RepeatMode.REPEAT_OFF, mediaStatus.repeatMode);
        assertEquals(15, mediaStatus.supportedMediaCommands);
        assertEquals(new Volume(1f, false, Volume.default_increment), mediaStatus.volume);
    }

    private String fixtureAsString (final String res) throws IOException {
        final InputStream is = getClass().getResourceAsStream(res);
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
