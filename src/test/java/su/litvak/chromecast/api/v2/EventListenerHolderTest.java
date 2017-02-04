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

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Before;
import org.junit.Test;

import su.litvak.chromecast.api.v2.ChromeCastSpontaneousEvent.SpontaneousEventType;

public class EventListenerHolderTest {
    private final ObjectMapper jsonMapper = JacksonHelper.createJSONMapper();
    private List<ChromeCastSpontaneousEvent> emittedEvents;
    private EventListenerHolder underTest;

    private static class CustomAppEvent {
        @JsonProperty
        public String responseType;
        @JsonProperty
        public long requestId;
        @JsonProperty
        public String event;

        @SuppressWarnings("unused")
        CustomAppEvent() {
        }

        CustomAppEvent(String responseType, long requestId, String event) {
            this.responseType = responseType;
            this.requestId = requestId;
            this.event = event;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((event == null) ? 0 : event.hashCode());
            result = prime * result + (int) (requestId ^ (requestId >>> 32));
            result = prime * result
                    + ((responseType == null) ? 0 : responseType.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            CustomAppEvent other = (CustomAppEvent) obj;
            if (event == null) {
                if (other.event != null)
                    return false;
            } else if (!event.equals(other.event))
                return false;
            if (requestId != other.requestId)
                return false;
            if (responseType == null) {
                if (other.responseType != null)
                    return false;
            } else if (!responseType.equals(other.responseType))
                return false;
            return true;
        }
    }

    @Before
    public void before () throws Exception {
        this.emittedEvents = new ArrayList<ChromeCastSpontaneousEvent>();
        this.underTest = new EventListenerHolder();
        this.underTest.registerListener(new ChromeCastSpontaneousEventListener() {
            @Override
            public void spontaneousEventReceived (ChromeCastSpontaneousEvent event) {
                emittedEvents.add(event);
            }
        });
    }

    @Test
    public void itHandlesMediaStatusEvent () throws Exception {
        final String json = FixtureHelper.fixtureAsString("/mediaStatus-chromecast-audio.json").replaceFirst("\"type\"", "\"responseType\"");
        this.underTest.deliverEvent(jsonMapper.readTree(json));

        ChromeCastSpontaneousEvent event = emittedEvents.get(0);

        assertEquals(SpontaneousEventType.MEDIA_STATUS, event.getType());
        // Is it roughly what we passed in?  More throughly tested in MediaStatusTest.
        assertEquals(15, event.getData(MediaStatus.class).supportedMediaCommands);

        assertEquals(1, emittedEvents.size());
    }

    @Test
    public void itHandlesStatusEvent () throws Exception {
        Volume volume = new Volume(123f, false, 2f, Volume.DEFAULT_INCREMENT.doubleValue(),
                Volume.DEFAULT_CONTROL_TYPE);
        StandardResponse.Status status = new StandardResponse.Status(new Status(volume, null, false, false));
        this.underTest.deliverEvent(jsonMapper.valueToTree(status));

        ChromeCastSpontaneousEvent event = emittedEvents.get(0);

        assertEquals(SpontaneousEventType.STATUS, event.getType());
        // Not trying to test everything, just that is basically what we passed in.
        assertEquals(volume, event.getData(Status.class).volume);

        assertEquals(1, emittedEvents.size());
    }

    @Test
    public void itHandlesPlainAppEvent () throws Exception {
        final String NAMESPACE = "urn:x-cast:com.example.app";
        final String MESSAGE = "Sample message";
        AppEvent appevent = new AppEvent(NAMESPACE, MESSAGE);
        this.underTest.deliverAppEvent(appevent);

        ChromeCastSpontaneousEvent event = emittedEvents.get(0);

        assertEquals(SpontaneousEventType.APPEVENT, event.getType());
        assertEquals(NAMESPACE, event.getData(AppEvent.class).namespace);
        assertEquals(MESSAGE, event.getData(AppEvent.class).message);

        assertEquals(1, emittedEvents.size());
    }

    @Test
    public void itHandlesJsonAppEvent () throws Exception {
        final String NAMESPACE = "urn:x-cast:com.example.app";
        CustomAppEvent customAppEvent = new CustomAppEvent("MYEVENT", 3, "Sample message");
        final String MESSAGE = jsonMapper.writeValueAsString(customAppEvent);
        AppEvent appevent = new AppEvent(NAMESPACE, MESSAGE);
        this.underTest.deliverAppEvent(appevent);

        ChromeCastSpontaneousEvent event = emittedEvents.get(0);

        assertEquals(SpontaneousEventType.APPEVENT, event.getType());
        assertEquals(NAMESPACE, event.getData(AppEvent.class).namespace);

        // Check whether we received the same object
        CustomAppEvent responseEvent = jsonMapper.readValue(
                event.getData(AppEvent.class).message, CustomAppEvent.class);
        assertEquals(customAppEvent, responseEvent);

        assertEquals(1, emittedEvents.size());
    }

}
