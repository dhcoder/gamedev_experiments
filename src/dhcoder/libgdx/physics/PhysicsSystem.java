package dhcoder.libgdx.physics;

import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Box2D;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import dhcoder.support.collection.ArraySet;
import dhcoder.support.memory.HeapPool;
import dhcoder.support.memory.Pool;
import dhcoder.support.memory.Poolable;
import dhcoder.support.time.Duration;

import java.util.List;

import static dhcoder.support.contract.ContractUtils.requireTrue;
import static dhcoder.support.math.BitUtils.getBitIndex;
import static dhcoder.support.text.StringUtils.format;

/**
 * Constants for our physics system
 */
public final class PhysicsSystem {

    private interface CollisionCallback {
        public void run(CollisionCallbackData callbackData);
    }

    private static final class CollisionHandlerEntry {
        int categoriesFirst;
        int categoriesSecond;
        CollisionHandler collisionHandler;

        public CollisionHandlerEntry(final int categoriesFirst, final int categoriesSecond,
            final CollisionHandler collisionHandler) {
            this.categoriesFirst = categoriesFirst;
            this.categoriesSecond = categoriesSecond;
            this.collisionHandler = collisionHandler;
        }

        public boolean matches(final int categoryA, final int categoryB) {
            return (((this.categoriesFirst & categoryA) != 0 && (this.categoriesSecond & categoryB) != 0) ||
                ((this.categoriesFirst & categoryB) != 0 && (this.categoriesSecond & categoryA) != 0));
        }

        public boolean isFirstCategory(final int categoryBitsA) {
            return categoriesFirst == categoryBitsA;
        }
    }

    private static final class ActiveCollision implements Poolable {
        public Fixture fixtureA;
        public Fixture fixtureB;
        public boolean justCollided = true;

        public boolean matches(final Fixture fixtureC, final Fixture fixtureD) {
            return ((fixtureA == fixtureC && fixtureB == fixtureD) || (fixtureA == fixtureD && fixtureB == fixtureC));
        }

        public boolean ownsBody(final Body body) {
            return fixtureA.getBody() == body || fixtureB.getBody() == body;
        }

        @Override
        public void reset() {
            fixtureA = null;
            fixtureB = null;
            justCollided = true;
        }
    }

    private static final class CollisionCallbackData implements Poolable {
        // Order matters with callbacks! Users expect one body to appear first and another
        // to appear second (depending on how they registered their callback)
        public Body bodyFirst;
        public Body bodySecond;
        public CollisionHandler collisionHandler;

        @Override
        public void reset() {
            bodyFirst = null;
            bodySecond = null;
            collisionHandler = null;
        }
    }

    private final class CollisionListener implements ContactListener {

        @Override
        public void beginContact(final Contact contact) {
            if (!contact.isTouching()) {
                return;
            }

            if (hasCollisionHandlers(contact)) {
                final ActiveCollision activeCollision = activeCollisionsPool.grabNew();
                activeCollision.fixtureA = contact.getFixtureA();
                activeCollision.fixtureB = contact.getFixtureB();
            }
        }

        @Override
        public void endContact(final Contact contact) {
            if (hasCollisionHandlers(contact)) {
                Fixture fixtureA = contact.getFixtureA();
                Fixture fixtureB = contact.getFixtureB();
                removeActiveCollision(fixtureA, fixtureB);
            }
        }

        @Override
        public void preSolve(final Contact contact, final Manifold oldManifold) {
            if (hasCollisionHandlers(contact)) {
                contact.setEnabled(false); // Collision will be handled externally, don't handle it via Box2D
            }

            if (inactiveBodies.contains(contact.getFixtureA().getBody()) ||
                inactiveBodies.contains(contact.getFixtureB().getBody())) {
                contact.setEnabled(false);
            }
        }

        @Override
        public void postSolve(final Contact contact, final ContactImpulse impulse) {}
    }

