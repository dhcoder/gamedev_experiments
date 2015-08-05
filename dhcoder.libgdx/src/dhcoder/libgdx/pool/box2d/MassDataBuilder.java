package dhcoder.libgdx.pool.box2d;

import com.badlogic.gdx.physics.box2d.MassData;
import dhcoder.support.memory.Pool;

/**
 * Convenience class that builds a pool of {@link MassData}'s.
 */
public final class MassDataBuilder {

    public static Pool<MassData> build(int capacity) {
        return new Pool<MassData>(new Pool.AllocateMethod<MassData>() {
            @Override
            public MassData run() {
                return new MassData();
            }
        }, new Pool.ResetMethod<MassData>() {
            @Override
            public void run(MassData massData) {
                massData.mass = 0f;
                massData.center.setZero();
                massData.I = 0f;
            }
        }, capacity);
    }

    public static Pool<MassData> build() {
        return build(Pool.DEFAULT_CAPACITY);
    }

    private MassDataBuilder() {}
}
