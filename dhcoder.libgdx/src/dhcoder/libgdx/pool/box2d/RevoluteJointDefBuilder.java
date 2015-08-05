package dhcoder.libgdx.pool.box2d;

import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.JointDef;
import com.badlogic.gdx.physics.box2d.joints.RevoluteJointDef;
import dhcoder.support.memory.Pool;

/**
 * Convenience class that builds a pool of {@link FixtureDef}'s.
 */
public final class RevoluteJointDefBuilder {

    public static Pool<RevoluteJointDef> build(int capacity) {
        return new Pool<RevoluteJointDef>(new Pool.AllocateMethod<RevoluteJointDef>() {
            @Override
            public RevoluteJointDef run() {
                return new RevoluteJointDef();
            }
        }, new Pool.ResetMethod<RevoluteJointDef>() {
            @Override
            public void run(RevoluteJointDef revoluteJointDef) {
                revoluteJointDef.type = JointDef.JointType.Unknown;
                revoluteJointDef.collideConnected = false;
                revoluteJointDef.bodyA = null;
                revoluteJointDef.bodyB = null;
                revoluteJointDef.localAnchorA.setZero();
                revoluteJointDef.localAnchorB.setZero();
                revoluteJointDef.referenceAngle = 0;
                revoluteJointDef.enableLimit = false;
                revoluteJointDef.lowerAngle = 0;
                revoluteJointDef.upperAngle = 0;
                revoluteJointDef.enableMotor = false;
                revoluteJointDef.motorSpeed = 0;
                revoluteJointDef.maxMotorTorque = 0;
            }
        }, capacity);
    }

    public static Pool<RevoluteJointDef> build() {
        return build(Pool.DEFAULT_CAPACITY);
    }

    private RevoluteJointDefBuilder() {}
}
