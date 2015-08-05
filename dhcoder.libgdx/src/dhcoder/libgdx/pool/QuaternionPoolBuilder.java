package dhcoder.libgdx.pool;

import com.badlogic.gdx.math.Quaternion;
import dhcoder.support.memory.Pool;

/**
 * Convenience class that builds a pool of Quaternion's.
 */
public final class QuaternionPoolBuilder {

    public static Pool<Quaternion> build(int capacity) {
        return new Pool<Quaternion>(new Pool.AllocateMethod<Quaternion>() {
            @Override
            public Quaternion run() {
                return new Quaternion();
            }
        }, new Pool.ResetMethod<Quaternion>() {
            @Override
            public void run(Quaternion item) {
                item.idt();
            }
        }, capacity);
    }

    public static Pool<Quaternion> build() {
        return build(Pool.DEFAULT_CAPACITY);
    }

    private QuaternionPoolBuilder() {}
}
