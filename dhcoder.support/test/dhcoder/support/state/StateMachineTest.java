package dhcoder.support.state;

import dhcoder.support.annotations.Nullable;
import org.junit.Before;
import org.junit.Test;

import static com.google.common.truth.Truth.assertThat;
import static dhcoder.test.TestUtils.assertException;

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
        public void run(TestState fromState, TestEvent withEvent, @Nullable Object eventData) {
            ranCount++;
        }

        public int getRanCount() {
            return ranCount;
        }
    }

    private final class TestMachine extends StateMachine<TestState, TestEvent> {

        public TestMachine(TestState startState) {
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
            public TestState run(TestState fromState, TestEvent withEvent, @Nullable Object eventData) {
                return TestState.B;
            }
        });

        fsm.registerEvent(TestState.A, TestEvent.A_TO_C, new StateTransitionHandler<TestState, TestEvent>() {
            @Override
            public TestState run(TestState fromState, TestEvent withEvent, @Nullable Object eventData) {
                return TestState.C;
            }
        });

        fsm.registerEvent(TestState.B, TestEvent.B_TO_C, new StateTransitionHandler<TestState, TestEvent>() {
            @Override
            public TestState run(TestState fromState, TestEvent withEvent, @Nullable Object eventData) {
                return TestState.C;
            }
        });

        fsm.registerEvent(TestState.B, TestEvent.ANY_TO_A, new StateTransitionHandler<TestState, TestEvent>() {
            @Override
            public TestState run(TestState fromState, TestEvent withEvent, @Nullable Object eventData) {
                return TestState.A;
            }
        });

        fsm.registerEvent(TestState.C, TestEvent.ANY_TO_A, new StateTransitionHandler<TestState, TestEvent>() {
            @Override
            public TestState run(TestState fromState, TestEvent withEvent, @Nullable Object eventData) {
                return TestState.A;
            }
        });

        fsm.setDefaultHandler(defaultHandler);
    }

    @Test
    public void stateMachineStartsInStateSetInConstructor() {
        TestMachine fsmA = new TestMachine(TestState.A);
        TestMachine fsmC = new TestMachine(TestState.C);

        assertThat(fsmA.getCurrentState()).isEqualTo(TestState.A);
        assertThat(fsmC.getCurrentState()).isEqualTo(TestState.C);
    }

    @Test
    public void testStateMachineChangesStateAsExpected() {
        assertThat(fsm.getCurrentState()).isEqualTo(TestState.A);
        fsm.handleEvent(TestEvent.A_TO_B);
        assertThat(fsm.getCurrentState()).isEqualTo(TestState.B);
        fsm.handleEvent(TestEvent.B_TO_C);
        assertThat(fsm.getCurrentState()).isEqualTo(TestState.C);
        fsm.handleEvent(TestEvent.ANY_TO_A);
        assertThat(fsm.getCurrentState()).isEqualTo(TestState.A);
        fsm.handleEvent(TestEvent.A_TO_B);
        assertThat(fsm.getCurrentState()).isEqualTo(TestState.B);
        fsm.handleEvent(TestEvent.B_TO_C);
        assertThat(fsm.getCurrentState()).isEqualTo(TestState.C);
    }

    @Test
    public void defaultHandlerCatchesUnregisteredEvent() {

        assertThat(defaultHandler.getRanCount()).isEqualTo(0);

        fsm.handleEvent(TestEvent.A_TO_B);
        assertThat(defaultHandler.getRanCount()).isEqualTo(0);

        fsm.handleEvent(TestEvent.UNREGISTERED_EVENT);
        assertThat(defaultHandler.getRanCount()).isEqualTo(1);

        assertThat(fsm.getCurrentState()).isEqualTo(TestState.B);
        fsm.handleEvent(TestEvent.A_TO_B);
        assertThat(defaultHandler.getRanCount()).isEqualTo(2);
    }

    @Test
    public void duplicateRegistrationThrowsException() {

        assertException("Duplicate event registration is not allowed", IllegalArgumentException.class, new Runnable() {
            @Override
            public void run() {
                fsm.registerEvent(TestState.A, TestEvent.A_TO_B, new StateTransitionHandler<TestState, TestEvent>() {
                    @Override
                    public TestState run(TestState fromState, TestEvent withEvent, @Nullable Object eventData) {
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
            public TestState run(TestState fromState, TestEvent withEvent, @Nullable Object eventData) {
                assertThat(eventData).isNotNull();
                assertThat(eventData).isEqualTo(dummyData);

                ran = true;
                return fromState;
            }

        }

        DummyDataHandler handler = new DummyDataHandler();
        fsm.registerEvent(TestState.A, TestEvent.EVENT_WITH_DATA, handler);

        assertThat(handler.wasRun()).isEqualTo(false);
        fsm.handleEvent(TestEvent.EVENT_WITH_DATA, dummyData);
        assertThat(handler.wasRun()).isEqualTo(true);
    }

}