package dhcoder.libgdx.collection;

import com.badlogic.gdx.utils.Array;

import java.util.ArrayList;

/**
 * Collection utils for working with Libgdx collections.
 */
public final class CollectionUtils {
    public static <E> ArrayList<E> toArrayList(Array<E> array) {
        ArrayList<E> arrayList = new ArrayList<E>(array.size);
        for (E e : array) {
            arrayList.add(e);
        }
        return arrayList;
    }
}
