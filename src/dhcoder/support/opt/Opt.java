package dhcoder.support.opt;

import dhcoder.support.memory.Poolable;

import static dhcoder.support.text.StringUtils.format;

/**
 * A class which represents an nullable value - that is, like a reference, it either has a value or not. Unlike a
 * reference, if an Opt doesn't have a value and you try to access it, it will throw an {@link IllegalStateException}.
 * <p/>
 * The practice encouraged by this class is to never pass around nulls directly - instead,
 * assume a reference always indicates a non-null value. If you have a method that needs to sometimes return null,
 * or perhaps take in argument where it makes sense for it to sometime be null, use this class instead. It marks the
 * use-case explicitly and forces good habits in the programmer by having them explicitly check for the existence of
 * a value before using it.
 * <p/>
 * You cannot create optionals directly. Instead, you should use Opt's helper static methods to do so. The reason for
 * this approach is it is easier to read and uses less characters than writing
 * {@code new Opt<SomeType>(SomeTypeInstance)} out each time.
 */
public final class Opt<T> implements Poolable {

    /**
     * Creates an Opt wrapper around a non-null value.
     *
     * @throws IllegalArgumentException if value is null.
     */
    public static <T> Opt<T> of(final T value) throws IllegalArgumentException {
        if (value == null) {
            throw new IllegalArgumentException("Can't call opt with null value - use 'ofNullable' instead.");
        }
        return new Opt<T>(value);
    }

    /**
     * Creates a new optional type.
     */
    public static <T> Opt<T> ofNullable(final T value) {
        return new Opt<T>(value);
    }

    /**
     * Creates an Opt which is initialized to no value.
     */
    public static <T> Opt<T> withNoValue() {
        return new Opt<T>();
    }

    private T value;

    /**
     * Create an optional without a value.
     * <p/>
     * Use {@link #withNoValue()} instead.
     */
    private Opt() {
        // Intentionally doing nothing leaves this.value null.
    }

    /**
     * Create an optional with an initial value.
     * <p/>
     * Use {@link #of(Object)} or {@link #ofNullable(Object)} instead.
     */
    private Opt(final T value) { set(value); }

    /**
     * Clears the value of this optional.
     */
    public void clear() { this.value = null; }

    /**
     * Returns the current value of this optional, or throws an exception otherwise. You may consider checking {@link
     * #hasValue()} first before calling this method.
     *
     * @throws IllegalStateException if this optional doesn't currently have a value.
     */
    public T getValue() {
        if (value == null) {
            throw new IllegalStateException("Call to value() on a valueless optional.");
        }
        return value;
    }

    /**
     * Sets this optional to a new value. It is incorrect to set this with null. Use {@link #clear()} instead. This
     * ensures users are clearing an optional intentionally, not by accident.
     */
    public void set(final T value) {
        if (value == null) {
            throw new IllegalArgumentException("Cannot set an optional to a null value. Use clear() instead");
        }
        this.value = value;
    }

    /**
     * Set the value of this optional to the value held by another optional (or no value if the target optional is also
     * valueless).
     */
    public void setFrom(final Opt<T> rhs) { this.value = rhs.value; }

    /**
     * Returns true if this optional currently has a value set.
     */
    public boolean hasValue() { return value != null; }

    @Override
    public int hashCode() { return value != null ? value.hashCode() : 0; }

    @Override
    public boolean equals(final Object o) {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }
        Opt rhs = (Opt)o;
        if (value != null ? !value.equals(rhs.value) : rhs.value != null) { return false; }
        return true;
    }

    @Override
    public String toString() {
        if (value == null) { return "Opt<?>{}"; }

        return format("Opt<{0}>{{{1}}}", value.getClass().getSimpleName(), value);
    }

    @Override
    /**
     * Overridden from the {@link Poolable} interface, but otherwise use {@link #clear()} instead for readability.
     */
    public void reset() {
        clear();
    }
}
