package dhcoder.libgdx.entity;

import dhcoder.support.memory.Poolable;
import dhcoder.support.time.Duration;

/**
 * The base class for all components, where a component is simply an isolated unit of logic. An {@link Entity} is
 * defined by what components drive it.
 */
public interface Component extends Poolable {
    /**
     * Initializes this component, called after the parent {@link Entity} is created.
     *
     * @param owner The entity that owns this component. May be useful to use this to fetch other components this
     *              component relies on.
     */
    void initialize(Entity owner);

    /**
     * Updates this component. The passed in time is in seconds.
     */
    void update(Duration elapsedTime);
}
