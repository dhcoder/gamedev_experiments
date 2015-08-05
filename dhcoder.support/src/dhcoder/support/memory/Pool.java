package dhcoder.support.memory;

import dhcoder.support.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import static dhcoder.support.collection.ListUtils.swapToEndAndRemove;
import static dhcoder.support.memory.ReflectionUtils.assertSame;
import static dhcoder.support.text.StringUtils.format;

/**
 * A class which manages a pool of pre-allocated objects so you can avoid thrashing Android's garbage collector when you
 * want to make lots of small, temporary allocations.
 * <p/>
 * Note: This class is appropriate for small pools or pools where you allocate temporaries which you then deallocate
 * in reverse order (like a stack). Use {@link HeapPool} for larger pools especially when you want to allocate and
 * return elements in random order.
 * <p/>
 * Pools are constructed with two callbacks, one which allocates a new instance of a class, and one which clears an
 * instance of a class for re-use later. After that, just call {@link #grabNew()} and {@link #free(Object)}, and this
 * class will take care of the rest!
 * <p/>
 * Be careful using pools. After you grab something from a pool, you have to remember to release it - and if anyone is
 * still holding on to that reference after you release it, that's an error - they will soon find the reference reset
 * underneath them.
 */
public final class Pool<T> {

    public interface AllocateMethod<T> {
        T run();
    }

    public interface ResetMethod<T> {
        void run(T item);
    }

    public static final int DEFAULT_CAPACITY = 10;
    /**
     * If true, run reflection sanity checks on the objects to make sure they were reset appropriately.
     * <p/>
     * This is done using {@link ReflectionUtils#assertSame(Object, Object)}, so you may wish to register your own
     * equality testers using {@link ReflectionUtils#registerEqualityTester(Class, ReflectionUtils.EqualityTester)} in
     * case you need to specify a special-case equals method when the default equals method isn't cutting it (for
     * example, if an equals method wasn't provided by a third party class).
     */
    public static boolean RUN_SANITY_CHECKS = false;

    public static <P extends Poolable> Pool<P> of(Class<P> poolableClass) {
        return of(poolableClass, DEFAULT_CAPACITY);
    }

    public static <P extends Poolable> Pool<P> of(final Class<P> poolableClass, int capacity) {
        return new Pool<P>(new AllocateMethod<P>() {
            ReflectionAllocator<P> reflectionAllocator = new ReflectionAllocator<P>(poolableClass);

            public P run() {
                return reflectionAllocator.allocate();
            }
        }, new ResetMethod<P>() {
            @Override
            public void run(P item) {
                item.reset();
            }
        }, capacity);
    }

    private final AllocateMethod<T> allocate;
    private final ResetMethod<T> reset;
    private final Stack<T> freeItems;
    private final ArrayList<T> itemsInUse;
    @Nullable private T referenceObject;
    private boolean resizable;
    private int capacity;
    private int maxCapacity;

    public Pool(AllocateMethod<T> allocate, ResetMethod<T> reset) {
        this(allocate, reset, DEFAULT_CAPACITY);
    }

    public Pool(AllocateMethod<T> allocate, ResetMethod<T> reset, int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException(format("Invalid pool capacity: {0}", capacity));
        }

        this.allocate = allocate;
        this.reset = reset;
        this.capacity = capacity;

        resizable = false;
        maxCapacity = capacity;

        freeItems = new Stack<T>();
        freeItems.ensureCapacity(capacity);
        itemsInUse = new ArrayList<T>(capacity);

        for (int i = 0; i < capacity; i++) {
            freeItems.push(allocate.run());
        }

        if (RUN_SANITY_CHECKS) {
            referenceObject = allocate.run(); // Allocate one extra object for reference
        }
    }

    public Pool makeResizable(int maxCapacity) {
        if (maxCapacity < capacity) {
            throw new IllegalArgumentException(
                format("Can't set pool's max capacity {0} smaller than its current capactiy {1}", maxCapacity,
                    capacity));
        }

        resizable = true;
        this.maxCapacity = maxCapacity;
        return this;
    }

    public int getCapacity() { return capacity; }

    public int getMaxCapacity() { return maxCapacity; }

    public List<T> getItemsInUse() {
        return itemsInUse;
    }

    public int getRemainingCount() { return freeItems.size(); }

    public T grabNew() {
        if (getRemainingCount() == 0) {

            if (!resizable || capacity == maxCapacity) {
                throw new IllegalStateException(
                    format("Requested too many items from this pool (capacity: {0}) - are you forgetting to free some?",
                        capacity));
            }

            int oldCapacity = capacity;
            capacity = Math.min(capacity * 2, maxCapacity);

            freeItems.ensureCapacity(capacity);
            itemsInUse.ensureCapacity(capacity);

            for (int i = oldCapacity; i < capacity; i++) {
                freeItems.push(allocate.run());
            }
        }

        T newItem = freeItems.pop();
        itemsInUse.add(newItem);

        return newItem;
    }

    public int mark() {
        return itemsInUse.size();
    }

    public void freeToMark(int mark) {
        freeCount(itemsInUse.size() - mark);
    }

    public void freeCount(int count) {
        int indexToFree = itemsInUse.size() - 1;
        for (int i = count - 1; i >= 0; --i) {
            T item = itemsInUse.get(indexToFree);
            returnItemToPool(item);
            itemsInUse.remove(indexToFree);
            indexToFree--;
        }
    }

    public void freeAll() {
        freeCount(itemsInUse.size());
    }

    public void free(T item) {
        swapToEndAndRemove(itemsInUse, item);
        returnItemToPool(item);
    }

    public void free(int itemIndex) {
        T item = swapToEndAndRemove(itemsInUse, itemIndex);
        returnItemToPool(item);
    }

    private void returnItemToPool(T item) {
        reset.run(item);
        freeItems.push(item);
        if (RUN_SANITY_CHECKS) {
            assert referenceObject != null;
            assertSame(referenceObject, item);
        }
    }

}
