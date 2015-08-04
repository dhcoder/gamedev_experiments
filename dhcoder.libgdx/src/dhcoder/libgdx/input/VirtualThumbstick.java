package dhcoder.libgdx.input;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import dhcoder.support.annotations.NotNull;

/**
 * A class which tracks the position of a thumb on the screen and how much it has moved. Useful
 * helper class when using touch as a movement controller.
 */
public final class VirtualThumbstick {
    private final float myRadius;
    private final float myRadius2;
    @NotNull private final Vector2 myStart = new Vector2();
    @NotNull private final Vector2 myCurr = new Vector2();
    @NotNull private final Vector2 myDrag = new Vector2();
    private boolean myStarted;

    public VirtualThumbstick(float radius) {
        if (radius <= 0f) {
            throw new IllegalArgumentException(String.format("Can't create radius with invalid radius %s", radius));
        }

        myRadius = radius;
        myRadius2 = myRadius * myRadius;
    }

    public boolean isPressed() {
        return myStarted;
    }

    @NotNull
    public Vector2 getPosition() {
        if (!myStarted) {
            throw new IllegalStateException("getPosition called without calling  begin() first");
        }

        return myStart;
    }

    @NotNull
    public Vector2 getDrag() {
        if (!myStarted) {
            throw new IllegalStateException("getDrag called without calling begin() first");
        }

        return myDrag;
    }

    public void begin(@NotNull Vector2 position) {
        if (myStarted) {
            throw new IllegalStateException("Invalid call to begin() without calling end() first");
        }

        myStart.set(position);
        myCurr.set(myStart);
        myDrag.setZero();
        myStarted = true;
    }

    public void drag(@NotNull Vector2 dragPosition) {
        if (!myStarted) {
            throw new IllegalStateException("Invalid call to drag() without calling begin()");
        }

        myCurr.set(dragPosition);
        Vector2 dragVector = myDrag;
        dragVector.set(myCurr).sub(myStart);

        if (dragVector.len2() > myRadius2) {
            dragVector.nor().scl(myRadius);
            myStart.set(myCurr).sub(dragVector);
        }
    }

    public void end() {
        if (!myStarted) {
            throw new IllegalStateException("Invalid call to end() without calling begin()");
        }
        myStarted = false;
    }

    public void debugRender(@NotNull ShapeRenderer shapeRenderer) {
        if (!myStarted) return;

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.circle(myStart.x, myStart.y, myRadius);
        shapeRenderer.line(myStart, myCurr);
        shapeRenderer.end();
    }
}
