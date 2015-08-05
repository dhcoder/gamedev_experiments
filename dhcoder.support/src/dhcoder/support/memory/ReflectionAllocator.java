package dhcoder.support.memory;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import static dhcoder.support.text.StringUtils.format;

/**
* A class which can allocate any target class by reflection as long as that target class has a null constructor.
*/
public final class ReflectionAllocator<T> {
    private static IllegalArgumentException newConstructionException(Class<?> targetClass) {
        return new IllegalArgumentException(
            format("Class type {0} must have an empty constructor and be instantiable", targetClass));
    }

    private final Class<T> targetClass;
    private final Constructor<T> constructor;

    public ReflectionAllocator(Class<T> targetClass) {
        this.targetClass = targetClass;
        try {
            constructor = targetClass.getDeclaredConstructor();
            constructor.setAccessible(true);
        } catch (NoSuchMethodException e) {
            throw newConstructionException(targetClass);
        }
    }

    public T allocate() {
        try {
            return constructor.newInstance();
        } catch (InstantiationException e) {
            throw newConstructionException(targetClass);
        } catch (IllegalAccessException e) {
            throw newConstructionException(targetClass);
        } catch (InvocationTargetException e) {
            throw newConstructionException(targetClass);
        }
    }
}
