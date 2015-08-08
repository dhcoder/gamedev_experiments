package dhcoder.libgdx.physics;

/**
 * A tag for any class that has information it wants to update after the physics step is complete.
 */
public interface PhysicsListener {
    /**
     * Method called after the physics update step is complete.
     */
    void onUpdated();
}
