package dhcoder.sandbox

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.glutils.ShaderProgram

/**
 * Convenience shader class that wraps a vertex and fragment shader.
 *
 * Shader file names should be "vertex.glsl" and "fragment.glsl" and share a common path.
 */
public class BasicShader(path: String) {
    public val program: ShaderProgram

    init {
        val vertexShader = Gdx.files.internal("$path/vertex.glsl").readString()
        val fragmentShader = Gdx.files.internal("$path/fragment.glsl").readString()
        program = ShaderProgram(vertexShader, fragmentShader)
    }
}
