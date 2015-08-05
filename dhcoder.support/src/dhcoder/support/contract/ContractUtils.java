package dhcoder.support.contract;

import java.util.Iterator;

/**
 * Simple collection of methods to assert expected values or else throw a {@link ContractException}.
 */
public final class ContractUtils {
    public static class ContractException extends RuntimeException {
        public ContractException(String message) {
            super(message);
        }
    }

    public static void requireNull(Object value, String message) {
        if (value != null) {
            throw new ContractException(message);
        }
    }

    public static void requireNonNull(Object value, String message) {
        if (value == null) {
            throw new ContractException(message);
        }
    }

    public static void requireValue(int expected, int value, String message) {
        if (value != expected) {
            throw new ContractException(message);
        }
    }

    public static void requireValue(float expected, float value, String message) {
        if (value != expected) {
            throw new ContractException(message);
        }
    }

    public static void requireTrue(boolean value, String message) {
        if (value != true) {
            throw new ContractException(message);
        }
    }

    public static void requireFalse(boolean value, String message) {
        if (value != false) {
            throw new ContractException(message);
        }
    }

    public static void requireElements(Iterable list, String message) {
        Iterator iterator = list.iterator();
        while (iterator.hasNext()) {
            requireNonNull(iterator.next(), message);
        }
    }
}
