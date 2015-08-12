package dhcoder.sandbox;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import dhcoder.support.time.Duration;

public class MyGdxApp extends ApplicationAdapter {

    public static final String TAG = "SANDBOX";

    // When you hit a breakpoint while debugging an app, or if the phone you're using is just simply being slow, the
    // delta times between frames can be HUGE. Let's clamp to a reasonable max here. This also prevents physics update
    // logic from dealing with time steps that are too large (at which point, objects start going through walls, etc.)
    private static final float MAX_DELTA_TIME_SECS = 1f / 30f;

    private final Duration myElapsedTime = Duration.zero();
    private Camera myCamera;
    private ShapeRenderer myShapeRenderer;
    private SpriteBatch mySpriteBatch;
    private Texture myLogo;

    @Override
    public void create() {
        myCamera = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        myShapeRenderer = new ShapeRenderer();
        myShapeRenderer.setProjectionMatrix(myCamera.combined);
        mySpriteBatch = new SpriteBatch();
        mySpriteBatch.setProjectionMatrix(myCamera.combined);
        myLogo = new Texture(Gdx.files.internal("badlogic.jpg"));
        mySpriteBatch.setShader(new BasicShader("shaders/inverse").getProgram());
        
        Gdx.input.setInputProcessor(new MyInputHandler());
    }

    @Override
    public void render() {
        myElapsedTime.setSeconds(Math.min(Gdx.graphics.getRawDeltaTime(), MAX_DELTA_TIME_SECS));
        update(myElapsedTime);

        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        myShapeRenderer.begin(ShapeType.Filled);
        myShapeRenderer.setColor(Color.DARK_GRAY);
        myShapeRenderer.circle(0f, 0f, myLogo.getWidth() * 2f / 3f);
        myShapeRenderer.end();

        mySpriteBatch.begin();
        mySpriteBatch.draw(myLogo, -myLogo.getHeight() / 2f, -myLogo.getWidth() / 2f);
        mySpriteBatch.end();
    }

    private void update(Duration duration) {
        // YOUR CODE HERE
    }

    private class MyInputHandler extends InputAdapter {
        private Vector3 myTouch3d = new Vector3();
        private Vector2 myTouch = new Vector2();

        @Override
        public boolean mouseMoved(int screenX, int screenY) {
            return true;
        }

        @Override
        public boolean touchDown(int screenX, int screenY, int pointer, int button) {
            updateMyTouch(screenX, screenY);
            return true;
        }

        @Override
        public boolean touchUp(int screenX, int screenY, int pointer, int button) {
            updateMyTouch(screenX, screenY);
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
    }
}
