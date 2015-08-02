package dhcoder.support.collection;

import dhcoder.support.memory.Poolable;

/**
 * Class which is used to hold an integer value which will be used as a key into a hashtable. Using an IntKey instead
 * of an int directly avoids boxing / unboxing.
 * <p/>
 * Important: This class provides {@link #reset()} and {@link #set(Object, Object)} methods, but this is only for being
 * able to pool keys. You absolutely should not modify a key while its in use in a Map somewhere!
 */
public class IntKey implements Poolable {
    private int value;

    public IntKey() {}

    public IntKey(final int value) {
        set(value);
    }

    public final IntKey set(final int value) {
        this.value = value;
        return this;
    }

    @Override
    public int hashCode() {
        return value;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }

        IntKey intKey = (IntKey)o;

        if (value != intKey.value) { return false; }

        return true;
    }

    @Override
    public final void reset() {
        value = 0;
    }
}
