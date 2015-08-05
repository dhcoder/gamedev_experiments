package dhcoder.support.collection;

import java.util.List;

public final class ListUtils {

    /**
     * Take the current item and swap it to the end of the list before removing it. This is a useful operation
     * because removing something from the end of a list is often a much more lightweight operation than removing
     * from the middle.
     *
     * @throws IllegalArgumentException if the specified item is not in the list.
     */
    public static <T> void swapToEndAndRemove(List<T> list, T item) {
        int itemIndex = -1;
        int listSize = list.size();
        for (int i = 0; i < listSize; ++i) {
            if (list.get(i) == item) { // Use == and not indexOf so we get instance equality, not value equality
                itemIndex = i;
                break;
            }
        }

        swapToEndAndRemove(list, itemIndex);
    }

    /**
     * Like {@link #swapToEndAndRemove(List, Object)} but where the list index is known.
     */
    public static <T> T swapToEndAndRemove(List<T> list, int itemIndex) {
        int listSize = list.size();
        if (itemIndex < 0 || itemIndex >= listSize) {
            throw new IllegalArgumentException("Trying to remove an item that's not in the list");
        }

        // Swap item to the end before removing (since removing from the end avoids shifting elements)
        int lastIndex = listSize - 1;
        if (itemIndex != lastIndex) {
            T temp = list.get(lastIndex);
            list.set(lastIndex, list.get(itemIndex));
            list.set(itemIndex, temp);
        }

        return list.remove(lastIndex);
    }
}
