package dhcoder.support.memory;

/**
 * Interface which marks a class as designed intentionally to work with a {@link Pool}.
 * <p/>
 * Poolable subclasses are expected to provide a default constructor (either explicitly or by specifying no constructors
 * at all). The constructor can be private, if there are API concerns - {@link Pool} can still work with that.
 */
public interface Poolable {
    void reset();
}
