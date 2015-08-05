package dhcoder.libgdx.physics;

import com.badlogic.gdx.physics.box2d.Body;

/**
 * Default implementation of the {@link CollisionHandler} interface.
 */
public abstract class AbstractCollisionHandler implements CollisionHandler {
    @Override
    public void onCollided(Body bodyA, Body bodyB) {}

    @Override
    public void onOverlapping(Body bodyA, Body bodyB) {}

    @Override
    public void onSeparated(Body bodyA, Body bodyB) {}
}
