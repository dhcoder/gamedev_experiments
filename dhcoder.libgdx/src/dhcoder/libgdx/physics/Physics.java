package dhcoder.libgdx.physics;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.PolygonShape;

/**
 * Misc. helper methods for managing the difference of scale between the physics world and
 * pixel space.
 */
public final class Physics {
    private float myPixelsToMeters;
    private float myMetersToPixels;

    /**
     * Create physics with 1 meter to 20 pixels
     */
    public Physics() {
        this(1f / 20f); // Default: 1 meter to 20 pixels
    }

    public Physics(float pixelsToMeters) {
        myPixelsToMeters = pixelsToMeters;
        myMetersToPixels = 1 / myPixelsToMeters;
    }

    public float getMetersToPixels() {
        return myMetersToPixels;
    }

    public float getPixelsToMeters() {
        return myPixelsToMeters;
    }

    public float toPixels(float meters) {
        return meters * myMetersToPixels;
    }

    public float toMeters(float pixels) {
        return pixels * myPixelsToMeters;
    }

    public Vector2 toPixels(Vector2 meters) {
        return meters.scl(myMetersToPixels);
    }

    public Vector2 toMeters(Vector2 pixels) {
        return pixels.scl(myPixelsToMeters);
    }

    public CircleShape newCircle(float radiusPixels) {
        CircleShape circleShape = new CircleShape();
        circleShape.setRadius(radiusPixels * myPixelsToMeters);
        return circleShape;
    }

    public PolygonShape newRectangle(float halfWidthPixels, float halfHeightPixels) {
        PolygonShape polygonShape = new PolygonShape();
        polygonShape.setAsBox(halfWidthPixels * myPixelsToMeters, halfHeightPixels * myPixelsToMeters);
        return polygonShape;
    }
}
