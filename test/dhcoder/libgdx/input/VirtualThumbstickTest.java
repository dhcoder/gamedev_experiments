package dhcoder.libgdx.input;

import com.badlogic.gdx.math.Vector2;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

public final class VirtualThumbstickTest {
    @Test
    public void defaultThumbstick() {
        VirtualThumbstick thumbstick = new VirtualThumbstick(1.0f);
        Vector2 pos = new Vector2(1.0f, 2.0f);
        Vector2 expectedDrag = new Vector2();
        thumbstick.begin(pos);
        assertThat(thumbstick.getPosition(), equalTo(pos));
        assertThat(thumbstick.getDrag(), equalTo(expectedDrag));
        thumbstick.end();
    }

    @Test
    public void dragThumbstick() {
        VirtualThumbstick thumbstick = new VirtualThumbstick(1.0f);
        Vector2 posStart = new Vector2(10.0f, 10.0f);
        Vector2 posDrag1 = new Vector2(10.0f, 20.0f);
        Vector2 expectedDrag1 = new Vector2(0.0f, 1.0f);
        Vector2 posDrag2 = new Vector2(10.0f, 15.0f);
        Vector2 expectedDrag2 = new Vector2(0.0f, -1.0f);
        thumbstick.begin(posStart);
        assertThat(thumbstick.getPosition(), equalTo(posStart));

        thumbstick.drag(posDrag1);
        assertThat(thumbstick.getDrag(), equalTo(expectedDrag1));

        thumbstick.drag(posDrag2);
        assertThat(thumbstick.getDrag(), equalTo(expectedDrag2));

        thumbstick.end();
    }
}