package dhcoder.libgdx.pool.box2d;

import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import dhcoder.support.memory.Pool;

/**
 * Convenience class that builds a pool of {@link BodyDef}'s.
 */
public final class BodyDefBuilder {

    public static Pool<BodyDef> build(final int capacity) {
        return new Pool<BodyDef>(new Pool.AllocateMethod<BodyDef>() {
            @Override
            public BodyDef run() {
                return new BodyDef();
            }
        }, new Pool.ResetMethod<BodyDef>() {
            @Override
            public void run(final BodyDef bodyDef) {
                bodyDef.type = BodyType.StaticBody;
                bodyDef.position.setZero();
                bodyDef.angle = 0f;
                bodyDef.linearVelocity.setZero();
                bodyDef.angularVelocity = 0f;
                bodyDef.linearDamping = 0f;
                bodyDef.angularDamping = 0f;
                bodyDef.allowSleep = true;
                bodyDef.fixedRotation = false;
                bodyDef.bullet = false;
                bodyDef.active = true;
                bodyDef.gravityScale = 1f;
            }
        }, capacity);
    }

    public static Pool<BodyDef> build() {
        return build(Pool.DEFAULT_CAPACITY);
    }

    private BodyDefBuilder() {}
}
