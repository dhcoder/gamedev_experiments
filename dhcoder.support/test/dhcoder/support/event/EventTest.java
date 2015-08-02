package dhcoder.support.event;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

public final class EventTest {

    private final class EventOwner {

        private final Event event = new Event();

        public Event getEvent() { return event; }

        public void testFire() {
            event.fire(this);
        }

        public void testClear() {
            event.clearListeners();
        }
    }

    private final class EventFiredCounter implements EventListener {

        private Object sender;
        private int eventFiredCount;

        public Object getSender() {
            return sender;
        }

        public int getCount() {
            return eventFiredCount;
        }

        @Override
        public void run(final Object sender) {
            this.sender = sender;
            eventFiredCount++;
        }
    }

    @Test
    public void firingAnEventTriggersAnEventListener() {
        EventOwner eventOwner = new EventOwner();

        EventFiredCounter eventFiredCounter = new EventFiredCounter();
        eventOwner.getEvent().addListener(eventFiredCounter);

        assertThat(eventFiredCounter.getCount(), equalTo(0));

        eventOwner.testFire();
        assertThat(eventFiredCounter.getCount(), equalTo(1));
    }

    @Test
    public void firingAnEventTriggersAllEventListener() {
        EventOwner eventOwner = new EventOwner();

        EventFiredCounter eventFiredCounter1 = new EventFiredCounter();
        EventFiredCounter eventFiredCounter2 = new EventFiredCounter();

        eventOwner.getEvent().addListener(eventFiredCounter1);
        eventOwner.getEvent().addListener(eventFiredCounter2);

        eventOwner.testFire();
        assertThat(eventFiredCounter1.getCount(), equalTo(1));
        assertThat(eventFiredCounter2.getCount(), equalTo(1));
    }

    @Test
    public void removeListenerStopsEventListenerFromBeingTriggered() {
        EventOwner eventOwner = new EventOwner();
        EventFiredCounter eventFiredCounter = new EventFiredCounter();
        eventOwner.getEvent().addListener(eventFiredCounter);

        eventOwner.testFire();
        assertThat(eventFiredCounter.getCount(), equalTo(1));

        eventOwner.getEvent().removeListener(eventFiredCounter);
        eventOwner.testFire();
        assertThat(eventFiredCounter.getCount(), equalTo(1));
    }

    @Test
    public void clearListenersStopsEventListenerFromBeingTriggered() {
        EventOwner eventOwner = new EventOwner();
        EventFiredCounter eventFiredCounter = new EventFiredCounter();
        eventOwner.getEvent().addListener(eventFiredCounter);

        eventOwner.testFire();
        assertThat(eventFiredCounter.getCount(), equalTo(1));

        eventOwner.testClear();
        eventOwner.testFire();
        assertThat(eventFiredCounter.getCount(), equalTo(1));
    }
}