    private static final int EXPECTED_COLLIDER_HANDLER_COUNT = 30;
    private static final int EXPECTED_INACTIVE_BODY_COUNT = 10;
    // Recommended values from Box2D manual
    private static final int VELOCITY_ITERATIONS = 6;
    private static final int POSITION_ITERATIONS = 2;
    /**
     * Box2D has a hard limit of 16 collision categories.
     */
    public static int MAX_NUM_CATEGORIES = 16;
    private final CollisionCallback onCollidedDispatcher = new CollisionCallback() {
        @Override
        public void run(final CollisionCallbackData data) {
            data.collisionHandler.onCollided(data.bodyFirst, data.bodySecond);
        }
    };
    private final CollisionCallback onOverlappingDispatcher = new CollisionCallback() {
        @Override
        public void run(final CollisionCallbackData data) {
            data.collisionHandler.onOverlapping(data.bodyFirst, data.bodySecond);
        }
    };
    private final CollisionCallback onSeparatedDispatcher = new CollisionCallback() {
        @Override
        public void run(final CollisionCallbackData data) {
            data.collisionHandler.onSeparated(data.bodyFirst, data.bodySecond);
        }
    };
    private final World world;
    private final Array<PhysicsUpdateListener> physicsElements;
    private final Array<CollisionHandlerEntry> collisionHandlers;
    private final ArraySet<Body> inactiveBodies;
    private final HeapPool<ActiveCollision> activeCollisionsPool;
    // Usually we only need 1 collision data item, but occasionally runCollisionHandlers triggers a callback which calls
    // runCollisionHandlers again recursively, but this should never go very deep.
    private final Pool<CollisionCallbackData> collisionDataPool = Pool.of(CollisionCallbackData.class, 4);
    private int[] categoryMasks = new int[MAX_NUM_CATEGORIES];
    private Box2DDebugRenderer collisionRenderer;
    private Matrix4 debugRenderMatrix;

    static {
        Box2D.init();
    }

    public PhysicsSystem(final int capacity) {
        this.world = new World(new Vector2(0f, 0f), true);
        world.setContactListener(new CollisionListener());

        physicsElements = new Array<PhysicsUpdateListener>(false, capacity);
        collisionHandlers = new Array<CollisionHandlerEntry>(false, EXPECTED_COLLIDER_HANDLER_COUNT);
        inactiveBodies = new ArraySet<Body>(EXPECTED_INACTIVE_BODY_COUNT);
        activeCollisionsPool = HeapPool.of(ActiveCollision.class, capacity);
    }

    public World getWorld() {
        return world;
    }

    public void addUpdateListener(final PhysicsUpdateListener physicsUpdateListener) {
        physicsElements.add(physicsUpdateListener);
    }

    public boolean removeUpdateListener(final PhysicsUpdateListener physicsUpdateListener) {
        return physicsElements.removeValue(physicsUpdateListener, true);
    }

