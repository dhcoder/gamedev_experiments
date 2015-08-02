package dhcoder.support.opt;

import dhcoder.support.memory.Poolable;

import static dhcoder.support.text.StringUtils.format;

/**
 * Special-case {@link Opt} for the float primitive type (to avoid the allocation that comes from auto-boxing Floats)
 */
public final class OptFloat implements Poolable {

    /**
     * Creates an Opt wrapper around a float value.
     */
    public static OptFloat of(final float value) {
        return new OptFloat(value);
    }

    /**
     * Creates an Opt which is initialized to no value.
     */
    public static OptFloat withNoValue() {
        return new OptFloat();
    }

    private float value;
    private boolean hasValue;

    /**
     * Create an optional without a value.
     * <p/>
     * Use {@link #withNoValue()} instead.
     */
    private OptFloat() { hasValue = false; }

    /**
     * Create an optional with an initial value.
     * <p/>
     * Use {@link #of(float)} instead.
     */
    private OptFloat(final float value) { set(value); }

    /**
     * Clears the value of this optional.
     */
    public void clear() {
        hasValue = false;
        value = 0f; // Set to be safe - all cleared OptFloats will have the same value
    }

    /**
     * Returns the current value of this optional, or throws an exception otherwise. You may consider checking {@link
     * #hasValue()} first before calling this method.
     *
     * @throws IllegalStateException if this optional doesn't currently have a value.
     */
    public float getValue() {
        if (!hasValue) {
            throw new IllegalStateException("Call to value() on a valueless optional.");
        }
        return value;
    }

    /**
     * Returns the current value of this optional or the specified default value if this optional has no value.
     */
    public float getValueOr(final float defaultValue) {
        return (hasValue ? value : defaultValue);
    }

    /**
     * Sets this optional to a new value.
     */
    public void set(final float value) {
        this.value = value;
        this.hasValue = true;
    }

    /**
     * Set the value of this optional to the value held by another optional (or no value if the target optional is also
     * valueless).
     */
    public void setFrom(final OptFloat rhs) {
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
        return hasValue ? Float.floatToIntBits(value) : 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }

        OptFloat optFloat = (OptFloat)o;

        if (hasValue != optFloat.hasValue) { return false; }
        if (Float.compare(optFloat.value, value) != 0) { return false; }

        return true;
    }

    @Override
    public String toString() {
        if (!hasValue) { return "OptFloat{}"; }

        return format("OptFloat{{{0}}}", value);
    }

    @Override
    /**
     * Overridden from the {@link Poolable} interface, but otherwise use {@link #clear()} instead for readability.
     */
    public void reset() {
        clear();
    }
}
