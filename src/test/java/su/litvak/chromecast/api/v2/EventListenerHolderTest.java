package su.litvak.chromecast.api.v2;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Before;
import org.junit.Test;

import su.litvak.chromecast.api.v2.ChromeCastSpontaneousEvent.SpontaneousEventType;

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

        assertEquals(SpontaneousEventType.MEDIA_STATUS, event.getType());
        // Is it roughly what we passed in?  More throughly tested in MediaStatusTest.
        assertEquals(15, event.getData(MediaStatus.class).supportedMediaCommands);

        assertEquals(1, emittedEvents.size());
    }

    @Test
    public void itHandlesStatusEvent () throws Exception {
        Volume volume = new Volume(123f, false, 2f);
        StandardResponse.Status status = new StandardResponse.Status(new Status(volume, null, false, false));
        this.underTest.deliverEvent(jsonMapper.valueToTree(status));

        ChromeCastSpontaneousEvent event = emittedEvents.get(0);

        assertEquals(SpontaneousEventType.STATUS, event.getType());
        // Not trying to test everything, just that is basically what we passed in.
        assertEquals(volume, event.getData(Status.class).volume);

        assertEquals(1, emittedEvents.size());
    }

}
