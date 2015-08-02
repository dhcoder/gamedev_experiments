package dhcoder.support.state;

import dhcoder.support.opt.Opt;
import org.junit.Before;
import org.junit.Test;

import static dhcoder.test.TestUtils.assertException;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

public final class StateMachineTest {

    private enum TestState {
        A,
        B,
        C
    }

    private enum TestEvent {
        A_TO_B,
        A_TO_C,
        B_TO_C,
        ANY_TO_A,
        UNREGISTERED_EVENT,
        EVENT_WITH_DATA,
    }

    class DefaultHandler implements StateEventHandler<TestState, TestEvent> {

        private int ranCount;

        @Override
        public void run(final TestState fromState, final TestEvent withEvent, final Opt eventData) {
            ranCount++;
        }

        public int getRanCount() {
            return ranCount;
        }
    }

    private final class TestMachine extends StateMachine<TestState, TestEvent> {

        public TestMachine(final TestState startState) {
            super(startState);
        }
    }

    private TestMachine fsm;
    private DefaultHandler defaultHandler = new DefaultHandler();

    @Before
    public void setUp() throws Exception {
        fsm = new TestMachine(TestState.A);

        fsm.registerEvent(TestState.A, TestEvent.A_TO_B, new StateTransitionHandler<TestState, TestEvent>() {
            @Override
            public TestState run(final TestState fromState, final TestEvent withEvent, final Opt eventData) {
                return TestState.B;
            }
        });

        fsm.registerEvent(TestState.A, TestEvent.A_TO_C, new StateTransitionHandler<TestState, TestEvent>() {
            @Override
            public TestState run(final TestState fromState, final TestEvent withEvent, final Opt eventData) {
                return TestState.C;
            }
        });

        fsm.registerEvent(TestState.B, TestEvent.B_TO_C, new StateTransitionHandler<TestState, TestEvent>() {
            @Override
            public TestState run(final TestState fromState, final TestEvent withEvent, final Opt eventData) {
                return TestState.C;
            }
        });

        fsm.registerEvent(TestState.B, TestEvent.ANY_TO_A, new StateTransitionHandler<TestState, TestEvent>() {
            @Override
            public TestState run(final TestState fromState, final TestEvent withEvent, final Opt eventData) {
                return TestState.A;
            }
        });

        fsm.registerEvent(TestState.C, TestEvent.ANY_TO_A, new StateTransitionHandler<TestState, TestEvent>() {
            @Override
            public TestState run(final TestState fromState, final TestEvent withEvent, final Opt eventData) {
                return TestState.A;
            }
        });

        fsm.setDefaultHandler(defaultHandler);
    }

    @Test
    public void stateMachineStartsInStateSetInConstructor() {
        TestMachine fsmA = new TestMachine(TestState.A);
        TestMachine fsmC = new TestMachine(TestState.C);

        assertThat(fsmA.getCurrentState(), equalTo(TestState.A));
        assertThat(fsmC.getCurrentState(), equalTo(TestState.C));
    }

    @Test
    public void testStateMachineChangesStateAsExpected() {
        assertThat(fsm.getCurrentState(), equalTo(TestState.A));
        fsm.handleEvent(TestEvent.A_TO_B);
        assertThat(fsm.getCurrentState(), equalTo(TestState.B));
        fsm.handleEvent(TestEvent.B_TO_C);
        assertThat(fsm.getCurrentState(), equalTo(TestState.C));
        fsm.handleEvent(TestEvent.ANY_TO_A);
        assertThat(fsm.getCurrentState(), equalTo(TestState.A));
        fsm.handleEvent(TestEvent.A_TO_B);
        assertThat(fsm.getCurrentState(), equalTo(TestState.B));
        fsm.handleEvent(TestEvent.B_TO_C);
        assertThat(fsm.getCurrentState(), equalTo(TestState.C));
    }

    @Test
    public void defaultHandlerCatchesUnregisteredEvent() {

        assertThat(defaultHandler.getRanCount(), equalTo(0));

        fsm.handleEvent(TestEvent.A_TO_B);
        assertThat(defaultHandler.getRanCount(), equalTo(0));

        fsm.handleEvent(TestEvent.UNREGISTERED_EVENT);
        assertThat(defaultHandler.getRanCount(), equalTo(1));

        assertThat(fsm.getCurrentState(), equalTo(TestState.B));
        fsm.handleEvent(TestEvent.A_TO_B);
        assertThat(defaultHandler.getRanCount(), equalTo(2));
    }

    @Test
    public void duplicateRegistrationThrowsException() {

        assertException("Duplicate event registration is not allowed", IllegalArgumentException.class, new Runnable() {
            @Override
            public void run() {
                fsm.registerEvent(TestState.A, TestEvent.A_TO_B, new StateTransitionHandler<TestState, TestEvent>() {
                    @Override
                    public TestState run(final TestState fromState, final TestEvent withEvent, final Opt eventData) {
                        return TestState.B;
                    }
                });
            }
        });
    }

    @Test
    public void eventDataIsPassedOn() {
        final Object dummyData = new Object();
        class DummyDataHandler implements StateTransitionHandler<TestState, TestEvent> {

            private boolean ran;

            public boolean wasRun() {
                return ran;
            }

            @Override
            public TestState run(final TestState fromState, final TestEvent withEvent, final Opt eventData) {
                assertThat(eventData.hasValue(), equalTo(true));
                assertThat(eventData.getValue(), equalTo(dummyData));

                ran = true;
                return fromState;
            }

        }

        DummyDataHandler handler = new DummyDataHandler();
        fsm.registerEvent(TestState.A, TestEvent.EVENT_WITH_DATA, handler);

        assertThat(handler.wasRun(), equalTo(false));
        fsm.handleEvent(TestEvent.EVENT_WITH_DATA, dummyData);
        assertThat(handler.wasRun(), equalTo(true));
    }

}