package dhcoder.support.math;

import static dhcoder.support.text.StringUtils.format;

public final class MathUtils {

    public static int log2(int value) {

        if (value < 0) {
            throw new IllegalArgumentException(format("Log2 must take a value >= 1. Got: {0}", value));
        }

        int valueCopy = value;
        int log2 = 0;
        while (valueCopy > 1) {
            valueCopy >>= 1;
            log2++;
        }

        if (!isPowerOfTwo(value)) {
            log2++; // Round up to the power of two ceiling. For example, 4 -> 4, 5 -> 8, 8 -> 8, 9 -> 16, etc.
        }
        return log2;
    }

    public static boolean isPowerOfTwo(int value) {
        // See http://stackoverflow.com/a/19383296/1299302
        return (value & (value - 1)) == 0;
    }

    public static int clamp(int value, int min, int max) {
        if (min > max) {
            throw new IllegalArgumentException(format("Called clamp with min < max (min: {0}, max: {1})", min, max));
        }
        return Math.max(min, Math.min(max, value));
    }

    public static float clamp(float value, float min, float max) {
        if (min > max) {
            throw new IllegalArgumentException(format("Called clamp with min < max (min: {0}, max: {1})", min, max));
        }
        return Math.max(min, Math.min(max, value));
    }

    private MathUtils() {} // Disabled constructor
}
