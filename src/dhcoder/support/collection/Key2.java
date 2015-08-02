package dhcoder.support.collection;

import dhcoder.support.memory.Poolable;

/**
 * Class which is used to hold two values purely to be used as a key to a Map. There are no getters in this class, as
 * its point isn't to be a container you pass around. It just exposes enough methods so its values can be converted
 * into a map index.
 * <p/>
 * For readability, you may want to inherit from this class, as follows:
 * <p/>
 * {@code private final class MyKey extends Key2<SomeClass, AnotherClass> { ... thin constructors ... }}
 * <p/>
 * Important: This class provides {@link #reset()} and {@link #set(Object, Object)} methods, but this is only for being
 * able to pool keys. You absolutely should not modify a key while its in use in a Map somewhere!
 */
public class Key2<T1, T2> implements Poolable {
    private T1 value1;
    private T2 value2;

    public Key2() {}

    public Key2(final T1 value1, final T2 value2) {
        this.value1 = value1;
        this.value2 = value2;
    }

    public final Key2 set(final T1 value1, final T2 value2) {
        this.value1 = value1;
        this.value2 = value2;
        return this;
    }

    @Override
    public final int hashCode() {
        int result = value1.hashCode();
        result = 31 * result + value2.hashCode();
        return result;
    }

    @Override
    public final boolean equals(final Object o) {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }

        Key2 key2 = (Key2)o;

        if (!value1.equals(key2.value1)) { return false; }
        if (!value2.equals(key2.value2)) { return false; }

        return true;
    }

    @Override
    public final void reset() {
        value1 = null;
        value2 = null;
    }
}
