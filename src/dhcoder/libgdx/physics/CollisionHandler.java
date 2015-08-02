package dhcoder.libgdx.physics;

import com.badlogic.gdx.physics.box2d.Body;

/**
 * Interface for a class that should handle a collision between two bodies.
 *
 * Register your collision handlers via {@link PhysicsSystem#addCollisionHandler(int, int, CollisionHandler)}.
 */
public interface CollisionHandler {
    void onCollided(Body bodyA, Body bodyB);
    void onOverlapping(Body bodyA, Body bodyB);
    void onSeparated(Body bodyA, Body bodyB);
}
