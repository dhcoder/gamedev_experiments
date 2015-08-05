package dhcoder.libgdx.assets;

import com.badlogic.gdx.graphics.g2d.Animation;

/**
 * A named collection of all {@link Animation}s loaded so far for this game.
 */
public final class AnimationDatastore extends AssetGroup<AnimationGroup> {

    /**
     * Convenience method to grab a tile out from a subgroup directly.
     */
    public Animation get(String group, String name) {
        return get(group).get(name);
    }
}
