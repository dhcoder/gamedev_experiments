package dhcoder.sandbox

import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.InputAdapter
import com.badlogic.gdx.graphics.*
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.Vector3
import dhcoder.support.time.Duration
import kotlin.properties.Delegates

public class MyGdxApp : ApplicationAdapter() {
    private val myElapsedTime = Duration.zero()

    private var myCamera: Camera by Delegates.notNull()
    private var myShapeRenderer: ShapeRenderer by Delegates.notNull()
    private var mySpriteBatch: SpriteBatch by Delegates.notNull()
    private var myLogo: Texture by Delegates.notNull()

    override fun create() {
        myCamera = OrthographicCamera(Gdx.graphics.getWidth().toFloat(), Gdx.graphics.getHeight().toFloat())
        myShapeRenderer = ShapeRenderer()
        myShapeRenderer.setProjectionMatrix(myCamera.combined)
        mySpriteBatch = SpriteBatch()
        mySpriteBatch.setProjectionMatrix(myCamera.combined)
        myLogo = Texture(Gdx.files.internal("badlogic.jpg"))
        mySpriteBatch.setShader(BasicShader("shaders/inverse").program)

        Gdx.input.setInputProcessor(MyInputHandler())
    }

    override fun render() {
        myElapsedTime.setSeconds(Math.min(Gdx.graphics.getRawDeltaTime(), MAX_DELTA_TIME_SECS))
        update(myElapsedTime)

        Gdx.gl.glClearColor(0f, 0f, 0f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        myShapeRenderer.begin(ShapeType.Filled)
        myShapeRenderer.setColor(Color.DARK_GRAY)
        myShapeRenderer.circle(0f, 0f, myLogo.getWidth() * 2f / 3f)
        myShapeRenderer.end()

        mySpriteBatch.begin()
        mySpriteBatch.draw(myLogo, -myLogo.getHeight() / 2f, -myLogo.getWidth() / 2f)
        mySpriteBatch.end()
    }

    private fun update(duration: Duration) {
        // YOUR CODE HERE
    }

    private inner class MyInputHandler : InputAdapter() {
        private val myTouch3d = Vector3()
        private val myTouch = Vector2()

        override fun mouseMoved(screenX: Int, screenY: Int): Boolean {
            return true
        }

        override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
            updateMyTouch(screenX, screenY)
            return true
        }

        override fun touchUp(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
            updateMyTouch(screenX, screenY)
            return true
        }

        override fun keyDown(keycode: Int): Boolean {
            if (keycode == Input.Keys.ESCAPE) {
                Gdx.app.log(TAG, "Quitting")
                Gdx.app.exit()
                return true
            }

            return false
        }

        // Call this and then myTouch vec will have screen coordinates
        private fun updateMyTouch(screenX: Int, screenY: Int) {
            myTouch3d.set(screenX.toFloat(), screenY.toFloat(), 0f)
            myCamera.unproject(myTouch3d)
            myTouch.set(myTouch3d.x, myTouch3d.y)
        }
    }

    companion object {
        public val TAG: String = "SANDBOX"

        // When you hit a breakpoint while debugging an app, or if the phone you're using is just simply being slow, the
        // delta times between frames can be HUGE. Let's clamp to a reasonable max here. This also prevents physics update
        // logic from dealing with time steps that are too large (at which point, objects start going through walls, etc.)
        private val MAX_DELTA_TIME_SECS = 1f / 30f
    }
}
