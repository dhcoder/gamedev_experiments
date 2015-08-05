package dhcoder.libgdx.pool.box2d;

import com.badlogic.gdx.physics.box2d.FixtureDef;
import dhcoder.support.memory.Pool;

/**
 * Convenience class that builds a pool of {@link FixtureDef}'s.
 */
public final class FixtureDefBuilder {

    public static Pool<FixtureDef> build(int capacity) {
        return new Pool<FixtureDef>(new Pool.AllocateMethod<FixtureDef>() {
            @Override
            public FixtureDef run() {
                return new FixtureDef();
            }
        }, new Pool.ResetMethod<FixtureDef>() {
            @Override
            public void run(FixtureDef fixtureDef) {
                fixtureDef.shape = null;
                fixtureDef.friction = 0.2f;
                fixtureDef.restitution = 0;
                fixtureDef.density = 0;
                fixtureDef.isSensor = false;
                fixtureDef.filter.categoryBits = 0x0001;
                fixtureDef.filter.maskBits = -1;
                fixtureDef.filter.groupIndex = 0;
            }
        }, capacity);
    }

    public static Pool<FixtureDef> build() {
        return build(Pool.DEFAULT_CAPACITY);
    }

    private FixtureDefBuilder() {}
}
