package dhcoder.libgdx.assets.serialization;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Json;
import dhcoder.libgdx.assets.TileDatastore;
import dhcoder.libgdx.assets.TileGroup;
import dhcoder.libgdx.assets.Tileset;
import dhcoder.libgdx.assets.TilesetDatastore;

/**
 * Class that loads {@link TextureRegion}s into our {@link TileDatastore}.
 */
public final class TilesLoader {

    private final static class TileGroupData {
        public String tilesetPath;
        public TileData[] tiles;
        public RegionData[] regions;
    }

    private final static class TileData {
        public String name;
        public int[] coord;
    }

    private final static class RegionData {
        public String name;
        public int[][] coords;
    }

    public static void load(final Json json, final TilesetDatastore tilesets, final TileDatastore tiles,
        final String jsonPath) {

        final FileHandle fileHandle = Gdx.files.internal(jsonPath);
        TileGroupData groupData = json.fromJson(TileGroupData.class, fileHandle.readString());

        Tileset tileset = tilesets.get(groupData.tilesetPath);
        Texture tileTexture = tileset.getTexture();
        int tileW = tileset.getTileWidth();
        int tileH = tileset.getTileHeight();
        String groupName = fileHandle.nameWithoutExtension();

        TileGroup tileGroup = new TileGroup();
        for (int i = 0; i < groupData.tiles.length; ++i) {
            TileData tileData = groupData.tiles[i];
            tileGroup.add(tileData.name,
                new TextureRegion(tileTexture, tileData.coord[0] * tileW, tileData.coord[1] * tileH, tileW, tileH));
        }

        for (int i = 0; i < groupData.regions.length; ++i) {
            RegionData regionData = groupData.regions[i];
            tileGroup.add(regionData.name,
                new TextureRegion(tileTexture, regionData.coords[0][0], regionData.coords[0][1],
                    regionData.coords[1][0], regionData.coords[1][1]));
        }

        tiles.add(groupName, tileGroup);
    }

}
