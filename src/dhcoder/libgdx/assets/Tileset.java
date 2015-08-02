package dhcoder.libgdx.assets;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;

import static dhcoder.support.text.StringUtils.format;

/**
 * A 2d collection of tiles.
 */
public final class Tileset {
    private final Texture texture;
    private final Array<TextureRegion> textureRegions;
    private final int tileWidth;
    private final int tileHeight;
    private final int numRows;
    private final int numCols;

    public Tileset(final Texture texture, final int tileWidth, final int tileHeight) {
        this.texture = texture;
        this.tileWidth = tileWidth;
        this.tileHeight = tileHeight;
        numCols = texture.getWidth() / tileWidth;
        numRows = texture.getHeight() / tileHeight;

        int numRegions = numRows * numCols;
        textureRegions = new Array<TextureRegion>(numRegions);
        for (int i = 0; i < numRegions; ++i) {
            textureRegions.add(null);
        }
    }

    public Texture getTexture() {
        return texture;
    }

    public int getTileWidth() {
        return tileWidth;
    }

    public int getTileHeight() {
        return tileHeight;
    }

    public TextureRegion getTile(final int tileX, final int tileY) {
        if (tileX >= numCols || tileY >= numRows) {
            throw new IllegalArgumentException(
                format("Invalid tile coordinates {0}x{1} (tileset is {2}x{3})", tileX, tileY, numCols, numRows));
        }

        // Lazily instatiate tiles on request
        int index = tileY * numCols + tileX;
        TextureRegion tile = textureRegions.get(index);
        if (tile == null) {
            tile = new TextureRegion(texture, tileX * tileWidth, tileY * tileHeight, tileWidth, tileHeight);
            textureRegions.set(index, tile);
        }

        return tile;
    }

    public TextureRegion getTile(final int tileIndex) {
        int tileX = tileIndex % numCols;
        int tileY = tileIndex / numCols;
        return getTile(tileX, tileY);
    }
}
