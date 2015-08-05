package dhcoder.support.state;

import dhcoder.support.annotations.NotNull;
import dhcoder.support.annotations.Nullable;
import dhcoder.support.collection.Key2;
import dhcoder.support.memory.Pool;

import java.util.HashMap;
import java.util.Map;

import static dhcoder.support.text.StringUtils.format;

/**
 * Encapsulation of a finite state machine.
 * <p/>
 * You instantiate a state machine by registering a list of states and a list of events that it can accept in each
 * state.
 *
 * @param <S> An enumeration type that represents the known states this machine can get into.
 * @param <E> An enumeration type that represents the known events this machine can accept.
 */
public class StateMachine<S extends Enum, E extends Enum> {

    private final Map<Key2<S, E>, StateTransitionHandler<S, E>> eventResponses =
        new HashMap<Key2<S, E>, StateTransitionHandler<S, E>>();
    private final Pool<Key2> keyPool = Pool.of(Key2.class, 1);
    @Nullable private StateEventHandler<S, E> defaultHandler;
    private S startState;
    private S currentState;

    public StateMachine(final S startState) {
        this.startState = currentState = startState;
    }

    public final S getCurrentState() {
        return currentState;
    }

    /**
     * Reset this state machine back to its initial state.
     */
    public void reset() {
        currentState = startState;
    }

    /**
     * Set a method handler which, if set, will get called any time an event is called on the state machine that isn't
     * handled.
     */
    public final void setDefaultHandler(@NotNull StateEventHandler<S, E> defaultHandler) {
        this.defaultHandler = defaultHandler;
    }

    /**
     * Register a state/event pair with a handler that will get triggered if the event happens when the state is active.
     * <p/>
     * It is an error to register more than one handler for any state/event pair.
     *
     * @throws IllegalArgumentException if the state/event pair has previously been registered.
     */
    public final void registerEvent(S state, E event, StateTransitionHandler<S, E> eventHandler) {
        Key2<S, E> key = new Key2<S, E>(state, event);
        if (eventResponses.containsKey(key)) {
            throw new IllegalArgumentException(
                format("Duplicate registration of state+event pair: {0}, {1}.", state, event));
        }

        eventResponses.put(key, eventHandler);
    }

    /**
     * Tell the state machine to handle the passed in event given the current state.
     */
    public final void handleEvent(E event) {
        handleEvent(event, null);
    }

    /**
     * Like {@link #handleEvent(Enum)} but with some additional data that is related to the event.
     */
    public void handleEvent(E event, @Nullable Object eventData) {
        Key2 key = keyPool.grabNew();
        key.set(currentState, event);
        if (!eventResponses.containsKey(key)) {
            keyPool.free(key);
            if (defaultHandler != null) {
                defaultHandler.run(currentState, event, eventData);
            }
            return;
        }

        StateTransitionHandler<S, E> eventHandler = eventResponses.get(key);
        keyPool.free(key);

        currentState = eventHandler.run(currentState, event, eventData);
    }
}

