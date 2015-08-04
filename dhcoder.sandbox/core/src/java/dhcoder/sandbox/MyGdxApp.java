package dhcoder.sandbox;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import dhcoder.libgdx.input.VirtualThumbstick;
import dhcoder.support.annotations.NotNull;
import dhcoder.support.math.Angle;
import dhcoder.support.time.Duration;

// TODO: Add Input timer and reset touch point if < episilon for > cutoff seconds

public class MyGdxApp extends ApplicationAdapter {

    public static final String TAG = "SANDBOX";

    // When you hit a breakpoint while debugging an app, or if the phone you're using is just simply being slow, the
    // delta times between frames can be HUGE. Let's clamp to a reasonable max here. This also prevents physics update
    // logic from dealing with time steps that are too large (at which point, objects start going through walls, etc.)
    private static final float MAX_DELTA_TIME_SECS = 1f / 30f;

    private final Duration myElapsedTime = Duration.zero();
    private Camera myCamera;
    private ShapeRenderer myShapeRenderer;
    private MyInputHandler myInputProcessor;
    private Vector2 myPos = new Vector2();
    private Angle myHeading = Angle.fromDegrees(0f);
    private Vector2 myVel = new Vector2();

    private Rectangle myBounds;

    @Override
    public void create() {
        myCamera = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        myShapeRenderer = new ShapeRenderer();
        myShapeRenderer.setProjectionMatrix(myCamera.combined);

        myInputProcessor = new MyInputHandler();
        Gdx.input.setInputProcessor(myInputProcessor);

        float halfW = Gdx.graphics.getWidth() / 2f;
        float halfH = Gdx.graphics.getHeight() / 2f;

        myBounds = new Rectangle(-halfW, -halfH, halfW * 2f, halfH * 2f);
    }

    @Override
    public void render() {
        myElapsedTime.setSeconds(Math.min(Gdx.graphics.getRawDeltaTime(), MAX_DELTA_TIME_SECS));
        update(myElapsedTime);

        float playerSize = Gdx.graphics.getWidth() * 0.03f;
        float triangleSize = playerSize / 4f;
        float halfTriangleSize = triangleSize / 2f;

        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        myShapeRenderer.begin(ShapeType.Filled);
        myShapeRenderer.identity();
        myShapeRenderer.translate(myPos.x, myPos.y, 0f);
        myShapeRenderer.rotate(0f, 0f, 1f, myHeading.getDegrees());
        myShapeRenderer.setColor(Color.BLUE);
        myShapeRenderer.circle(0, 0, playerSize);
        myShapeRenderer.setColor(Color.RED);
        myShapeRenderer.triangle(playerSize - halfTriangleSize, triangleSize, playerSize, 0, playerSize -
            halfTriangleSize, -triangleSize);
        myShapeRenderer.identity();
        myShapeRenderer.end();

        myInputProcessor.debugRender(myShapeRenderer);

    }

    private void update(Duration duration) {
        Vector2 dragged = myInputProcessor.getVelocity();
        myVel.set(dragged).scl(0.1f);
        myHeading.set(myInputProcessor.getHeading());

        myPos.add(myVel);

        if (!myBounds.contains(myPos)) {
            myPos.setZero();
        }
    }

    private class MyInputHandler extends InputAdapter {

        private VirtualThumbstick myThumbL;
        private VirtualThumbstick myThumbR;
        private int myLeftPointer = -1;
        private int myRightPointer = -1;
        private Vector3 myTouch3d = new Vector3();
        private Vector2 myTouch = new Vector2();
        private Vector2 myVelocity = new Vector2();
        private Angle myHeading = Angle.fromDegrees(0f);

        public MyInputHandler() {
            float thumbSize = Gdx.graphics.getWidth() * 0.05f;

            myThumbL = new VirtualThumbstick(thumbSize);
            myThumbR = new VirtualThumbstick(thumbSize);
        }

        public Vector2 getVelocity() {
            return myVelocity;
        }

        public Angle getHeading() {
            return myHeading;
        }

        @Override
        public boolean touchDown(int screenX, int screenY, int pointer, int button) {
            updateMyTouch(screenX, screenY);
            if (screenX <= Gdx.graphics.getWidth() / 2) {
                if (!myThumbL.isPressed()) {
                    myThumbL.begin(myTouch);
                    myLeftPointer = pointer;
                }
            }
            else {
                if (!myThumbR.isPressed()) {
                    myThumbR.begin(myTouch);
                    myRightPointer = pointer;
                }
            }

            return true;
        }

        @Override
        public boolean touchUp(int screenX, int screenY, int pointer, int button) {
            if (pointer == myLeftPointer) {
                myThumbL.end();
                myVelocity.setZero();
                myLeftPointer = -1;
            }
            else if (pointer == myRightPointer) {
                myThumbR.end();
                myRightPointer = -1;
            }

            return true;
        }

        @Override
        public boolean touchDragged(int screenX, int screenY, int pointer) {
            updateMyTouch(screenX, screenY);
            if (pointer == myLeftPointer) {
                myThumbL.drag(myTouch);
                myVelocity.set(myThumbL.getDrag());
            }
            else if (pointer == myRightPointer) {
                myThumbR.drag(myTouch);
                myHeading.setDegrees(myThumbR.getDrag().angle());
            }

            return true;
        }

        @Override
        public boolean keyDown(int keycode) {
            if (keycode == Input.Keys.ESCAPE) {
                Gdx.app.log(TAG, "Quitting");
                Gdx.app.exit();
                return true;
            }

            return false;
        }

        // Call this and then myTouch vec will have screen coordinates
        private void updateMyTouch(int screenX, int screenY) {
            myTouch3d.set(screenX, screenY, 0f);
            myCamera.unproject(myTouch3d);
            myTouch.set(myTouch3d.x, myTouch3d.y);
        }

        public void debugRender(@NotNull ShapeRenderer shapeRenderer) {
            shapeRenderer.setColor(Color.RED);
            myThumbL.debugRender(shapeRenderer);

            shapeRenderer.setColor(Color.GREEN);
            myThumbR.debugRender(shapeRenderer);
        }
    }
}
