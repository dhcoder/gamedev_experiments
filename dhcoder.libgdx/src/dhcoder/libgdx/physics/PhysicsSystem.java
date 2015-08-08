package dhcoder.libgdx.physics;

import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.Array;
import dhcoder.support.annotations.NotNull;
import dhcoder.support.annotations.Nullable;
import dhcoder.support.collection.ArraySet;
import dhcoder.support.memory.HeapPool;
import dhcoder.support.memory.Pool;
import dhcoder.support.memory.Poolable;
import dhcoder.support.time.Duration;

import java.util.List;

/**
 * A system that wraps a Box2D {@link World} and provides simpler collision management.
 * <p/>
 * To start, initialize this class and register collision handlers with
 * {@link #addCollisionHandler(int, int, CollisionHandler)}. When you create Box2D bodies via
 * {@link FixtureDef}s, be sure to set up the filters and masks appropriately to match. After that,
 * simply call {@link #update(Duration)} to step the physics simulation forward one step.
 */
public final class PhysicsSystem {

    private static final int EXPECTED_COLLIDER_HANDLER_COUNT = 30;
    private static final int EXPECTED_INACTIVE_BODY_COUNT = 10;
    // Recommended values from Box2D manual
    private static final int VELOCITY_ITERATIONS = 6;
    private static final int POSITION_ITERATIONS = 2;

    static {
        Box2D.init();
    }

    private final CollisionCallback onCollidedDispatcher = new CollisionCallback() {
        @Override
        public void run(CollisionCallbackData data) {
            data.myCollisionHandler.onCollided(data.myBodyFirst, data.myBodySecond);
        }
    };
    private final CollisionCallback onOverlappingDispatcher = new CollisionCallback() {
        @Override
        public void run(CollisionCallbackData data) {
            data.myCollisionHandler.onOverlapping(data.myBodyFirst, data.myBodySecond);
        }
    };
    private final CollisionCallback onSeparatedDispatcher = new CollisionCallback() {
        @Override
        public void run(CollisionCallbackData data) {
            data.myCollisionHandler.onSeparated(data.myBodyFirst, data.myBodySecond);
        }
    };

    private final World myWorld;
    private final Array<PhysicsListener> myPhysicsListeners;
    private final Array<CollisionHandlerEntry> myCollisionHandlers;
    private final ArraySet<Body> myInactiveBodies;
    private final HeapPool<ActiveCollision> myActiveCollisionsPool;
    // Usually we only need 1 collision data item, but occasionally runCollisionHandlers triggers a callback which calls
    // runCollisionHandlers again recursively, but this should never go very deep.
    private final Pool<CollisionCallbackData> myCollisionDataPool = Pool.of(CollisionCallbackData.class, 4);
    @Nullable private Box2DDebugRenderer myDebugRenderer;
    @Nullable private Matrix4 myDebugRenderMatrix;

    public PhysicsSystem(int capacity, Vector2 gravity) {
        myWorld = new World(gravity, true);
        myWorld.setContactListener(new CollisionListener());

        myPhysicsListeners = new Array<PhysicsListener>(false, capacity);
        myCollisionHandlers = new Array<CollisionHandlerEntry>(false, EXPECTED_COLLIDER_HANDLER_COUNT);
        myInactiveBodies = new ArraySet<Body>(EXPECTED_INACTIVE_BODY_COUNT);
        myActiveCollisionsPool = HeapPool.of(ActiveCollision.class, capacity);
        // We expect much fewer recent separations than total number of collisions
    }

    public World getWorld() {
        return myWorld;
    }

    public void addListener(PhysicsListener physicsListener) {
        myPhysicsListeners.add(physicsListener);
    }

    public boolean removeListener(PhysicsListener physicsListener) {
        return myPhysicsListeners.removeValue(physicsListener, true);
    }

    public void setActive(Body body, boolean active) {
        if (active) {
            if (myInactiveBodies.removeIf(body)) {
                runCollisionHandlers(body, onCollidedDispatcher);
            }

        }
        else {
            if (myInactiveBodies.putIf(body)) {
                runCollisionHandlers(body, onSeparatedDispatcher);

                List<ActiveCollision> activeCollisions = myActiveCollisionsPool.getItemsInUse();
                int numCollisions = activeCollisions.size();
                for (int i = 0; i < numCollisions; i++) {
                    ActiveCollision activeCollision = activeCollisions.get(i);
                    if (activeCollision.ownsBody(body)) {
                        activeCollision.myJustCollided = true; // In case we become active again while still colliding
                    }
                }
            }
        }
    }

