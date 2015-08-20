package dhcoder.sandbox;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import dhcoder.libgdx.physics.PhysicsSystem;
import dhcoder.support.annotations.NotNull;
import dhcoder.support.annotations.Nullable;
import dhcoder.support.memory.Poolable;

/**
 * Grab from a pool and then {@link #initialize(PhysicsSystem, float, float, float, float, float, float)} it.
 */
public final class Bullet implements Poolable {
    @Nullable private PhysicsSystem mySystem;
    @Nullable private Body myBody;

    @SuppressWarnings("unused") // Used by pool
    Bullet() {}

    public void initialize(PhysicsSystem system, float x, float y, float r, float toX, float toY, float speed) {
        mySystem = system;

        CircleShape circle = new CircleShape();
        circle.setRadius(r);
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = circle;
        fixtureDef.density = 1f;
        fixtureDef.filter.categoryBits = Layers.BULLET;
        fixtureDef.filter.maskBits = Layers.PLAYER;
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.bullet = true;
        bodyDef.position.set(x, y);
        bodyDef.linearVelocity.set(toX, toY).sub(x, y).nor().scl(speed);
        myBody = system.getWorld().createBody(bodyDef);
        myBody.createFixture(fixtureDef);
        myBody.setUserData(this);
        circle.dispose();
    }

    @Override
    public void reset() {
        assert mySystem != null;
        assert myBody != null;
        mySystem.destroyBody(myBody);
        mySystem = null;
        myBody = null;
    }

    @NotNull
    public Body getBody() {
        assert myBody != null;
        return myBody;
    }

    public Vector2 getPosition() {
        assert myBody != null;
        return myBody.getWorldCenter();
    }
}
