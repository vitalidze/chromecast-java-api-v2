package su.litvak.chromecast.api.v2;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Before;
import org.junit.Test;

import su.litvak.chromecast.api.v2.ChromeCastSpontaneousEvent.MediaStatusSpontaneousEvent;
import su.litvak.chromecast.api.v2.ChromeCastSpontaneousEvent.StatusSpontaneousEvent;

public class EventListenerHolderTest {
    private final ObjectMapper jsonMapper = new ObjectMapper();
    private List<ChromeCastSpontaneousEvent> emittedEvents;
    private EventListenerHolder underTest;

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
        assertThat(event, instanceOf(MediaStatusSpontaneousEvent.class));
        MediaStatusSpontaneousEvent mediaStatusEvent = (MediaStatusSpontaneousEvent) event;

        // Is it roughly what we passed in?  More throughly tested in MediaStatusTest.
        assertEquals(15, mediaStatusEvent.getMediaStatus().supportedMediaCommands);

        assertEquals(1, emittedEvents.size());
    }

    @Test
    public void itHandlesStatusEvent () throws Exception {
        Volume volume = new Volume(123f, false, 2f);
        StandardResponse.Status status = new StandardResponse.Status(new Status(volume, null, false, false));
        this.underTest.deliverEvent(jsonMapper.valueToTree(status));

        ChromeCastSpontaneousEvent event = emittedEvents.get(0);
        assertThat(event, instanceOf(StatusSpontaneousEvent.class));
        StatusSpontaneousEvent statusEvent = (StatusSpontaneousEvent) event;

        // Not trying to test everything, just that is basically what we passed in.
        assertEquals(volume, statusEvent.getStatus().volume);

        assertEquals(1, emittedEvents.size());
    }

}
