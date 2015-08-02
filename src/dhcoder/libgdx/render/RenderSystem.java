package dhcoder.libgdx.render;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import dhcoder.libgdx.entity.Entity;

import java.util.Comparator;

/**
 * System that renders a bunch of renderable components.
 * <p/>
 * This system depends on libgdx components and should be disposed when the program exits.
 */
public final class RenderSystem {

    public class Layer {
        private final Array<Renderable> renderables;
        private boolean isActive;

        public boolean isActive() {
            return isActive;
        }

        public void setActive(final boolean isActive) {
            this.isActive = isActive;
        }

        private boolean useBlending;
        private boolean isSorted;
        private boolean isUiLayer;

        public Layer(final int capacity) {
            renderables = new Array<Renderable>(capacity);
            useBlending = true;
            isActive = true;
            isSorted = true;
        }

        /**
         * Set whether this layer should be sorted before rendering. Sprites will be sorted by z-order in this case.
         * Defaults to {@code true}.
         */
        public Layer setSorted(final boolean isSorted) {
            this.isSorted = isSorted;
            return this;
        }

        /**
         * Set whether this layer should ignore the render offset. This is essentially useful for the UI layer which
         * always renders to an absolute position regardless of how much the game world has translated. Defaults to
         * {@code false}.
         */
        public Layer setUiLayer(final boolean isUiLayer) {
            this.isUiLayer = isUiLayer;
            return this;
        }

        /**
         * Set whether this layer supports blending. If you know all objects in this layer are opaque, you should set
         * this {@code false} and it may improve performance. Defaults to {@code true}.
         */
        public Layer setBlending(final boolean useBlending) {
            this.useBlending = useBlending;
            return this;
        }

        void add(final Renderable renderable) {
            renderables.add(renderable);
        }

        void remove(final Renderable renderable) {
            renderables.removeValue(renderable, true);
        }

        void render() {

            int numSprites = renderables.size;
            if (!isActive || numSprites == 0) {
                return;
            }

            if (isSorted) {
                renderables.sort(Z_SORT);
            }

            if (useBlending) {
                spriteBatch.enableBlending();
            }

            final Camera activeCamera = isUiLayer ? uiCamera : worldCamera;
            spriteBatch.setProjectionMatrix(activeCamera.combined);

            spriteBatch.begin();

            for (int i = 0; i < numSprites; i++) {
                Renderable renderable = renderables.get(i);
                renderable.render(spriteBatch);
            }

            spriteBatch.end();

            if (useBlending) {
                spriteBatch.disableBlending();
            }
        }
    }

    private static final Comparator<Renderable> Z_SORT = new Comparator<Renderable>() {
        @Override
        public int compare(final Renderable r1, final Renderable r2) {
            return Float.compare(r1.getZ(), r2.getZ());
        }
    };
    private final SpriteBatch spriteBatch;
    private final OrthographicCamera worldCamera;
    private final OrthographicCamera uiCamera;
    private final Array<Layer> renderLayers;
    private boolean cameraNeedsUpdate;

    public RenderSystem(final float viewportWidth, final float viewportHeight, final int batchSize) {
        spriteBatch = new SpriteBatch(batchSize);
        worldCamera = new OrthographicCamera(viewportWidth, viewportHeight);
        uiCamera = new OrthographicCamera(viewportWidth, viewportHeight);
        renderLayers = new Array<Layer>(4);

        uiCamera.update();
        worldCamera.update();
        cameraNeedsUpdate = false;
    }

    public OrthographicCamera getCamera() {
        return worldCamera;
    }

    public Layer addLayer(final int capacity) {
        Layer layer = new Layer(capacity);
        renderLayers.add(layer);
        return layer;
    }

    /**
     * Add a renderable which will get drawn by {@link #render}
     */
    public void add(final Enum layer, final Renderable renderable) {
        renderLayers.get(layer.ordinal()).add(renderable);
    }

    /**
     * Remove a renderable added by {@link #add(Enum, Renderable)}
     */
    public void remove(final Enum layer, final Renderable renderable) {
        renderLayers.get(layer.ordinal()).remove(renderable);
    }

    /**
     * Offset the world camera, shifting any non-UI layer.
     */
    public void setOffset(final Vector2 offset) {
        if (!worldCamera.position.epsilonEquals(offset.x, offset.y, 0f, 0f)) {
            worldCamera.position.set(offset, 0f);
            cameraNeedsUpdate = true;
        }
    }

    /**
     * Prepare the render system. This should be called after all {@link Entity} objects have a chance to update but
     * before the {@link #render} is called.
     */
    public void update() {
        if (cameraNeedsUpdate) {
            worldCamera.update();
        }
    }

    /**
     * Render all layers, from bottom (first layer added) to top.
     */
    public void render() {
        for (int i = 0; i < renderLayers.size; i++) {
            renderLayers.get(i).render();
        }
    }

    /**
     * Release all libgdx resources used by this class.
     */
    public void dispose() {
        spriteBatch.dispose();
    }
}