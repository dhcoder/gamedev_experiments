package dhcoder.support.memory;

import java.util.ArrayList;

/**
 * TODO: HEADER COMMENT HERE.
 */
public final class IntegerCache {

    public static final int DEFAULT_CAPACITY = 200;

    private static ArrayList<Integer> PREALLOCATED_INDICES;

    static {
        PREALLOCATED_INDICES = new ArrayList<Integer>(DEFAULT_CAPACITY);
        for (int i = 0; i < DEFAULT_CAPACITY; i++) {
            PREALLOCATED_INDICES.add(new Integer(i));
        }
    }

    public static Integer getFor(final int index) {
        final int numIndices = PREALLOCATED_INDICES.size();
        if (numIndices < index) {
            PREALLOCATED_INDICES.ensureCapacity(index);
            for (int i = numIndices; i < index; i++) {
                PREALLOCATED_INDICES.add(new Integer(i));
            }
        }
        return PREALLOCATED_INDICES.get(index);
    }
}