    public void setActive(final Body body, final boolean active) {
        if (active) {
            if (inactiveBodies.removeIf(body)) {
                runCollisionHandlers(body, onCollidedDispatcher);
            }

        }
        else {
            if (inactiveBodies.putIf(body)) {
                runCollisionHandlers(body, onSeparatedDispatcher);

                final List<ActiveCollision> activeCollisions = activeCollisionsPool.getItemsInUse();
                int numCollisionFixtures = activeCollisions.size();
                for (int i = 0; i < numCollisionFixtures; i++) {
                    ActiveCollision activeCollision = activeCollisions.get(i);
                    if (activeCollision.ownsBody(body)) {
                        activeCollision.justCollided = true; // In case we become active again while still colliding
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
    public void addCollisionHandler(final int categoriesA, final int categoriesB,
        final CollisionHandler collisionHandler) {

        for (int i = 0; i < MAX_NUM_CATEGORIES; i++) {
            int bitMask = 1 << i;
            if (((categoriesA & bitMask) != 0) && ((categoryMasks[i] & categoriesB) == 0)) {
                throw new IllegalArgumentException(format(
                    "Attempting to add handler for categories that don't collide ({0} and {1}). Did you forget to " +
                        "call registerCollidable?", categoriesA, categoriesB));
            }
        }
        collisionHandlers.add(new CollisionHandlerEntry(categoriesA, categoriesB, collisionHandler));
    }

    public void update(final Duration elapsedTime) {

        // Variable time step is not recommended, but we'll be careful... We can change this later if it causes
        // trouble, but otherwise, it would be nice to have 1:1 entity::update and physics::update steps.
        world.step(elapsedTime.getSeconds(), VELOCITY_ITERATIONS, POSITION_ITERATIONS);

        final List<ActiveCollision> activeCollisions = activeCollisionsPool.getItemsInUse();
        int numCollisions = activeCollisions.size();
        for (int i = 0; i < numCollisions; i++) {
            ActiveCollision activeCollision = activeCollisions.get(i);

            final Fixture fixtureA = activeCollision.fixtureA;
            final Fixture fixtureB = activeCollision.fixtureB;
            if (inactiveBodies.contains(fixtureA.getBody()) || inactiveBodies.contains(fixtureB.getBody())) {
                continue;
            }

            runCollisionHandlers(fixtureA, fixtureB,
                activeCollision.justCollided ? onCollidedDispatcher : onOverlappingDispatcher);
            activeCollision.justCollided = false;
        }

        for (int i = 0; i < physicsElements.size; i++) {
            PhysicsUpdateListener physicsUpdateListener = physicsElements.get(i);
            physicsUpdateListener.onPhysicsUpdate();
        }
    }

    public void debugRender(final Matrix4 cameraMatrix, final float pixelsToMeters) {
        if (collisionRenderer == null) {
            collisionRenderer = new Box2DDebugRenderer();
            debugRenderMatrix = new Matrix4();
        }

        debugRenderMatrix.set(cameraMatrix).scl(pixelsToMeters);
        collisionRenderer.render(world, debugRenderMatrix);
    }

    public void dispose() {
        if (collisionRenderer != null) {
            collisionRenderer.dispose();
        }
        world.dispose();
    }

    public void registerCollidable(final int categoriesA, final int categoriesB) {
        for (int i = 0; i < MAX_NUM_CATEGORIES; i++) {
            int bitMask = 1 << i;
            if ((categoriesA & bitMask) != 0) {
                categoryMasks[i] |= categoriesB;
            }
            if ((categoriesB & bitMask) != 0) {
                categoryMasks[i] |= categoriesA;
            }
        }
    }

    public int getCategoryMask(final int category) {
        final int bitIndex = getBitIndex(category);
        requireTrue(bitIndex < MAX_NUM_CATEGORIES, "Requesting mask for invalid category");
        return categoryMasks[bitIndex];
    }

    /**
     * Unfortunately, LibGdx does not have a way to tell us when a body is destroyed. Therefore, in order to not leak
     * references, it is better to release bodies through the physics system instead of destroying them directly.
     *
     * @see <a href="https://code.google.com/p/libgdx/issues/detail?id=484">Issue: LibGdx body listener</a>
     */
    public void destroyBody(final Body body) {
        setActive(body, false); // This forces active collisions to separate
        inactiveBodies.remove(body); // setActive puts a body reference in inactiveBodies - remove it!
        removeActiveCollisions(body);
        body.getWorld().destroyBody(body);
    }

    private void removeActiveCollision(final Fixture fixtureA, final Fixture fixtureB) {
        final List<ActiveCollision> collisions = activeCollisionsPool.getItemsInUse();
        int numCollisions = collisions.size();
        for (int i = 0; i < numCollisions; i++) {
            ActiveCollision activeCollision = collisions.get(i);
            if (activeCollision.matches(fixtureA, fixtureB)) {

                activeCollisionsPool.free(activeCollision);
                break;
            }
        }
    }

    private void removeActiveCollisions(final Body body) {
        final List<ActiveCollision> collisions = activeCollisionsPool.getItemsInUse();
        int numCollisions = collisions.size();
        for (int i = 0; i < numCollisions; i++) {
            ActiveCollision activeCollision = collisions.get(i);
            if (activeCollision.ownsBody(body)) {
                activeCollisionsPool.free(activeCollision);
                i--;
                numCollisions--;
            }
        }
    }

    private boolean hasCollisionHandlers(final Contact contact) {
        Fixture fixtureA = contact.getFixtureA();
        Fixture fixtureB = contact.getFixtureB();

        for (int i = 0; i < collisionHandlers.size; i++) {
            CollisionHandlerEntry entry = collisionHandlers.get(i);
            if (entry.matches(fixtureA.getFilterData().categoryBits, fixtureB.getFilterData().categoryBits)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Given two fixtures that are colliding, call any collision handlers that may have been registered to handle it.
     */
    private void runCollisionHandlers(final Fixture fixtureA, final Fixture fixtureB,
        final CollisionCallback collisionCallback) {

        for (int i = 0; i < collisionHandlers.size; i++) {
            CollisionHandlerEntry entry = collisionHandlers.get(i);
            if (entry.matches(fixtureA.getFilterData().categoryBits, fixtureB.getFilterData().categoryBits)) {
                Body bodyA = fixtureA.getBody();
                Body bodyB = fixtureB.getBody();

                final CollisionCallbackData data = collisionDataPool.grabNew();
                if (entry.isFirstCategory(fixtureA.getFilterData().categoryBits)) {
                    data.bodyFirst = bodyA;
                    data.bodySecond = bodyB;
                }
                else {
                    data.bodyFirst = bodyB;
                    data.bodySecond = bodyA;
                }
                data.collisionHandler = entry.collisionHandler;
                collisionCallback.run(data);
                collisionDataPool.freeCount(1);
            }
        }
    }

    /**
     * Run all active collision handlers that reference the {@link Body} parameter.
     */
    private void runCollisionHandlers(final Body body, final CollisionCallback collisionCallback) {

        final List<ActiveCollision> activeCollisions = activeCollisionsPool.getItemsInUse();
        int numCollisions = activeCollisions.size();
        for (int i = 0; i < numCollisions; i++) {
            ActiveCollision activeCollision = activeCollisions.get(i);
            if (activeCollision.ownsBody(body)) {
                final Fixture fixtureA = activeCollision.fixtureA;
                final Fixture fixtureB = activeCollision.fixtureB;

                runCollisionHandlers(fixtureA, fixtureB, collisionCallback);
            }
        }
    }

}
