package dhcoder.libgdx.entity;

import dhcoder.support.time.Duration;
import org.junit.Test;

import static dhcoder.test.TestUtils.assertException;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

public final class EntityTest {

    private static final class DummyComponent extends AbstractComponent {
        private Entity owner;

        public Entity getOwner() {
            return owner;
        }

        @Override
        public void initialize(Entity owner) {
            this.owner = owner;
        }
    }

    /**
     * This class exists only to be found by a {@link DependentComponent}
     */
    private static final class SourceComponent extends AbstractComponent {}

    /**
     * This component expects to find a {@link SourceComponent} on the {@link Entity} it's attached to.
     */
    private static final class DependentComponent extends AbstractComponent {

        private SourceComponent sourceComponent;

        public SourceComponent getSourceComponent() {
            return sourceComponent;
        }

        @Override
        public void initialize(Entity owner) {
            sourceComponent = owner.requireComponent(SourceComponent.class);
        }
    }

    @Test
    public void managerCanCreateEntities() {
        EntityManager manager = new EntityManager(1);
        Entity entity = manager.newEntity();
        assertThat(entity.getManager(), equalTo(manager));
    }

    @Test
    public void componentGetsInitializedWithItsOwningEntityOnFirstUpdate() {
        EntityManager manager = new EntityManager(1);

        Entity entity = manager.newEntity();
        DummyComponent dummyComponent = entity.addComponent(DummyComponent.class);
        assertThat(dummyComponent.getOwner(), equalTo(null));

        entity.update(Duration.zero());
        assertThat(entity, equalTo(dummyComponent.getOwner()));
    }

    @Test
    public void requireComponentReturnsExpectedValues() {
        EntityManager manager = new EntityManager(1);

        final Entity entity = manager.newEntity();
        DummyComponent dummyComponent = entity.addComponent(DummyComponent.class);
        SourceComponent sourceComponent = entity.addComponent(SourceComponent.class);
        entity.update(Duration.zero());
        assertThat(entity.requireComponent(DummyComponent.class), equalTo(dummyComponent));
        assertThat(entity.requireComponent(SourceComponent.class), equalTo(sourceComponent));
        assertException("RequireComponent throws exception if it can't find the component", IllegalStateException.class,
            new Runnable() {
                @Override
                public void run() {
                    entity.requireComponent(DependentComponent.class);
                }
            });
    }

    @Test
    public void dependentComponentCanFindOtherComponent() {
        EntityManager manager = new EntityManager(1);

        Entity entity = manager.newEntity();
        SourceComponent sourceComponent = entity.addComponent(SourceComponent.class);
        DependentComponent dependentComponent = entity.addComponent(DependentComponent.class);
        entity.update(Duration.zero());
        assertThat(sourceComponent, equalTo(dependentComponent.getSourceComponent()));
    }

    @Test
    public void dependentComponentCanFindOtherComponent_EntityConstructedReverseOrder() {
        EntityManager manager = new EntityManager(1);

        // Component order shouldn't matter here, even if we pass in dependentComponent first
        Entity entity = manager.newEntity();
        SourceComponent sourceComponent = entity.addComponent(SourceComponent.class);
        DependentComponent dependentComponent = entity.addComponent(DependentComponent.class);
        entity.update(Duration.zero());
        assertThat(sourceComponent, equalTo(dependentComponent.getSourceComponent()));
    }
}