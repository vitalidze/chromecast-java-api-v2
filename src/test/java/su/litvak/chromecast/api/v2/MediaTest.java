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
import su.litvak.chromecast.api.v2.Media.StreamType;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.StringContains.containsString;

public class MediaTest {
    final ObjectMapper jsonMapper = JacksonHelper.createJSONMapper();

    @Test
    public void itIncludesOptionalFieldsWhenSet() throws Exception {
        Map<String, Object> customData = new HashMap<String, Object>();
        customData.put("a", "b");
        Map<String, Object> metadata = new HashMap<String, Object>();
        metadata.put("1", "2");
        Media m = new Media(null, null, 123.456d, StreamType.BUFFERED, customData, metadata, null, null);

        String json = jsonMapper.writeValueAsString(m);

        assertThat(json, containsString("\"duration\":123.456"));
        assertThat(json, containsString("\"streamType\":\"BUFFERED\""));
        assertThat(json, containsString("\"customData\":{\"a\":\"b\"}"));
        assertThat(json, containsString("\"metadata\":{\"1\":\"2\"}"));
    }

    @Test
    public void itDoesNotContainOptionalFieldsWhenNotSet() throws Exception {
        Media m = new Media(null, null, null, null, null, null, null, null);

        String json = jsonMapper.writeValueAsString(m);

        assertThat(json, not(containsString("duration")));
        assertThat(json, not(containsString("streamType")));
        assertThat(json, not(containsString("customData")));
        assertThat(json, not(containsString("metadata")));
        assertThat(json, not(containsString("metadataType")));
    }

}
