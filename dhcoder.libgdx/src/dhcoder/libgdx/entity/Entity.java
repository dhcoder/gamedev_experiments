package dhcoder.libgdx.entity;

import dhcoder.support.annotations.NotNull;
import dhcoder.support.annotations.Nullable;
import dhcoder.support.memory.Poolable;
import dhcoder.support.time.Duration;

import java.util.ArrayList;
import java.util.List;

import static dhcoder.support.text.StringUtils.format;

/**
 * A skeletal game object whose behavior is implemented by {@link Component}s.
 * <p/>
 * Allocate an entity and its components through a manager, using {@link EntityManager#newEntity()},
 * {@link EntityManager#newEntityFromTemplate(Enum)}, or {@link EntityManager#newComponent(Class)}. You can then
 * free an entity and its components by calling {@link EntityManager#freeEntity(Entity)}.
 */
public final class Entity implements Poolable {

    // Map a component's type to the component itself
    private final List<Component> components = new ArrayList<Component>();
    private final EntityManager manager;
    private boolean initialized;

    /**
     * Restricted access - use {@link EntityManager#newEntity} instead.
     */
    Entity(EntityManager manager) {
        reset();
        this.manager = manager;
    }

    public EntityManager getManager() {
        return manager;
    }

    /**
     * Add a component to the entity. You can safely add components after you've created an entity but before you call
     * {@link #update(Duration)} for the very first time.
     *
     * @throws IllegalStateException if you try to add a component to an entity that's already in use (that is, has
     *                               been updated at least once).
     */
    public <C extends Component> C addComponent(Class<C> componentClass) {
        if (initialized) {
            throw new IllegalStateException("Can't add a component to an Entity that's already in use.");
        }

        C component = manager.newComponent(componentClass);
        this.components.add(component);
        return component;
    }

    /**
     * Returns the first component that matches the input type, if found.
     */
    @SuppressWarnings("unchecked") // (T) cast is safe because of instanceof check
    @Nullable
    public <T extends Component> T getComponent(Class<T> classType) {
        int numComponets = components.size();
        for (int i = 0; i < numComponets; i++) {
            Component component = components.get(i);
            if (classType.isInstance(component)) {
                return (T)component;
            }
        }

        return null;
    }

    /**
     * Require that there be at least one instance of the specified {@link Component} on this entity, and return the
     * first one.
     *
     * @throws IllegalStateException if there aren't any components that match the class type parameter.
     */
    @SuppressWarnings("unchecked") // (T) cast is safe because of instanceof check
    @NotNull
    public <T extends Component> T requireComponent(Class<T> classType) throws IllegalStateException {
        T component = getComponent(classType);
        if (component == null) {
            throw new IllegalStateException(format("Entity doesn't have any instances of {0}",
                classType.getSimpleName()));
        }
        return component;
    }

    /**
     * Require that there be at least one instance of the specified {@link Component} on this entity, and that it exists
     * earlier in the list than another component.
     */
    @SuppressWarnings("unchecked") // (T) cast is safe because of instanceof check
    @NotNull
    public <T extends Component> T requireComponentAfter(Component otherComponent, Class<T> classType)
        throws IllegalStateException {
        boolean isAfter = false;
        int numComponents = components.size();
        for (int i = 0; i < numComponents; i++) {
            Component component = components.get(i);
            if (component == otherComponent) {
                isAfter = true;
            }
            if (classType.isInstance(component) && isAfter) {
                return (T)component;
            }
        }

        throw new IllegalStateException(
            format("Entity doesn't have any instances of {0} after {1}", classType.getSimpleName(),
                otherComponent.getClass().getSimpleName()));
    }

    /**
     * Require that there be at least one instance of the specified {@link Component} on this entity, and that it exists
     * before another component is found.
     */
    @SuppressWarnings("unchecked") // (T) cast is safe because of instanceof check
    @NotNull
    public <T extends Component> T requireComponentBefore(Component otherComponent, Class<T> classType)
        throws IllegalStateException {
        int numComponents = components.size();
        for (int i = 0; i < numComponents; i++) {
            Component component = components.get(i);
            if (component == otherComponent) {
                break;
            }
            if (classType.isInstance(component)) {
                return (T)component;
            }
        }

        throw new IllegalStateException(
            format("Entity doesn't have any instances of {0} before {1}", classType.getSimpleName(),
                otherComponent.getClass().getSimpleName()));
    }

    /**
     * Update this entity. The passed in time is in seconds.
     */
    public void update(Duration elapsedTime) {
        if (!initialized) {
            initialize();
        }

        int numComponents = components.size(); // Simple iteration to avoid Iterator allocation
        for (int i = 0; i < numComponents; ++i) {
            components.get(i).update(elapsedTime);
        }
    }

    @Override
    public void reset() {
        components.clear();
        initialized = false;
    }

    /**
     * Convenience method for {@link EntityManager#freeEntity(Entity)} called with this entity.
     */
    public void free() {
        getManager().freeEntity(this);
    }

    // Called by EntityManager
    void freeComponents() {
        int numComponents = components.size(); // Simple iteration to avoid Iterator allocation
        for (int i = 0; i < numComponents; ++i) {
            manager.freeComponent(components.get(i));
        }
    }

    private void initialize() {
        assert !initialized;

        int numComponents = components.size();
        for (int i = 0; i < numComponents; i++) {
            Component component = components.get(i);
            component.initialize(this);
        }
        initialized = true;
    }
}
