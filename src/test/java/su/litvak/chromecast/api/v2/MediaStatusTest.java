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

import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Test;
import su.litvak.chromecast.api.v2.MediaStatus.PlayerState;
import su.litvak.chromecast.api.v2.MediaStatus.RepeatMode;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class MediaStatusTest {
    final ObjectMapper jsonMapper = JacksonHelper.createJSONMapper();

    @Test
    public void testDeserializationWithIdleReason() throws Exception {
        final StandardResponse.MediaStatus response =
                (StandardResponse.MediaStatus) jsonMapper.readValue(getClass()
                        .getResourceAsStream("/mediaStatus-with-idleReason.json"), StandardResponse.class);
        assertEquals(1, response.statuses.length);
        MediaStatus mediaStatus = response.statuses[0];
        assertEquals(MediaStatus.IdleReason.ERROR, mediaStatus.idleReason);
    }

    @Test
    public void testDeserializationWithoutIdleReason() throws Exception {
        final StandardResponse.MediaStatus response =
                (StandardResponse.MediaStatus) jsonMapper.readValue(getClass()
                        .getResourceAsStream("/mediaStatus-without-idleReason.json"), StandardResponse.class);
        assertEquals(1, response.statuses.length);
        MediaStatus mediaStatus = response.statuses[0];
        assertNull(mediaStatus.idleReason);
    }

    @Test
    public void testDeserializationWithChromeCastAudioFixture() throws Exception {
        final String jsonMSG = FixtureHelper.fixtureAsString("/mediaStatus-chromecast-audio.json")
                .replaceFirst("\"type\"", "\"responseType\"");
        final StandardResponse.MediaStatus response =
                (StandardResponse.MediaStatus) jsonMapper.readValue(jsonMSG, StandardResponse.class);
        assertEquals(1, response.statuses.length);
        final MediaStatus mediaStatus = response.statuses[0];
        assertEquals((Integer) 1, mediaStatus.currentItemId);
        assertEquals(0f, mediaStatus.currentTime, 0f);

        final Media media = new Media("http://192.168.1.6:8192/audio-123-mp3", "audio/mpeg",
                389.355102d, Media.StreamType.BUFFERED);

        final Map<String, String> payload = new HashMap<String, String>();
        payload.put("thumb", null);
        payload.put("title", "Example Track Title");
        final Map<String, Object> customData = new HashMap<String, Object>();
        customData.put("payload", payload);
        assertEquals(Collections.singletonList(new Item(true, customData, 1, media)), mediaStatus.items);

        assertEquals(media, mediaStatus.media);
        assertEquals(Media.MetadataType.GENERIC, media.getMetadataType());
        assertEquals(1, mediaStatus.mediaSessionId);
        assertEquals(1, mediaStatus.playbackRate);
        assertEquals(PlayerState.BUFFERING, mediaStatus.playerState);
        assertEquals(RepeatMode.REPEAT_OFF, mediaStatus.repeatMode);
        assertEquals(15, mediaStatus.supportedMediaCommands);
        assertEquals(new Volume(1f, false, Volume.DEFAULT_INCREMENT,
                Volume.DEFAULT_INCREMENT.doubleValue(), Volume.DEFAULT_CONTROL_TYPE), mediaStatus.volume);
    }

    @Test
    public void testDeserializationPandora() throws IOException {
        final StandardResponse.MediaStatus response =
                (StandardResponse.MediaStatus) jsonMapper.readValue(getClass()
                        .getResourceAsStream("/mediaStatus-pandora.json"), StandardResponse.class);

        assertEquals(1, response.statuses.length);
        final MediaStatus mediaStatus = response.statuses[0];
        assertNull(mediaStatus.currentItemId);
        assertEquals(16d, mediaStatus.currentTime, 0.1);
        assertEquals(7, mediaStatus.mediaSessionId);
        assertEquals(1, mediaStatus.playbackRate);
        assertEquals(PlayerState.PLAYING, mediaStatus.playerState);
        assertNull(mediaStatus.customData);
        assertNull(mediaStatus.items);
        assertNull(mediaStatus.preloadedItemId);

        assertEquals(new Volume(0.6999999f, false, 0.05f, null, null), mediaStatus.volume);

        assertNotNull(mediaStatus.media);
        Media media = mediaStatus.media;
        assertEquals(7, media.metadata.size());
        assertEquals(Media.MetadataType.MUSIC_TRACK, media.getMetadataType());
        assertEquals("http://audioURL", media.url);
        assertEquals(246d, media.duration, 0.1);
        assertEquals(Media.StreamType.BUFFERED, media.streamType);
        assertEquals("BUFFERED", media.contentType);
        assertNull(media.textTrackStyle);
        assertNull(media.tracks);
        assertEquals(1, media.customData.size());
        assertNotNull(media.customData.get("status"));
        Map<String, Object> status = (Map<String, Object>) media.customData.get("status");

        assertEquals(8, status.size());
        assertEquals(2, status.get("state"));
    }

    @Test
    public void testDeserializationNoMetadataType() throws IOException {
        final StandardResponse.MediaStatus response =
                (StandardResponse.MediaStatus) jsonMapper.readValue(getClass()
                        .getResourceAsStream("/mediaStatus-no-metadataType.json"), StandardResponse.class);

        final MediaStatus mediaStatus = response.statuses[0];
        Media media = mediaStatus.media;
        assertEquals(Media.MetadataType.GENERIC, media.getMetadataType());
    }

    @Test
    public void testDeserializationUnknownMetadataType() throws IOException {
        final StandardResponse.MediaStatus response =
                (StandardResponse.MediaStatus) jsonMapper.readValue(getClass()
                        .getResourceAsStream("/mediaStatus-unknown-metadataType.json"), StandardResponse.class);

        final MediaStatus mediaStatus = response.statuses[0];
        Media media = mediaStatus.media;
        assertEquals(Media.MetadataType.GENERIC, media.getMetadataType());
    }

    @Test
    public void testDeserializationWithVideoInfo() throws IOException {
        final String jsonMSG = FixtureHelper.fixtureAsString("/mediaStatus-with-videoinfo.json")
                .replaceFirst("\"type\"", "\"responseType\"");
        final StandardResponse.MediaStatus response =
                (StandardResponse.MediaStatus) jsonMapper.readValue(jsonMSG, StandardResponse.class);
        assertEquals(1, response.statuses.length);
    }

    @Test
    public void testDeserializationAudioWithExtraStatus() throws IOException {
        final String jsonMSG = FixtureHelper.fixtureAsString("/mediaStatus-audio-with-extraStatus.json")
                .replaceFirst("\"type\"", "\"responseType\"");
        final StandardResponse.MediaStatus response =
                (StandardResponse.MediaStatus) jsonMapper.readValue(jsonMSG, StandardResponse.class);
        assertEquals(1, response.statuses.length);
    }
}
