package dhcoder.libgdx.assets;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import dhcoder.support.collection.ArrayMap;

import java.util.List;

/**
 * A named collection of all {@link Texture}s loaded so far for this game.
 */
public final class ImageDatastore {

    ArrayMap<String, Texture> images = new ArrayMap<String, Texture>();

    public Texture get(String path) {
        Texture texture = images.getOrNull(path);
        if (texture == null) {
            texture = new Texture(Gdx.files.internal(path));
            images.put(path, texture);
        }

        return texture;
    }

    public void dispose() {
        List<Texture> values = images.getValues();
        int numValues = values.size();
        for (int i = 0; i < numValues; ++i) {
            values.get(i).dispose();
        }
        images.clear();
    }
}
