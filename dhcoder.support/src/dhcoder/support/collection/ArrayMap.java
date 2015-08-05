package dhcoder.support.collection;

import dhcoder.support.annotations.NotNull;
import dhcoder.support.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static dhcoder.support.math.MathUtils.log2;
import static dhcoder.support.text.StringUtils.format;

/**
 * A map implementation that uses {@link ArrayList}s under the hood, allowing it to preallocate all memory ahead of
 * time (unless the collection needs to grow.
 */
public final class ArrayMap<K, V> {

    public enum InsertMethod {
        PUT,
        REPLACE,
    }

    private enum IndexMethod {
        GET,
        PUT,
    }

    // See: http://planetmath.org/goodhashtableprimes
    private static final int[] PRIME_TABLE_SIZES = new int[]{
        3, // 2^0 -- Ex: If requested size is 0-1, actually use 3.
        3, // 2^1 -- Ex: If requested size is 1-2, actually use 3.
        7, // 2^2 -- Ex: If requested size is 3-4, actually use 7.
        13, // 2^3 -- Ex: If requested size is 5-8, actually use 13. etc...
        23, // 2^4
        53, // 2^5
        97, // 2^6
        193, // 2^7
        389, // 2^8
        769, // 2^9
        1543, // 2^10
        3079, // 2^11
        6151, // 2^12
        12289, // 2^13
        24593, // 2^14
        49157, // 2^15
        98317, // 2^16
        196613, // 2^17
        393241, // 2^18
        786433, // 2^19
        1572869, // 2^20
        3145739, // 2^21
        6291469, // 2^22
        12582917, // 2^23
        25165843, // 2^24
        50331653, // 2^25
        100663319, // 2^26
        201326611, // 2^27
        402653189, // 2^28
        805306457, // 2^29
        1610612741, // 2^30
    };
    private static final int DEFAULT_EXPECTED_SIZE = 10;
    private static final float DEFAULT_LOAD_FACTOR = 0.75f;
    public static boolean RUN_SANITY_CHECKS = false;

    private static int getNextPrimeSize(int requestedSize) {
        int log2 = log2(requestedSize);
        if (log2 >= PRIME_TABLE_SIZES.length) {
            throw new IllegalStateException("Table can't grow big enough to accomodate requested size");
        }

        return PRIME_TABLE_SIZES[log2];
    }

    private final float loadFactor;
    private int size;
    private int resizeAtSize;
    private int capacity;
    // We use a probing algorithm to find a key's index, jumping over spots that are already taken to find a free
    // bucket. But if a key/value is removed, that leaves a hold that future searches for our key may fall in to. We
    // mark the spot so that, even if a key was removed, we know it used to be there.
    private boolean[] keyIsDead;
    private ArrayList<K> keys;
    private ArrayList<V> values;

    public ArrayMap() {
        this(DEFAULT_EXPECTED_SIZE, DEFAULT_LOAD_FACTOR);
    }

    public ArrayMap(int expectedSize) {
        this(expectedSize, DEFAULT_LOAD_FACTOR);
    }

    /**
     * Create a map with an expected size and load factor. The load factor dictates how full a hashtable should get
     * before it resizes. A load factor of 0.5 means the table should resize when it is 50% full.
     *
     * @throws IllegalArgumentException if the input load factor is not between 0 and 1.
     */
    public ArrayMap(int expectedSize, float loadFactor) {
        if (loadFactor <= 0.0f || loadFactor >= 1.0f) {
            throw new IllegalArgumentException(format("Load factor must be between 0 and 1. Got {0}", loadFactor));
        }

        this.loadFactor = loadFactor;

        capacity = getNextPrimeSize(expectedSize);

        // Ensure initial capacity to be at least large enough so we don't resize unless user puts more keys in than
        // they said they would. Note: adding 0.5f rounds up so we don't accidentally truncate our needed capacity by 1
        int enoughInitialCapacity = (int)(expectedSize / loadFactor + 0.5f);
        while (capacity < enoughInitialCapacity) {
            capacity = getNextPrimeSize(capacity + 1);
        }

        initializeStructures();
    }

