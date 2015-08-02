package dhcoder.libgdx.assets;

import dhcoder.support.collection.ArrayMap;

/**
 * A collection of named assets. This class is abstract so you can inherit from it, thereby giving it a concrete name.
 */
public abstract class AssetGroup<T> {

    ArrayMap<String, T> assets = new ArrayMap<String, T>();

    public void add(final String name, final T asset) {
        assets.put(name, asset);
    }

    public T get(final String name) {
        return assets.get(name);
    }
}
