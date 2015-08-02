package dhcoder.support.event;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

public final class ArgEventTest {

    private final class IntArgs implements EventArgs {

        private final int intValue;

        private IntArgs(final int intValue) {
            this.intValue = intValue;
        }

        public int getIntValue() {
            return intValue;
        }
    }

    private final class IntEventOwner {

        private final ArgEvent<IntArgs> intEvent = new ArgEvent<IntArgs>();

        public ArgEvent<IntArgs> getIntEvent() {
            return intEvent;
        }

        public void testFire(final int intValue) {
            intEvent.fire(this, new IntArgs(intValue));
        }

        public void testClear() { intEvent.clearListeners(); }
    }

    private final class IntEventListener implements ArgEventListener<IntArgs> {

        private Object sender;
        private IntArgs args;

        public Object getSender() {
            return sender;
        }

        public IntArgs getArgs() {
            return args;
        }

        @Override
        public void run(final Object sender, final IntArgs args) {
            this.sender = sender;
            this.args = args;
        }
    }

    @Test
    public void testFiringAnEventWithArgs() {
        IntEventOwner intEventOwner = new IntEventOwner();
        IntEventListener intEventListener = new IntEventListener();

        intEventOwner.getIntEvent().addListener(intEventListener);

        final int ARBITRARY_VALUE = 97531;
        intEventOwner.testFire(ARBITRARY_VALUE);

        assertThat(intEventListener.getSender(), equalTo((Object)intEventOwner));
        assertThat(intEventListener.getArgs().getIntValue(), equalTo(ARBITRARY_VALUE));
    }

    @Test
    public void firingAnEventTriggersAllEventListeners() {
        IntEventOwner intEventOwner = new IntEventOwner();

        IntEventListener intEventListener1 = new IntEventListener();
        IntEventListener intEventListener2 = new IntEventListener();

        intEventOwner.getIntEvent().addListener(intEventListener1);
        intEventOwner.getIntEvent().addListener(intEventListener2);

        final int ARBITRARY_VALUE = 13579;
        intEventOwner.testFire(ARBITRARY_VALUE);

        assertThat(intEventListener1.getArgs().getIntValue(), equalTo(ARBITRARY_VALUE));
        assertThat(intEventListener2.getArgs().getIntValue(), equalTo(ARBITRARY_VALUE));
    }

    @Test
    public void removeListenerStopsEventListenerFromBeingTriggered() {
        IntEventOwner intEventOwner = new IntEventOwner();

        IntEventListener intEventListener = new IntEventListener();
        intEventOwner.getIntEvent().addListener(intEventListener);

        intEventOwner.testFire(1);
        assertThat(intEventListener.getArgs().getIntValue(), equalTo(1));

        intEventOwner.getIntEvent().removeListener(intEventListener);
        intEventOwner.testFire(2);
        assertThat(intEventListener.getArgs().getIntValue(), equalTo(1));
    }

    @Test
    public void clearListenersStopsListenerFromBeingTriggered() {
        IntEventOwner intEventOwner = new IntEventOwner();

        IntEventListener intEventListener = new IntEventListener();
        intEventOwner.getIntEvent().addListener(intEventListener);

        intEventOwner.testFire(1);
        assertThat(intEventListener.getArgs().getIntValue(), equalTo(1));

        intEventOwner.testClear();
        intEventOwner.testFire(2);
        assertThat(intEventListener.getArgs().getIntValue(), equalTo(1));
    }
}