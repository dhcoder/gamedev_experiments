package dhcoder.libgdx.entity;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

public final class EntityManagerTest {
    @Test
    public void managerCanCreateEntities() {
        EntityManager manager = new EntityManager(1);
        Entity entity = manager.newEntity();
        assertThat(entity.getManager(), equalTo(manager));
    }
}
