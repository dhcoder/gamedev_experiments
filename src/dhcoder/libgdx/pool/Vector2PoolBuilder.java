package dhcoder.libgdx.pool;

import com.badlogic.gdx.math.Vector2;
import dhcoder.support.memory.Pool;

/**
 * Convenience class that builds a pool of Vector2's.
 */
public final class Vector2PoolBuilder {

    public static Pool<Vector2> build(final int capacity) {
        return new Pool<Vector2>(new Pool.AllocateMethod<Vector2>() {
            @Override
            public Vector2 run() {
                return new Vector2();
            }
        }, new Pool.ResetMethod<Vector2>() {
            @Override
            public void run(final Vector2 item) {
                item.setZero();
            }
        }, capacity);
    }

    public static Pool<Vector2> build() {
        return build(Pool.DEFAULT_CAPACITY);
    }

    private Vector2PoolBuilder() {}
}