    /**
     * Register a {@link CollisionHandler} with this physics system. Note this either a registered handler will handle
     * a collision OR Box2D will handle it, but not both. The category order that a handler is registered with will
     * be preserved when the handler is called.
     * <p/>
     * You can register multiple handlers for the same collision, which is useful if you have a default behavior you
     * want to happen in multiple collision cases.
     */
    public void addCollisionHandler(int categoriesA, int categoriesB,
                                    CollisionHandler collisionHandler) {
        myCollisionHandlers.add(new CollisionHandlerEntry(categoriesA, categoriesB, collisionHandler));
    }

    public void update(Duration elapsedTime) {

        // Variable time step is not recommended, but we'll be careful... We can change this later if it causes
        // trouble, but otherwise, it would be nice to have 1:1 entity::update and physics::update steps.
        myWorld.step(elapsedTime.getSeconds(), VELOCITY_ITERATIONS, POSITION_ITERATIONS);

        List<ActiveCollision> activeCollisions = myActiveCollisionsPool.getItemsInUse();
        int numCollisions = activeCollisions.size();
        for (int i = 0; i < numCollisions; i++) {
            ActiveCollision activeCollision = activeCollisions.get(i);

            Fixture fixtureA = activeCollision.myFixtureA;
            Fixture fixtureB = activeCollision.myFixtureB;
            if (myInactiveBodies.contains(fixtureA.getBody()) || myInactiveBodies.contains(fixtureB.getBody())) {
                continue;
            }

            runCollisionHandlers(fixtureA, fixtureB,
                activeCollision.myJustCollided ? onCollidedDispatcher : onOverlappingDispatcher);
            activeCollision.myJustCollided = false;
        }

        for (int i = 0; i < myPhysicsListeners.size; i++) {
            PhysicsListener physicsListener = myPhysicsListeners.get(i);
            physicsListener.onUpdated();
        }
    }

    public void debugRender(Matrix4 cameraMatrix, float pixelsToMeters) {
        if (myDebugRenderer == null) {
            myDebugRenderer = new Box2DDebugRenderer();
            myDebugRenderMatrix = new Matrix4();
        }

        assert myDebugRenderMatrix != null; // set non-null w/ myDebugRenderer
        myDebugRenderMatrix.set(cameraMatrix).scl(pixelsToMeters);
        myDebugRenderer.render(myWorld, myDebugRenderMatrix);
    }

    public void dispose() {
        if (myDebugRenderer != null) {
            myDebugRenderer.dispose();
        }
        myWorld.dispose();
    }

    /**
     * Unfortunately, LibGdx does not have a way to tell us when a body is destroyed. Therefore, in order to not leak
     * references, it is better to release bodies through the physics system instead of destroying them directly.
     *
     * @see <a href="https://code.google.com/p/libgdx/issues/detail?id=484">Issue: LibGdx body listener</a>
     */
    public void destroyBody(@NotNull Body body) {
        setActive(body, false); // This forces active collisions to separate
        myInactiveBodies.remove(body); // setActive puts a body reference in myInactiveBodies - remove it!
        removeActiveCollisions(body);
        body.getWorld().destroyBody(body);
    }

    private void removeActiveCollision(Fixture fixtureA, Fixture fixtureB) {
        List<ActiveCollision> collisions = myActiveCollisionsPool.getItemsInUse();
        int numCollisions = collisions.size();
        for (int i = 0; i < numCollisions; i++) {
            ActiveCollision activeCollision = collisions.get(i);
            if (activeCollision.matches(fixtureA, fixtureB)) {
                freeActiveCollision(activeCollision);
                break;
            }
        }
    }

    private void removeActiveCollisions(Body body) {
        List<ActiveCollision> collisions = myActiveCollisionsPool.getItemsInUse();
        int numCollisions = collisions.size();
        for (int i = 0; i < numCollisions; i++) {
            ActiveCollision activeCollision = collisions.get(i);
            if (activeCollision.ownsBody(body)) {
                freeActiveCollision(activeCollision);
                i--;
                numCollisions--;
            }
        }
    }

    private void freeActiveCollision(ActiveCollision activeCollision) {
        Fixture fixtureA = activeCollision.myFixtureA;
        Fixture fixtureB = activeCollision.myFixtureB;
        myActiveCollisionsPool.free(activeCollision);

        runCollisionHandlers(fixtureA, fixtureB, onSeparatedDispatcher);
    }

