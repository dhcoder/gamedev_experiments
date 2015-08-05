package dhcoder.support.state;

import dhcoder.support.annotations.Nullable;

/**
 * A method which handles a state machine's event transition, returning which state the machine should transition into.
 *
 * @param <S> An enumeration type that represents the known states this machine can get into.
 * @param <E> An enumeration type that represents the known events this machine can accept.
 */
public interface StateTransitionHandler<S extends Enum, E extends Enum> {
    S run(S fromState, E withEvent, @Nullable Object eventData);
}
