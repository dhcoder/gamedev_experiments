package dhcoder.support.pattern;

import java.util.HashMap;
import java.util.Map;

import static dhcoder.support.text.StringUtils.format;

/**
 * Class which implements the service pattern - namely, it provides a way to register and, later, retrieve classes
 * by type. Having one singleton service provider is a suitable replacement for, and has many advantages over, creating
 * Singletons everywhere.
 * <p/>
 * A service can be anything, really, but a great example case for a service is a logger - imagine you have interface
 * {@code Logger} with implementations {@code ConsoleLogger} (for debug builds) and {@code StubLogger} (for release
 * builds). You can register the appropriate service at load time and the rest of the code can just call {@code
 * services.get(Logger.class).Log("So and so")}.
 *
 * @see <a href="http://gameprogrammingpatterns.com/service-locator.html">Service Locater Pattern</a>
 */
public final class ServiceLocator {
    private Map<Class<?>, Object> services = new HashMap<Class<?>, Object>(); // Map type to instance of type

    /**
     * Register a service under a type key.
     */
    public <T> void register(Class<? extends T> classType, T instance) {
        if (services.containsKey(classType)) {
            throw new IllegalArgumentException(format("Attempt to register duplicate service type {0}", classType));
        }

        services.put(classType, instance);
    }

    @SuppressWarnings("unchecked") // (T) cast is safe because register always puts the correct type in
    public <T> T get(Class<? extends T> classType) {
        if (!services.containsKey(classType)) {
            throw new IllegalArgumentException(format("Request for unregistered service {0}", classType));
        }

        return (T)services.get(classType);
    }
}
