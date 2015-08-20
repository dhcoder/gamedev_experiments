package dhcoder.sandbox;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.Body;
import dhcoder.libgdx.input.VirtualThumbstick;
import dhcoder.libgdx.physics.AbstractCollisionHandler;
import dhcoder.libgdx.physics.Physics;
import dhcoder.libgdx.physics.PhysicsSystem;
import dhcoder.support.annotations.NotNull;
import dhcoder.support.annotations.Nullable;
import dhcoder.support.math.Angle;
import dhcoder.support.memory.HeapPool;
import dhcoder.support.time.Duration;

import java.util.ArrayList;
import java.util.List;

// TODO: Add random start position and test

public class MyGdxApp extends ApplicationAdapter {

    private static final String TAG = "THUMB3";

    // When you hit a breakpoint while debugging an app, or if the phone you're using is just simply being slow, the
    // delta times between frames can be HUGE. Let's clamp to a reasonable max here. This also prevents physics update
    // logic from dealing with time steps that are too large (at which point, objects start going through walls, etc.)
    private static final float MAX_DELTA_TIME_SECS = 1f / 30f;

    private final Duration myElapsedTime = Duration.zero();
    private final Vector2 myForce = new Vector2();
    private final Duration BULLET_PERIOD = Duration.fromMilliseconds(500f);
    private Camera myCamera;
    private PhysicsSystem myPhysicsSystem;
    private Physics myPhysics;
    private ShapeRenderer myShapeRenderer;
    private MyInputHandler myInputProcessor;
    private Player myPlayer;
    @Nullable private Patch myPatch;
    private boolean myPlayerOnPatch;
    private Duration myPatchTime = Duration.zero();
    private Duration myPatchTimeLeft = Duration.zero();
    private Duration myNextBullet = Duration.from(BULLET_PERIOD);
    private Rectangle myBounds;
    private Wall myWallW;
    private Wall myWallE;
    private Wall myWallN;
    private Wall myWallS;
    private HeapPool<Bullet> myBullets = HeapPool.of(Bullet.class, 100);
    final List<Bullet> myDeadBullets = new ArrayList<>(10);


    private int myScore;
    private int myHighestScore;
    private String myScoreMessage;
    private BitmapFont myFont;
    private SpriteBatch mySpriteRenderer;

    @Override
    public void dispose() {
        myFont.dispose();
        myPhysicsSystem.dispose();
        myShapeRenderer.dispose();
        mySpriteRenderer.dispose();
    }

    @Override
    public void create() {
        myCamera = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        mySpriteRenderer = new SpriteBatch();
        myFont = new BitmapFont();
        updateScoreMessage();

        myShapeRenderer = new ShapeRenderer();
        myShapeRenderer.setProjectionMatrix(myCamera.combined);

        myInputProcessor = new MyInputHandler();
        Gdx.input.setInputProcessor(myInputProcessor);

        float halfW = Gdx.graphics.getWidth() / 2f;
        float halfH = Gdx.graphics.getHeight() / 2f;

        myBounds = new Rectangle(-halfW, -halfH, halfW * 2f, halfH * 2f);

        myPhysicsSystem = new PhysicsSystem(1000, new Vector2(0f, 0f));
        myPhysics = new Physics(1f / (Gdx.graphics.getWidth() * 0.025f));
        myPlayer = new Player(myPhysicsSystem.getWorld(), 1f);
        float size = 15f;
        myWallW = new Wall(myPhysicsSystem.getWorld(), -size, 0f, 1f, size);
        myWallE = new Wall(myPhysicsSystem.getWorld(), size, 0f, 1f, size);
        myWallN = new Wall(myPhysicsSystem.getWorld(), 0f, size, size, 1.0f);
        myWallS = new Wall(myPhysicsSystem.getWorld(), 0f, -size, size, 1.0f);

        myPhysicsSystem.addCollisionHandler(Layers.PLAYER, Layers.WALL, new AbstractCollisionHandler() {
            @Override
            public void onCollided(Body bodyA, Body bodyB) {
                myPlayer.reset();
            }
        });

        myPhysicsSystem.addCollisionHandler(Layers.PLAYER, Layers.PATCH, new AbstractCollisionHandler() {
            @Override
            public void onOverlapping(Body bodyA, Body bodyB) {
                myPlayerOnPatch = true;
            }

            @Override
            public void onSeparated(Body bodyA, Body bodyB) {
                myPlayerOnPatch = false;
                myPatchTimeLeft.set(myPatchTime);
            }
        });

        myPhysicsSystem.addCollisionHandler(Layers.PLAYER, Layers.BULLET, new AbstractCollisionHandler() {
            @Override
            public void onCollided(Body bodyA, Body bodyB) {
                Bullet bullet = (Bullet)bodyB.getUserData();
                Vector2 bulletAngle = new Vector2(bullet.getPosition()).sub(myPlayer.getPosition());
                Vector2 playerAngle = new Vector2(
                    MathUtils.cosDeg(myPlayer.getHeading().getDegrees()),
                    MathUtils.sinDeg(myPlayer.getHeading().getDegrees()));

                if (Math.abs(playerAngle.angle(bulletAngle)) > 60f) {
                    myPlayer.reset();
                    myScore = 0;
                    updateScoreMessage();
                }

                myDeadBullets.add(bullet);
            }
        });
    }

