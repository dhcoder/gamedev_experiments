package dhcoder.libgdx.pool;

import com.badlogic.gdx.math.Vector3;
import dhcoder.support.memory.Pool;

/**
 * Convenience class that builds a pool of Vector3's.
 */
public final class Vector3PoolBuilder {

    public static Pool<Vector3> build(final int capacity) {
        return new Pool<Vector3>(new Pool.AllocateMethod<Vector3>() {
            @Override
            public Vector3 run() {
                return new Vector3();
            }
        }, new Pool.ResetMethod<Vector3>() {
            @Override
            public void run(final Vector3 item) {
                item.setZero();
            }
        }, capacity);
    }

    public static Pool<Vector3> build() {
        return build(Pool.DEFAULT_CAPACITY);
    }

    private Vector3PoolBuilder() {}
}