    private boolean hasCollisionHandlers(Contact contact) {
        Fixture fixtureA = contact.getFixtureA();
        Fixture fixtureB = contact.getFixtureB();

        for (int i = 0; i < myCollisionHandlers.size; i++) {
            CollisionHandlerEntry entry = myCollisionHandlers.get(i);
            if (entry.matches(fixtureA.getFilterData().categoryBits, fixtureB.getFilterData().categoryBits)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Given two fixtures that are colliding, call any collision handlers that may have been registered to handle it.
     */
    private void runCollisionHandlers(Fixture fixtureA, Fixture fixtureB,
                                      CollisionCallback collisionCallback) {

        for (int i = 0; i < myCollisionHandlers.size; i++) {
            CollisionHandlerEntry entry = myCollisionHandlers.get(i);
            if (entry.matches(fixtureA.getFilterData().categoryBits, fixtureB.getFilterData().categoryBits)) {
                Body bodyA = fixtureA.getBody();
                Body bodyB = fixtureB.getBody();

                CollisionCallbackData data = myCollisionDataPool.grabNew();
                if (entry.isFirstCategory(fixtureA.getFilterData().categoryBits)) {
                    data.myBodyFirst = bodyA;
                    data.myBodySecond = bodyB;
                }
                else {
                    data.myBodyFirst = bodyB;
                    data.myBodySecond = bodyA;
                }
                data.myCollisionHandler = entry.myCollisionHandler;
                collisionCallback.run(data);
                myCollisionDataPool.freeCount(1);
            }
        }
    }

    /**
     * Run all active collision handlers that reference the {@link Body} parameter.
     */
    private void runCollisionHandlers(Body body, CollisionCallback collisionCallback) {

        List<ActiveCollision> activeCollisions = myActiveCollisionsPool.getItemsInUse();
        int numCollisions = activeCollisions.size();
        for (int i = 0; i < numCollisions; i++) {
            ActiveCollision activeCollision = activeCollisions.get(i);
            if (activeCollision.ownsBody(body)) {
                Fixture fixtureA = activeCollision.myFixtureA;
                Fixture fixtureB = activeCollision.myFixtureB;

                runCollisionHandlers(fixtureA, fixtureB, collisionCallback);
            }
        }
    }

    private interface CollisionCallback {
        void run(CollisionCallbackData callbackData);
    }

    private static final class CollisionHandlerEntry {
        int myCategoriesFirst;
        int myCategoriesSecond;
        CollisionHandler myCollisionHandler;

        public CollisionHandlerEntry(int categoriesFirst, int categoriesSecond,
                                     CollisionHandler collisionHandler) {
            myCategoriesFirst = categoriesFirst;
            myCategoriesSecond = categoriesSecond;
            myCollisionHandler = collisionHandler;
        }

        public boolean matches(int categoryA, int categoryB) {
            return (((myCategoriesFirst & categoryA) != 0 && (myCategoriesSecond & categoryB) != 0) ||
                ((myCategoriesFirst & categoryB) != 0 && (myCategoriesSecond & categoryA) != 0));
        }

        public boolean isFirstCategory(int categoryBitsA) {
            return myCategoriesFirst == categoryBitsA;
        }
    }

    private static final class ActiveCollision implements Poolable {
        public Fixture myFixtureA;
        public Fixture myFixtureB;
        public boolean myJustCollided = true;

        public boolean matches(Fixture fixtureC, Fixture fixtureD) {
            return ((myFixtureA == fixtureC && myFixtureB == fixtureD) || (myFixtureA == fixtureD && myFixtureB ==
                fixtureC));
        }

        public boolean ownsBody(Body body) {
            return myFixtureA.getBody() == body || myFixtureB.getBody() == body;
        }

        @Override
        public void reset() {
            myFixtureA = null;
            myFixtureB = null;
            myJustCollided = true;
        }
    }

    private static final class CollisionCallbackData implements Poolable {
        // Order matters with callbacks! Users expect one body to appear first and another
        // to appear second (depending on how they registered their callback)
        public Body myBodyFirst;
        public Body myBodySecond;
        public CollisionHandler myCollisionHandler;

        @Override
        public void reset() {
            myBodyFirst = null;
            myBodySecond = null;
            myCollisionHandler = null;
        }
    }

    private final class CollisionListener implements ContactListener {

        @Override
        public void beginContact(Contact contact) {
            if (!contact.isTouching()) {
                return;
            }

            if (hasCollisionHandlers(contact)) {
                ActiveCollision activeCollision = myActiveCollisionsPool.grabNew();
                activeCollision.myFixtureA = contact.getFixtureA();
                activeCollision.myFixtureB = contact.getFixtureB();
            }
        }

        @Override
        public void endContact(Contact contact) {
            if (hasCollisionHandlers(contact)) {
                Fixture fixtureA = contact.getFixtureA();
                Fixture fixtureB = contact.getFixtureB();
                removeActiveCollision(fixtureA, fixtureB);
            }
        }

        @Override
        public void preSolve(Contact contact, Manifold oldManifold) {
            if (hasCollisionHandlers(contact)) {
                contact.setEnabled(false); // Collision will be handled externally, don't handle it via Box2D
            }

            if (myInactiveBodies.contains(contact.getFixtureA().getBody()) ||
                myInactiveBodies.contains(contact.getFixtureB().getBody())) {
                contact.setEnabled(false);
            }
        }

        @Override
        public void postSolve(Contact contact, ContactImpulse impulse) {
        }
    }

}
