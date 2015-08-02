package dhcoder.support.contract;

import java.util.Iterator;

/**
 * Simple collection of methods to assert expected values or else throw a {@link ContractException}.
 */
public final class ContractUtils {
    public static class ContractException extends RuntimeException {
        public ContractException(final String message) {
            super(message);
        }
    }

    public static void requireNull(final Object value, final String message) {
        if (value != null) {
            throw new ContractException(message);
        }
    }

    public static void requireNonNull(final Object value, final String message) {
        if (value == null) {
            throw new ContractException(message);
        }
    }

    public static void requireValue(final int expected, final int value, final String message) {
        if (value != expected) {
            throw new ContractException(message);
        }
    }

    public static void requireValue(final float expected, final float value, final String message) {
        if (value != expected) {
            throw new ContractException(message);
        }
    }

    public static void requireTrue(final boolean value, final String message) {
        if (value != true) {
            throw new ContractException(message);
        }
    }

    public static void requireFalse(final boolean value, final String message) {
        if (value != false) {
            throw new ContractException(message);
        }
    }

    public static void requireElements(final Iterable list, final String message) {
        final Iterator iterator = list.iterator();
        while (iterator.hasNext()) {
            requireNonNull(iterator.next(), message);
        }
    }
}
