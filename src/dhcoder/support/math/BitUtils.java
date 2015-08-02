package dhcoder.support.math;

import static dhcoder.support.contract.ContractUtils.requireTrue;
import static dhcoder.support.text.StringUtils.format;

public final class BitUtils {

    public static void requireSingleBit(final int value) {
        requireTrue(hasSingleBit(value), "Passed in value should only have a single bit set");
    }

    /**
     * Quick check to see if the passed in value only has a single bit set.
     */
    public static boolean hasSingleBit(final int value) {
        // See http://stackoverflow.com/a/12483864
        return ((value != 0) && ((value & (value - 1)) == 0));
    }

    public static int getBitIndex(final int value) {
        requireSingleBit(value);
        for (int i = 0; i < 32; ++i) {
            if ((value & (1 << i)) != 0) {
                return i;
            }
        }
        throw new IllegalArgumentException(format("Unexpected value {0} passed into getBitIndex", value));
    }

    private BitUtils() {} // Disabled constructor
}
