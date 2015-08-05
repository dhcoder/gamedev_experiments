package dhcoder.libgdx.entity;

import dhcoder.support.memory.Poolable;
import dhcoder.support.opt.Opt;
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
    private boolean active;
    private boolean initialized;

    /**
     * Restricted access - use {@link EntityManager#newEntity} instead.
     */
    Entity(final EntityManager manager) {
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
    public <C extends Component> C addComponent(final Class<C> componentClass) {
        if (initialized) {
            throw new IllegalStateException("Can't add a component to an Entity that's already in use.");
        }

        C component = manager.newComponent(componentClass);
        this.components.add(component);
        return component;
    }

    /**
     * Returns the component that matches the input type, if found.
     */
    @SuppressWarnings("unchecked") // (T) cast is safe because of instanceof check
    public <T extends Component> void getComponent(final Class<T> classType, final Opt<T> outComponent) {
        outComponent.clear();
        int numComponets = components.size();
        for (int i = 0; i < numComponets; i++) {
            Component component = components.get(i);
            if (classType.isInstance(component)) {
                outComponent.set((T)component);
            }
        }
    }

    /**
     * Require that there be at least one instance of the specified {@link Component} on this entity, and return the
     * first one.
     *
     * @return the first matching component
     * @throws IllegalStateException if there aren't any components that match the class type parameter.
     */
    public <T extends Component> T requireComponent(final Class<T> classType) throws IllegalStateException {
        int numComponets = components.size();
        for (int i = 0; i < numComponets; i++) {
            Component component = components.get(i);
            if (classType.isInstance(component)) {
                return (T)component;
            }
        }

        throw new IllegalStateException(format("Entity doesn't have any instances of {0}", classType.getSimpleName()));
    }

    /**
     * Require that there be at least one instance of the specified {@link Component} on this entity, and that it exists
     * earlier in the list than another component.
     */
    public <T extends Component> T requireComponentAfter(final Component otherComponent, final Class<T> classType)
        throws IllegalStateException {
        boolean isAfter = false;
        int numComponets = components.size();
        for (int i = 0; i < numComponets; i++) {
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
    public <T extends Component> T requireComponentBefore(final Component otherComponent, final Class<T> classType)
        throws IllegalStateException {
        int numComponets = components.size();
        for (int i = 0; i < numComponets; i++) {
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
    public void update(final Duration elapsedTime) {
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

        final int numComponents = components.size();
        for (int i = 0; i < numComponents; i++) {
            Component component = components.get(i);
            component.initialize(this);
        }
        initialized = true;
    }
}
