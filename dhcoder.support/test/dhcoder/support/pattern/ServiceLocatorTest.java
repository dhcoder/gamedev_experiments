package dhcoder.support.pattern;

import org.junit.Test;

import static dhcoder.test.TestUtils.assertException;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

public final class ServiceLocatorTest {

    private interface TestService {}

    private final class DummyService1 implements TestService {}

    private final class DummyService2 implements TestService {}

    @Test
    public void serviceCanRegisterAndRetrieveServices() {
        ServiceLocator serviceLocator = new ServiceLocator();
        DummyService1 dummyService1 = new DummyService1();
        DummyService2 dummyService2 = new DummyService2();

        serviceLocator.register(DummyService1.class, dummyService1);
        serviceLocator.register(DummyService2.class, dummyService2);

        assertThat(serviceLocator.get(DummyService1.class), equalTo(dummyService1));
        assertThat(serviceLocator.get(DummyService2.class), equalTo(dummyService2));
    }

    @Test
    public void registeringDuplicateServiceTypesThrowsException() {
        final ServiceLocator serviceLocator = new ServiceLocator();
        TestService dummyService1 = new DummyService1();
        final TestService dummyService2 = new DummyService2();

        serviceLocator.register(TestService.class, dummyService1);

        assertException("Duplicate services are not allowed", IllegalArgumentException.class, new Runnable() {
            @Override
            public void run() {
                serviceLocator.register(TestService.class, dummyService2);
            }
        });
    }

    @Test
    public void requestingUnregistedServiceThrowsException() {
        final ServiceLocator serviceLocator = new ServiceLocator();
        TestService dummyService1 = new DummyService1();
        serviceLocator.register(DummyService1.class, dummyService1);

        assertException("Can't request unregistered service.", IllegalArgumentException.class, new Runnable() {
            @Override
            public void run() {
                serviceLocator.get(DummyService2.class);
            }
        });
    }

}