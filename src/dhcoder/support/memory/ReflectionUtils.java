package dhcoder.support.memory;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;

import static dhcoder.support.text.StringUtils.format;

/**
 * A collection of utility methods that use reflection.
 */
public final class ReflectionUtils {

    public interface EqualityTester<T> {
        boolean areSame(T obj1, T obj2);
    }

    private static class PrimitiveEqualityTester implements EqualityTester {
        @Override
        public boolean areSame(final Object obj1, final Object obj2) {
            return obj1.equals(obj2);
        }
    }

    private static class DefaultEqualityTester implements EqualityTester {
        @Override
        public boolean areSame(final Object obj1, final Object obj2) {
            try {
                assertSame(obj1, obj2);
            } catch (IllegalStateException e) {
                return false;
            }

            return true;
        }
    }

    private static final HashMap<Class, EqualityTester> EQUALITY_TESTERS = new HashMap<Class, EqualityTester>();
    private static final DefaultEqualityTester FALLBACK_TESTER = new DefaultEqualityTester();

    private static final int MAX_DEPTH = 10;
    private static int depth = 0;

    static {
        EQUALITY_TESTERS.put(boolean.class, new PrimitiveEqualityTester());
        EQUALITY_TESTERS.put(char.class, new PrimitiveEqualityTester());
        EQUALITY_TESTERS.put(short.class, new PrimitiveEqualityTester());
        EQUALITY_TESTERS.put(int.class, new PrimitiveEqualityTester());
        EQUALITY_TESTERS.put(float.class, new PrimitiveEqualityTester());
        EQUALITY_TESTERS.put(long.class, new PrimitiveEqualityTester());
        EQUALITY_TESTERS.put(double.class, new PrimitiveEqualityTester());
        EQUALITY_TESTERS.put(Boolean.class, new PrimitiveEqualityTester());
        EQUALITY_TESTERS.put(Character.class, new PrimitiveEqualityTester());
        EQUALITY_TESTERS.put(Short.class, new PrimitiveEqualityTester());
        EQUALITY_TESTERS.put(Integer.class, new PrimitiveEqualityTester());
        EQUALITY_TESTERS.put(Float.class, new PrimitiveEqualityTester());
        EQUALITY_TESTERS.put(Long.class, new PrimitiveEqualityTester());
        EQUALITY_TESTERS.put(Double.class, new PrimitiveEqualityTester());
    }

    public static void registerEqualityTester(final Class classType, final EqualityTester equalityTester) {
        EQUALITY_TESTERS.put(classType, equalityTester);
    }

    /**
     * Use reflection to check that all fields in the two classes being compared have the same value.
     * <p/>
     * NOTE: This method should only be called in development code. Heavy use of reflection causes lots of small
     * allocations to be made, so it shouldn't be released in production code.
     *
     * @throws IllegalStateException if the target class has a field set to a non-default value.
     */
    public static <T> void assertSame(final T obj1, final T obj2) {
        depth++;

        if (depth > MAX_DEPTH) {
            throw new StackOverflowError(format("assertSame called recursively over {0} times.", MAX_DEPTH));
        }

        Class classType = obj1.getClass();
        final Field[] fields = classType.getDeclaredFields();
        for (int i = 0; i < fields.length; i++) {
            Field field = fields[i];
            if (Modifier.isStatic(field.getModifiers())) {
                continue;
            }
            try {
                field.setAccessible(true);
                final Object value1 = field.get(obj1);
                final Object value2 = field.get(obj2);

                if (value1 == value2) {
                    continue; // Not only equal, but the same object!
                }

                Class fieldType = field.getType();
                int nullCount = (value1 == null ? 1 : 0) + (value2 == null ? 1 : 0);
                if (nullCount == 1 || (nullCount == 0 && !getTester(fieldType).areSame(value1, value2))) {
                    throw new IllegalStateException(
                        format("Field {0}#{1} ({2}) has value {3}, expected {4}", classType.getSimpleName(),
                            field.getName(), fieldType.getSimpleName(), value1, value2));
                }
            } catch (IllegalAccessException e) {
                throw new IllegalStateException(
                    format("Unexpected illegal access of field {0}#{1}", classType.getSimpleName(), field.getName()));
            }
        }

        depth--;
    }

    private static EqualityTester getTester(final Class classType) {
        if (!EQUALITY_TESTERS.containsKey(classType)) {
            return FALLBACK_TESTER;
        }

        return EQUALITY_TESTERS.get(classType);
    }

    private ReflectionUtils() {}

}
