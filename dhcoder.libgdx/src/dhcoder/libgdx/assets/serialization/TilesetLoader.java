package dhcoder.libgdx.assets.serialization;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Json;
import dhcoder.libgdx.assets.ImageDatastore;
import dhcoder.libgdx.assets.Tileset;
import dhcoder.libgdx.assets.TilesetDatastore;

/**
 * Class that serializes / deserializes {@link Tileset}s
 */
public final class TilesetLoader {

    private final static class TilesetData {
        public String imagePath;
        public int tileWidth;
        public int tileHeight;
    }

    public static void load(Json json, ImageDatastore images, TilesetDatastore tilesets,
                            String jsonPath) {
        TilesetData data = json.fromJson(TilesetData.class, Gdx.files.internal(jsonPath).readString());

        tilesets.add(jsonPath, new Tileset(images.get(data.imagePath), data.tileWidth, data.tileHeight));
    }

}
