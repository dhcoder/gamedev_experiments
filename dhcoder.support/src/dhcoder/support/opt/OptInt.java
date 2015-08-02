package dhcoder.support.opt;

import dhcoder.support.memory.Poolable;

import static dhcoder.support.text.StringUtils.format;

/**
 * Special-case {@link Opt} for the int primitive type (to avoid the allocation that comes from auto-boxing Ints)
 */
public final class OptInt implements Poolable {

    /**
     * Creates an Opt wrapper around a int value.
     */
    public static OptInt of(final int value) {
        return new OptInt(value);
    }

    /**
     * Creates an Opt which is initialized to no value.
     */
    public static OptInt withNoValue() {
        return new OptInt();
    }

    private int value;
    private boolean hasValue;

    /**
     * Create an optional without a value.
     * <p/>
     * Use {@link #withNoValue()} instead.
     */
    private OptInt() { hasValue = false; }

    /**
     * Create an optional with an initial value.
     * <p/>
     * Use {@link #of(int)} instead.
     */
    private OptInt(final int value) { set(value); }

    /**
     * Clears the value of this optional.
     */
    public void clear() {
        hasValue = false;
        value = 0; // Set to be safe - all cleared OptInts will have the same value
    }

    /**
     * Returns the current value of this optional, or throws an exception otherwise. You may consider checking {@link
     * #hasValue()} first before calling this method.
     *
     * @throws IllegalStateException if this optional doesn't currently have a value.
     */
    public int getValue() {
        if (!hasValue) {
            throw new IllegalStateException("Call to value() on a valueless optional.");
        }
        return value;
    }

    /**
     * Returns the current value of this optional or the specified default value if this optional has no value.
     */
    public int getValueOr(final int defaultValue) {
        return (hasValue ? value : defaultValue);
    }

    /**
     * Sets this optional to a new value.
     */
    public void set(final int value) {
        this.value = value;
        this.hasValue = true;
    }

    /**
     * Set the value of this optional to the value held by another optional (or no value if the target optional is also
     * valueless).
     */
    public void setFrom(final OptInt rhs) {
        this.hasValue = rhs.hasValue;
        this.value = rhs.value;
    }

    /**
     * Returns true if this optional currently has a value set.
     */
    public boolean hasValue() {
        return hasValue;
    }

    @Override
    public int hashCode() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }

        OptInt optInt = (OptInt)o;

        if (hasValue != optInt.hasValue) { return false; }
        if (optInt.value != value) { return false; }

        return true;
    }

    @Override
    public String toString() {
        if (!hasValue) { return "OptInt{}"; }

        return format("OptInt{{{0}}}", value);
    }

    @Override
    /**
     * Overridden from the {@link Poolable} interface, but otherwise use {@link #clear()} instead for readability.
     */
    public void reset() {
        clear();
    }
}
