package dhcoder.support.collection;

import java.util.List;

/**
 * Like {@link ArrayMap} but where you only care whether a key is present or not and values don't matter.
 */
public final class ArraySet<E> {

    private static final Object DUMMY_OJBECT = new Object();

    private ArrayMap<E, Object> internalMap;

    public ArraySet() { internalMap = new ArrayMap<E, Object>(); }

    public ArraySet(final int expectedSize) { internalMap = new ArrayMap<E, Object>(expectedSize); }

    /**
     * Create a set with an expected size and load factor. The load factor dictates how full a hashtable should get
     * before it resizes. A load factor of 0.5 means the table should resize when it is 50% full.
     *
     * @throws IllegalArgumentException if the input load factor is not between 0 and 1.
     */
    public ArraySet(final int expectedSize, final float loadFactor) {
        internalMap = new ArrayMap<E, Object>(expectedSize, loadFactor);
    }

    public int getSize() { return internalMap.getSize(); }

    /**
     * Note: This method allocates an array and should only be used in non-critical areas.
     */
    public List<E> getKeys() {
        return internalMap.getKeys();
    }

    public boolean isEmpty() { return internalMap.isEmpty(); }

    public boolean contains(final E element) { return internalMap.containsKey(element); }

    /**
     * Add the element, which must NOT exist in the set. Use {@link #putIf(Object)} if you don't need this
     * requirement.
     */
    public void put(final E element) { internalMap.put(element, DUMMY_OJBECT); }

    /**
     * Add the element, if it's not already in the set.
     *
     * @return {@code true} if the element was only just now added into the set.
     */
    public boolean putIf(final E element) {
        return internalMap.putOrReplace(element, DUMMY_OJBECT) == ArrayMap.InsertMethod.PUT;
    }

    /**
     * Remove the element, which MUST exist in the set. Use {@link #removeIf(Object)} if you don't need this
     * requirement. This distinction can be useful to assert cases when you want to guarantee the element is in the set,
     * and it also better mimics the related {@link ArrayMap} class.
     */
    public void remove(final E element) { internalMap.remove(element); }

    /**
     * Remove the element, which may or may not exist in the set. Use {@link #remove(Object)} if you want to assert
     * existence of the element in the set.
     *
     * @return {@code true} if the element was in the set.
     */
    public boolean removeIf(final E element) { return internalMap.removeIf(element); }

    public void clear() { internalMap.clear(); }
}
