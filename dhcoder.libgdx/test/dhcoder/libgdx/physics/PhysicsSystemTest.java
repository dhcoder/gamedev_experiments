package dhcoder.libgdx.physics;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import dhcoder.support.time.Duration;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static com.google.common.truth.Truth.assertThat;

public final class PhysicsSystemTest {

    private final int BALL_LAYER = 0x1;
    private final int GROUND_LAYER = 0x2;

    // A physics system is set up with (0,-10) gravity with the ball above the ground
    // set to fall onto it
    PhysicsSystem myPhysics;
    Body myBall;
    Body myGround;

    @Before
    public void setUp() throws Exception {
        myPhysics = new PhysicsSystem(50, new Vector2(0f, -10f));

        BodyDef bodyDef = new BodyDef();
        {
            CircleShape circle = new CircleShape();
            circle.setRadius(1f);

            FixtureDef ballFixture = new FixtureDef();
            ballFixture.shape = circle;
            ballFixture.density = 1f;
            ballFixture.restitution = 1f;
            ballFixture.filter.categoryBits = BALL_LAYER;
            ballFixture.filter.maskBits = GROUND_LAYER;

            bodyDef.type = BodyDef.BodyType.DynamicBody;
            bodyDef.position.set(0f, 2.01f);
            myBall = myPhysics.getWorld().createBody(bodyDef);
            myBall.createFixture(ballFixture);

            circle.dispose();
        }

        {
            bodyDef.type = BodyDef.BodyType.StaticBody;
            bodyDef.position.set(0f, 0f);
            PolygonShape polygon = new PolygonShape();
            polygon.setAsBox(50f, 1f);

            FixtureDef groundFixture = new FixtureDef();
            groundFixture.shape = polygon;
            groundFixture.density = 1f;
            groundFixture.restitution = 1f;
            groundFixture.filter.categoryBits = GROUND_LAYER;
            groundFixture.filter.maskBits = BALL_LAYER;

            bodyDef.type = BodyDef.BodyType.StaticBody;
            bodyDef.position.set(0f, 0f);
            myGround = myPhysics.getWorld().createBody(bodyDef);
            myGround.createFixture(groundFixture);
        }
    }

    @After
    public void tearDown() throws Exception {
        myPhysics.destroyBody(myBall);
        myPhysics.destroyBody(myGround);
        myPhysics.dispose();
    }

    @Test
    public void testSimpleCollision() {
        TestCollisionHandler collisionHandler = new TestCollisionHandler();
        myPhysics.addCollisionHandler(BALL_LAYER, GROUND_LAYER, collisionHandler);
        Duration elapsedTime = Duration.fromMilliseconds(16f);

        myPhysics.update(elapsedTime);
        assertThat(collisionHandler.hasCollided()).isTrue();
        assertThat(collisionHandler.hasOverlapped()).isFalse();
        assertThat(collisionHandler.hasSeparated()).isFalse();

        myPhysics.update(elapsedTime);
        assertThat(collisionHandler.hasOverlapped()).isTrue();
        assertThat(collisionHandler.hasSeparated()).isFalse();
    }

    @Test
    public void testPassThru() {
        TestCollisionHandler collisionHandler = new TestCollisionHandler();
        myPhysics.addCollisionHandler(BALL_LAYER, GROUND_LAYER, collisionHandler);
        Duration elapsedTime = Duration.fromMilliseconds(16f);
        // One seconds worth of 60fps, ball should fall ~ 10m
        for (int i = 0; i < 60; i++) {
            myPhysics.update(elapsedTime);
        }

        assertThat(collisionHandler.hasCollided()).isTrue();
        assertThat(collisionHandler.hasOverlapped()).isTrue();
        assertThat(collisionHandler.hasSeparated()).isTrue();
    }

    @Test
    public void deactivatingBodyTriggersSeparationCallback() {
        TestCollisionHandler collisionHandler = new TestCollisionHandler();
        myPhysics.addCollisionHandler(BALL_LAYER, GROUND_LAYER, collisionHandler);
        Duration elapsedTime = Duration.fromMilliseconds(16f);

        myPhysics.update(elapsedTime);
        assertThat(collisionHandler.hasCollided()).isTrue();
        assertThat(collisionHandler.hasSeparated()).isFalse();

        myPhysics.setActive(myBall, false);
        myPhysics.update(elapsedTime);
        assertThat(collisionHandler.hasSeparated()).isTrue();
    }

    @Test
    public void listenerIsCalledOnUpdate() {
        TestListener listener = new TestListener();
        assertThat(listener.getUpdatedCount()).isEqualTo(0);
        myPhysics.addListener(listener);

        Duration elapsedTime = Duration.fromMilliseconds(16f);

        myPhysics.update(elapsedTime);
        assertThat(listener.getUpdatedCount()).isEqualTo(1);

        myPhysics.update(elapsedTime);
        assertThat(listener.getUpdatedCount()).isEqualTo(2);

        myPhysics.removeListener(listener);
        myPhysics.update(elapsedTime);
        assertThat(listener.getUpdatedCount()).isEqualTo(2);

    }

    private static final class TestCollisionHandler extends AbstractCollisionHandler {
        private boolean hasCollided;
        private boolean hasOverlapped;
        private boolean hasSeparated;

        public boolean hasCollided() {
            return hasCollided;
        }

        public boolean hasOverlapped() {
            return hasOverlapped;
        }

        public boolean hasSeparated() {
            return hasSeparated;
        }

        @Override
        public void onCollided(Body bodyA, Body bodyB) {
            hasCollided = true;
        }

        @Override
        public void onOverlapping(Body bodyA, Body bodyB) {
            hasOverlapped = true;
        }

        @Override
        public void onSeparated(Body bodyA, Body bodyB) {
            hasSeparated = true;
        }
    }

    private static final class TestListener implements PhysicsListener {

        private int updatedCount;

        public int getUpdatedCount() {
            return updatedCount;
        }

        @Override
        public void onUpdated() {
            updatedCount++;
        }
    }
}