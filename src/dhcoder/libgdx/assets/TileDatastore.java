package dhcoder.libgdx.assets;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

/**
 * A collection of tile groups.
 */
public final class TileDatastore extends AssetGroup<TileGroup> {

    /**
     * Convenience method to grab a tile out from a subgroup directly.
     */
    public TextureRegion get(final String group, final String name) {
        return get(group).get(name);
    }
}
