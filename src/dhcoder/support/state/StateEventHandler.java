package dhcoder.support.state;

import dhcoder.support.opt.Opt;

/**
 * A method like {@link StateTransitionHandler} but doesn't return a state to transition into.
 *
 * @param <S> An enumeration type that represents the known states this machine can get into.
 * @param <E> An enumeration type that represents the known events this machine can accept.
 */
public interface StateEventHandler<S extends Enum, E extends Enum> {
    void run(S fromState, E withEvent, Opt eventData);
}
