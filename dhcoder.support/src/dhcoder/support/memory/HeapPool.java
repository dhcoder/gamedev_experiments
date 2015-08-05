package dhcoder.support.memory;

import dhcoder.support.collection.ArrayMap;

import java.util.List;

/**
 * A pool which is better than the base {@link Pool} at handling allocations and deallocations in any order,
 * especially when the pool in question is relatively large (more than dozens of elements).
 * <p/>
 * It works by mapping elements in the pool to their allocation index - meaning we can remove the element directly
 * instead of searching through the pool to find it.
 */
public final class HeapPool<T> {

    public static final int DEFAULT_CAPACITY = 200; // HeapPools should be relatively large

    public static <P extends Poolable> HeapPool<P> of(Class<P> poolableClass) {
        return new HeapPool<P>(Pool.of(poolableClass, DEFAULT_CAPACITY));
    }

    public static <P extends Poolable> HeapPool<P> of(Class<P> poolableClass, int capacity) {
        return new HeapPool<P>(Pool.of(poolableClass, capacity));
    }

    private final Pool<T> innerPool;
    private final ArrayMap<T, Integer> itemIndices;

    public HeapPool(Pool.AllocateMethod<T> allocate, Pool.ResetMethod<T> reset) {
        this(new Pool(allocate, reset));
    }

    public HeapPool(Pool.AllocateMethod<T> allocate, Pool.ResetMethod<T> reset, int capacity) {
        this(new Pool(allocate, reset, capacity));
    }

    private HeapPool(Pool<T> innerPool) {
        this.innerPool = innerPool;
        itemIndices = new ArrayMap<T, Integer>(innerPool.getCapacity());
    }

    public HeapPool makeResizable(int maxCapacity) {
        innerPool.makeResizable(maxCapacity);
        return this;
    }

    public int getCapacity() { return innerPool.getCapacity(); }

    public int getMaxCapacity() { return innerPool.getMaxCapacity(); }

    public List<T> getItemsInUse() {
        return innerPool.getItemsInUse();
    }

    public int getRemainingCount() { return innerPool.getRemainingCount(); }

    public T grabNew() {
        T item = innerPool.grabNew();
        itemIndices.put(item, IntegerCache.getFor(innerPool.getItemsInUse().size() - 1));
        return item;
    }

    public void free(T item) {
        int index = itemIndices.get(item).intValue();
        innerPool.free(index);

        itemIndices.remove(item);
        List<T> items = getItemsInUse();
        if (items.size() > index) {
            T movedItem = items.get(index); // An old item was moved to fill in the place of the removed item
            itemIndices.replace(movedItem, IntegerCache.getFor(index));
        }
    }

    public void freeAll() {
        innerPool.freeAll();
        itemIndices.clear();
    }
}
