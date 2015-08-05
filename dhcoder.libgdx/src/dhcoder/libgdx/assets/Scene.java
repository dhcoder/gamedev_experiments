package dhcoder.libgdx.assets;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import dhcoder.libgdx.render.Renderable;
import dhcoder.support.contract.ContractUtils;

import static dhcoder.support.text.StringUtils.format;

/**
 * Data that describes a scene, which is essentially a self-contained area full of ground tiles and entities.
 */
public final class Scene implements Renderable {

    public static boolean RUN_SANITY_CHECKS;

    // TODO: Allow multiple tilesets and animated tiles
    private final Tileset tileset;
    private final Array<TextureRegion> groundTiles;
    private final int numCols;
    private final int numRows;
    private final Vector2 offset = new Vector2();
    private boolean ranSanityChecks;

    public Scene(Tileset tileset, int numCols, int numRows, float offsetX,
                 float offsetY) {
        this.tileset = tileset;
        this.numCols = numCols;
        this.numRows = numRows;
        offset.x = offsetX;
        offset.y = offsetY;

        if (numCols <= 0 || numRows <= 0) {
            throw new IllegalArgumentException("Scene must have rows and cols set to 1 or more");
        }

        int numRegions = numCols * numRows;
        groundTiles = new Array<TextureRegion>(numRegions);
        for (int i = 0; i < numRegions; ++i) {
            groundTiles.add(null);
        }
    }

    public float getWidth() {
        return numCols * tileset.getTileWidth();
    }

    public float getHeight() {
        return numRows * tileset.getTileHeight();
    }

    public float getLeftX() {
        return -(getWidth() / 2f) + offset.x;
    }

    public float getBottomY() {
        return -(getHeight() / 2f) + offset.y;
    }

    public float getRightX() {
        return getLeftX() + getWidth();
    }

    public float getTopY() {
        return getBottomY() + getHeight();
    }

    public void setTile(int tileX, int tileY, TextureRegion tile) {
        if (tileX >= numCols || tileY >= numRows) {
            throw new IllegalArgumentException(
                format("Invalid tile coordinates {0}x{1} (scene is {2}x{3})", tileX, tileY, numCols, numRows));
        }

        int index = tileY * numCols + tileX;
        groundTiles.set(index, tile);

        ranSanityChecks = false;
    }

    public void setTile(int tileIndex, TextureRegion tile) {
        int tileX = tileIndex % numCols;
        int tileY = tileIndex / numCols;
        setTile(tileX, tileY, tile);
    }

    @Override
    public void render(Batch batch) {
        if (RUN_SANITY_CHECKS && !ranSanityChecks) {
            ContractUtils.requireElements(groundTiles, "All scene tiles must be set");
            ranSanityChecks = true;
        }

        float tileWidth = tileset.getTileWidth();
        float tileHeight = tileset.getTileHeight();
        int tileX = 0;
        float leftX = getLeftX();
        float topY = getTopY();
        float renderAtX = leftX;
        float renderAtY = topY - tileHeight;
        for (int i = 0; i < groundTiles.size; i++) {
            TextureRegion tile = groundTiles.get(i);
            batch.draw(tile, renderAtX, renderAtY);

            tileX++;
            renderAtX += tileWidth;
            if (tileX == numCols) {
                tileX = 0;
                renderAtX = leftX;
                renderAtY -= tileHeight;
            }
        }
    }

    @Override
    public float getZ() {
        return 0f;
    }
}
