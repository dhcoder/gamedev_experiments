package dhcoder.libgdx.assets;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import dhcoder.support.collection.ArrayMap;
import dhcoder.support.memory.Pool;
import dhcoder.support.opt.Opt;

import java.util.List;

/**
 * A named collection of all {@link Texture}s loaded so far for this game.
 */
public final class ImageDatastore {

    ArrayMap<String, Texture> images = new ArrayMap<String, Texture>();
    Pool<Opt> optPool = Pool.of(Opt.class, 1);

    public Texture get(final String path) {
        final Opt<Texture> textureOpt = optPool.grabNew();
        images.get(path, textureOpt);
        Texture texture;
        if (!textureOpt.hasValue()) {
            texture = new Texture(Gdx.files.internal(path));
            images.put(path, texture);
        }
        else {
            texture = textureOpt.getValue();
        }

        optPool.freeCount(1);

        return texture;
    }

    public void dispose() {
        final List<Texture> values = images.getValues();
        final int numValues = values.size();
        for (int i = 0; i < numValues; ++i) {
            values.get(i).dispose();
        }
        images.clear();
    }
}