    @Override
    public void render() {
        myElapsedTime.setSeconds(Math.min(Gdx.graphics.getRawDeltaTime(), MAX_DELTA_TIME_SECS));
        update(myElapsedTime);

        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

//        myPlayer.render(myShapeRenderer);
        myPhysicsSystem.debugRender(myCamera.combined, myPhysics.getMetersToPixels());
        myInputProcessor.debugRender(myShapeRenderer);

        mySpriteRenderer.setProjectionMatrix(myCamera.combined);
        mySpriteRenderer.begin();
        myFont.draw(mySpriteRenderer, myScoreMessage, myBounds.x + 10f, myBounds.y + myBounds.getHeight() - 10f);
        mySpriteRenderer.end();

    }

    private void update(Duration elapsedTime) {
        for (int i = 0; i < myDeadBullets.size(); i++) {
            myBullets.free(myDeadBullets.get(i));
        }
        myDeadBullets.clear();


        if (myPatch == null) {
            float size = 2f;
            float x = MathUtils.random(myWallW.getBody().getWorldCenter().x + size, myWallE.getBody().getWorldCenter
                ().x - size);
            float y = MathUtils.random(myWallS.getBody().getWorldCenter().y + size, myWallN.getBody().getWorldCenter
                ().y - size);
            myPatch = new Patch(myPhysicsSystem.getWorld(), x, y, size, size);
            myPatchTime.setMilliseconds(MathUtils.random(500f, 1500f));
            myPatchTimeLeft.set(myPatchTime);
        }
        else if (myPlayerOnPatch) {
            myPatchTimeLeft.subtract(elapsedTime);

            if (myPatchTimeLeft.isZero()) {
                myPhysicsSystem.destroyBody(myPatch.getBody());
                myPatch = null;
                myScore++;
                if (myHighestScore < myScore) {
                    myHighestScore = myScore;
                }
                updateScoreMessage();
            }
        }

        myNextBullet.subtract(elapsedTime);
        if (myNextBullet.isZero()) {
            Bullet bullet = myBullets.grabNew();
            bullet.initialize(myPhysicsSystem, -10f, -10f, 0.1f, myPlayer.getPosition().x, myPlayer.getPosition().y,
                .02f);
            myNextBullet.set(BULLET_PERIOD);
        }

        List<Bullet> bullets = myBullets.getItemsInUse();
        int numBullets = bullets.size();
        for (int i = 0; i < numBullets; i++) {
            Bullet bullet = bullets.get(i);
            float x = myPhysics.toPixels(bullet.getBody().getWorldCenter().x);
            float y = myPhysics.toPixels(bullet.getBody().getWorldCenter().y);
            if (!myBounds.contains(x, y)) {
                myBullets.free(bullet);
                --i;
                --numBullets;
            }
        }

        Vector2 dragged = myInputProcessor.getDragged();
        myForce.set(dragged).scl(.0005f);
        myPlayer.setVelocity(myForce);
        myPlayer.setHeading(myInputProcessor.getHeading());

        myPhysicsSystem.update(elapsedTime);

        if (!myBounds.contains(myPlayer.getPosition())) {
            myPlayer.reset();
        }
    }

    private void updateScoreMessage() {
        myScoreMessage = String.format("%d / %d", myScore, myHighestScore);
    }

    private class MyInputHandler extends InputAdapter {

        private VirtualThumbstick myThumbL;
        private VirtualThumbstick myThumbR;
        private int myLeftPointer = -1;
        private int myRightPointer = -1;
        private Vector3 myTouch3d = new Vector3();
        private Vector2 myTouch = new Vector2();
        private Vector2 myDragged = new Vector2();
        private Angle myHeading = Angle.fromDegrees(0f);

        public MyInputHandler() {
            float thumbSize = Gdx.graphics.getWidth() * 0.05f;

            myThumbL = new VirtualThumbstick(thumbSize * 1f);
            myThumbR = new VirtualThumbstick(thumbSize);
        }

        public Vector2 getDragged() {
            return myDragged;
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
                myDragged.setZero();
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
                myDragged.set(myThumbL.getDrag());
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
