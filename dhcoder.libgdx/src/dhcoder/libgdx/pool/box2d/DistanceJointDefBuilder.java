package dhcoder.libgdx.pool.box2d;

import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.JointDef.JointType;
import com.badlogic.gdx.physics.box2d.joints.DistanceJointDef;
import dhcoder.support.memory.Pool;

/**
 * Convenience class that builds a pool of {@link FixtureDef}'s.
 */
public final class DistanceJointDefBuilder {

    public static Pool<DistanceJointDef> build(final int capacity) {
        return new Pool<DistanceJointDef>(new Pool.AllocateMethod<DistanceJointDef>() {
            @Override
            public DistanceJointDef run() {
                return new DistanceJointDef();
            }
        }, new Pool.ResetMethod<DistanceJointDef>() {
            @Override
            public void run(final DistanceJointDef distanceJointDef) {
                distanceJointDef.type = JointType.Unknown;
                distanceJointDef.collideConnected = false;
                distanceJointDef.bodyA = null;
                distanceJointDef.bodyB = null;
                distanceJointDef.localAnchorA.setZero();
                distanceJointDef.localAnchorB.setZero();
                distanceJointDef.length = 1;
                distanceJointDef.frequencyHz = 0;
                distanceJointDef.dampingRatio = 0;
            }
        }, capacity);
    }

    public static Pool<DistanceJointDef> build() {
        return build(Pool.DEFAULT_CAPACITY);
    }

    private DistanceJointDefBuilder() {}
}