    public int getSize() {
        return size;
    }

    /**
     * Returns the potential capacity of this ArrayMap. Note that an ArrayMap will get resized before max capacity is
     * reached, depending on its load factor, so don't expect to fill one completely.
     */
    public int getCapacity() {
        return capacity;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    /**
     * Note: This method allocates an array and should only be used in non-critical areas.
     */
    public List<K> getKeys() {
        ArrayList<K> compactKeys = new ArrayList<K>(size);
        for (int i = 0; i < capacity; ++i) {
            K key = keys.get(i);
            if (key != null) {
                compactKeys.add(key);
            }
        }
        return compactKeys;
    }

    /**
     * Note: This method allocates an array and should only be used in non-critical areas.
     */
    public List<V> getValues() {
        ArrayList<V> compactValues = new ArrayList<V>(size);
        for (int i = 0; i < capacity; ++i) {
            V value = values.get(i);
            if (value != null) {
                compactValues.add(value);
            }
        }
        return compactValues;
    }

    public boolean containsKey(K key) {
        int index = getIndex(key, IndexMethod.GET);
        return index >= 0;
    }

    /**
     * Gets the value associated with the passed in key, or throws an exception if that key is not registered.
     *
     * @throws IllegalArgumentException if no value is associated with the passed in key.
     */
    @NotNull
    public V get(K key) {
        int index = getIndex(key, IndexMethod.GET);
        if (index < 0) {
            throw new IllegalArgumentException(format("No value associated with key {0}", key));
        }

        return values.get(index);
    }

    /**
     * This method is functionally equivalent to but more efficient than using {@link #containsKey(Object)} followed
     * by {@link #get(Object)}.
     */
    @Nullable
    public V getOrNull(K key) {
        int index = getIndex(key, IndexMethod.GET);
        return (index >= 0) ? values.get(index) : null;
    }

    /**
     * Put the key in the map. For efficiency, it is an error to call this method with the same key twice (without first
     * removing it) - this will not simply remove the existing key and replace its value. Use {@link #replace(Object,
     * Object)} if that's the behavior you desire, or use {@link #putOrReplace(Object, Object)} if you're not sure if
     * the key exists at all.
     * <p/>
     * This may seem like overly strict behavior for a put method, but the goal here is to allow users of the class to
     * enforce expected behavior on how their map is being used - sometimes, it is simply an error to trounce one key's
     * value with another, and doing so without realizing it leads to the sort of bug that is really hard to find later.
     * <p/>
     * NOTE: Although it is an error condition to use the same key twice in a row, for performance reasons, this method
     * won't report it unless {@link #RUN_SANITY_CHECKS} is set to {@code true}.
     */
    public void put(K key, V value) {

        if (RUN_SANITY_CHECKS) {
            int numKeys = keys.size();
            for (int i = 0; i < numKeys; i++) {
                if (keys.get(i) == key) {
                    throw new IllegalArgumentException(
                        "Attempt to add the same key a second time. Are you re-using the same key accidentally?");
                }
            }
        }

        int index = getIndex(key, IndexMethod.PUT);
        setInternal(index, key, value);

        size++;

        if (size == resizeAtSize) {
            increaseCapacity();
        }
    }

    /**
     * Use if you know for sure the key is already in the map. This is a bit more efficient than using {@link #remove
     * (Object)} followed by {@link #put(Object, Object)}
     *
     * @throws IllegalStateException if the key you want to replace doesn't already exist in the map.
     */
    public void replace(K key, V value) {
        int index = getIndex(key, IndexMethod.GET);
        if (index < 0) {
            throw new IllegalStateException(format("No value associated with key {0}", key));
        }

        setInternal(index, key, value);
    }

    /**
     * Use this if you're not sure if the key is already in the map or not. This is less efficient than either using
     * {@link #put(Object, Object)} or {@link #replace(Object, Object)} but is useful if you simply don't care.
     */
    public InsertMethod putOrReplace(K key, V value) {
        int index = getIndex(key, IndexMethod.GET);

        if (index >= 0) {
            setInternal(index, key, value);
            return InsertMethod.REPLACE;
        }
        else {
            put(key, value);
            return InsertMethod.PUT;
        }
    }

    /**
     * Remove the value associated with the passed in key. The key MUST be in the map because this method needs to
     * return a non-null value. If you are not sure if the key is in the map, you can instead use
     * {@link #removeOrNull(Object)}, or if you don't care about the return value, you can use
     * {@link #removeIf(Object)}.
     *
     * @throws IllegalArgumentException if the key is not found in the map.
     */
    @NotNull
    public V remove(K key) {
        V value = removeOrNull(key);
        if (value == null) {
            throw new IllegalArgumentException(format("No value associated with key {0}", key));
        }

        return value;
    }

    /**
     * Remove method with a simpler interface because it doesn't care about the return value.
     *
     * @return {@code true} if the key was in the map.
     */
    public boolean removeIf(K key) {
        V value = removeOrNull(key);
        return (value != null);
    }

    /**
     * Like {@link #remove(Object)} but can handle the case of the key not being found in the map.
     */
    @Nullable
    public V removeOrNull(K key) {
        int index = getIndex(key, IndexMethod.GET);
        if (index < 0) {
            return null;
        }

        keyIsDead[index] = true;
        V value = values.get(index);
        values.set(index, null);
        keys.set(index, null);

        size--;

        return value;
    }

    public void clear() {
        for (int i = 0; i < capacity; i++) {
            keyIsDead[i] = false;
            keys.set(i, null);
            values.set(i, null);
        }

        size = 0;
    }

    private void setInternal(int index, K key, V value) {
        keys.set(index, key);
        values.set(index, value);
        keyIsDead[index] = false;
    }

    private void increaseCapacity() {
        int oldCapacity = capacity;
        capacity = getNextPrimeSize(capacity + 1);

        ArrayList<K> keysCopy = keys;
        ArrayList<V> valuesCopy = values;

        initializeStructures();

        for (int i = 0; i < oldCapacity; ++i) {
            if (keysCopy.get(i) != null) {
                K key = keysCopy.get(i);
                V value = valuesCopy.get(i);

                put(key, value);
            }
        }
    }

    private void initializeStructures() {
        keyIsDead = new boolean[capacity];
        keys = new ArrayList<K>(capacity);
        values = new ArrayList<V>(capacity);

        size = 0;
        for (int i = 0; i < capacity; ++i) {
            keys.add(null);
            values.add(null);
        }

        resizeAtSize = (int)(capacity * loadFactor);
    }

    // Returns index of the key in this hashtable. If 'forGetMethods' is true, 'outIndex' won't have any value set if
    // the key couldn't be found.

    /**
     * Returns the index of the key in this table's inner array, or -1 if not found.
     * @param indexMethod Whether we are querying an index for retrieval or insertion. If insertion, -1 will never be
     *                    returned.
     */
    private int getIndex(K key, IndexMethod indexMethod) {
        int positiveHashCode = key.hashCode() & 0x7FFFFFFF;
        int initialIndex = positiveHashCode % capacity;
        int index = initialIndex;
        int loopCount = 1;
        while ((keys.get(index) != null || keyIsDead[index]) && loopCount <= capacity) {
            if (indexMethod == IndexMethod.PUT && keyIsDead[index]) {
                // This used to be a bucket for a key that got removed, so it's free for reuse!
                return index;
            }

            if (key.equals(keys.get(index))) {
                return index;
            }

            index = (initialIndex + loopCount * loopCount) % capacity; // Quadratic probing
            loopCount++;
        }

        if (indexMethod == IndexMethod.PUT) {
            return index;
        }
        else {
            return -1;
        }
    }
}
