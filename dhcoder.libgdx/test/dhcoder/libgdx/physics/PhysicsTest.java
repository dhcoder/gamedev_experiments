package dhcoder.libgdx.physics;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import org.junit.Test;

import static com.google.common.truth.Truth.assertThat;

public class PhysicsTest {

    @Test
    public void assertDefaultPhysicsHasValidScale() {
        Physics physics = new Physics();
        float metersToPixels = physics.getMetersToPixels();
        float pixelsToMeters = physics.getPixelsToMeters();

        assertThat(metersToPixels).isEqualTo(1f / pixelsToMeters);
    }

    @Test
    public void assertMetersToPixelScalesAsExpected() {
        Physics physics = new Physics(1f / 100f); // 100 pixels -> 1 meter

        assertThat(physics.toMeters(200f)).isEqualTo(2f);
        assertThat(physics.toPixels(5.5f)).isEqualTo(550f);

        Vector2 ptPixel = new Vector2(100f, 200f);
        physics.toMeters(ptPixel);
        assertThat(ptPixel.x).isEqualTo(1f);
        assertThat(ptPixel.y).isEqualTo(2f);

        physics.toPixels(ptPixel);
        assertThat(ptPixel.x).isEqualTo(100f);
        assertThat(ptPixel.y).isEqualTo(200f);
    }

    @Test
    public void createCircleShapeByPixels() {
        Physics physics = new Physics(1f / 100f); // 100 pixels -> 1 meter

        CircleShape circle = physics.newCircle(1000f);
        assertThat(circle.getRadius()).isEqualTo(10f);
        circle.dispose();
    }

    @Test
    public void createRectShapeByPixels() {
        Physics physics = new Physics(1f / 100f); // 100 pixels -> 1 meter

        PolygonShape rect = physics.newRectangle(500f, 100f);
        Vector2 topLeft = new Vector2();
        Vector2 botRight = new Vector2();

        rect.getVertex(0, topLeft);
        rect.getVertex(2, botRight);

        assertThat(topLeft.x).isEqualTo(-5f);
        assertThat(topLeft.y).isEqualTo(-1f);
        assertThat(botRight.x).isEqualTo(5f);
        assertThat(botRight.y).isEqualTo(1f);

        rect.dispose();
    }

}