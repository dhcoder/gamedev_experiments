package dhcoder.libgdx.entity;

import dhcoder.support.annotations.Nullable;
import dhcoder.support.collection.ArrayMap;
import dhcoder.support.collection.IntKey;
import dhcoder.support.memory.HeapPool;
import dhcoder.support.memory.Pool;
import dhcoder.support.time.Duration;

import java.util.List;
import java.util.Stack;

import static dhcoder.support.text.StringUtils.format;

/**
 * A class which manages of collection of entities.
 */
public final class EntityManager {

    public interface EntityCreator {
        void initialize(Entity entity);
    }

    private final HeapPool<Entity> entityPool;
    private final ArrayMap<Class, HeapPool> componentPools;
    private final ArrayMap<IntKey, EntityCreator> templates;
    private final Stack<Entity> queuedForRemoval;
    private final Pool<IntKey> keyPool = Pool.of(IntKey.class, 1);

    public EntityManager(int maxEntityCount) {
        final EntityManager manager = this; // For assigning within a closure
        entityPool = new HeapPool<Entity>(new Pool.AllocateMethod<Entity>() {
            @Override
            public Entity run() {
                return new Entity(manager);
            }
        }, new Pool.ResetMethod<Entity>() {
            @Override
            public void run(Entity item) {
                item.reset();
            }
        }, maxEntityCount);
        queuedForRemoval = new Stack<Entity>();
        queuedForRemoval.ensureCapacity(maxEntityCount / 10);
        componentPools = new ArrayMap<Class, HeapPool>(32);
        templates = new ArrayMap<IntKey, EntityCreator>();
    }

    /**
     * Register an ID with a function that creates an {@link Entity}. This can later be used to
     * create an entity with {@link #newEntityFromTemplate(Enum)}. Note that the enum ID can be any
     * enumeration value; it is recommended you create a local {@link Enum} somewhere in your
     * project that represents all entities and use it consistently.
     */
    public void registerTemplate(Enum id, EntityCreator entityCreator) {
        if (getEntityCreator(id) != null) {
            throw new IllegalArgumentException(format("Attempt to register duplicate entity template id {0}", id));
        }
        templates.put(new IntKey(id.ordinal()), entityCreator);
    }

    /**
     * Create a new entity based on a template registered with {@link #registerTemplate(Enum, EntityCreator)}.
     */
    public Entity newEntityFromTemplate(Enum id) {

        EntityCreator creator = getEntityCreator(id);
        if (creator == null) {
            throw new IllegalArgumentException(format("Attempt to create entity from invalid template id {0}", id));
        }

        Entity entity = newEntity();
        creator.initialize(entity);

        return entity;
    }

    public Entity newEntity() {
        return entityPool.grabNew();
    }

    public <C extends Component> C newComponent(Class<C> componentClass) {
        if (!componentPools.containsKey(componentClass)) {
            componentPools.put(componentClass,
                HeapPool.of(componentClass, entityPool.getCapacity()).makeResizable(entityPool.getMaxCapacity()));
        }

        //noinspection unchecked
        return (C)componentPools.get(componentClass).grabNew();
    }

    /**
     * Call when you are done with this entity and want to release its resources. If an update cycle is in progress,
     * it will be freed after the cycle has finished.
     */
    public void freeEntity(Entity entity) {
        // It's possible that this method can get called more than once before we have a chance to actually remove the
        // entity, so we guard against that here.
        if (!queuedForRemoval.contains(entity)) {
            queuedForRemoval.push(entity);
        }
    }

    public void update(Duration elapsedTime) {
        // Kill any dead objects from the last cycle
        while (!queuedForRemoval.empty()) {
            freeEntityInternal(queuedForRemoval.pop());
        }

        List<Entity> entities = entityPool.getItemsInUse();
        int numEntities = entities.size();
        for (int i = 0; i < numEntities; ++i) {
            entities.get(i).update(elapsedTime);
        }
    }

    void freeComponent(Component component) {
        Class<? extends Component> componentClass = component.getClass();
        if (!componentPools.containsKey(componentClass)) {
            throw new IllegalArgumentException(
                format("Can't free component type {0} as we don't own it.", componentClass));
        }

        //noinspection unchecked
        componentPools.get(componentClass).free(component);
    }

    @Nullable
    private EntityCreator getEntityCreator(Enum id) {
        IntKey key = keyPool.grabNew().set(id.ordinal());
        EntityCreator creator = templates.getOrNull(key);
        keyPool.free(key);

        return creator;
    }

    private void freeEntityInternal(Entity entity) {
        entity.freeComponents();
        entityPool.free(entity);
    }
}
