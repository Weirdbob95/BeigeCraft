package opengl;

import engine.Activatable;
import java.util.HashMap;
import org.joml.Matrix4d;
import static org.lwjgl.opengl.GL11.GL_TRUE;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL32.GL_GEOMETRY_SHADER;
import util.vectors.Vec3d;
import util.vectors.Vec4d;

public class ShaderProgram implements Activatable {

    private static int activeProgram;

    private final int shaderProgram;

    /**
     * Constructs a shader program with both a vertex shader and a fragment
     * shader.
     *
     * @param vertexShaderSource The source code of the vertex shader.
     * @param fragmentShaderSource The source code of the fragment shader.
     */
    public ShaderProgram(String vertexShaderSource, String fragmentShaderSource) {
        int vertexShader = glCreateShader(GL_VERTEX_SHADER);
        glShaderSource(vertexShader, vertexShaderSource);
        glCompileShader(vertexShader);
        if (glGetShaderi(vertexShader, GL_COMPILE_STATUS) != GL_TRUE) {
            throw new RuntimeException("Vertex shader doesn't compile:\n" + glGetShaderInfoLog(vertexShader));
        }

        int fragmentShader = glCreateShader(GL_FRAGMENT_SHADER);
        glShaderSource(fragmentShader, fragmentShaderSource);
        glCompileShader(fragmentShader);
        if (glGetShaderi(fragmentShader, GL_COMPILE_STATUS) != GL_TRUE) {
            throw new RuntimeException("Fragment shader doesn't compile:\n" + glGetShaderInfoLog(fragmentShader));
        }

        shaderProgram = glCreateProgram();
        glAttachShader(shaderProgram, vertexShader);
        glAttachShader(shaderProgram, fragmentShader);
        glLinkProgram(shaderProgram);
        // TODO: Check for linking errors

        glDeleteShader(vertexShader);
        glDeleteShader(fragmentShader);
    }

    /**
     * Constructs a shader program with a vertex shader, a geometry shader, and
     * a fragment shader.
     *
     * @param vertexShaderSource The source code of the vertex shader.
     * @param geometryShaderSource The source code of the vertex shader.
     * @param fragmentShaderSource The source code of the fragment shader.
     */
    public ShaderProgram(String vertexShaderSource, String geometryShaderSource, String fragmentShaderSource) {
        int vertexShader = glCreateShader(GL_VERTEX_SHADER);
        glShaderSource(vertexShader, vertexShaderSource);
        glCompileShader(vertexShader);
        if (glGetShaderi(vertexShader, GL_COMPILE_STATUS) != GL_TRUE) {
            throw new RuntimeException("Vertex shader doesn't compile:\n" + glGetShaderInfoLog(vertexShader));
        }

        int geometryShader = glCreateShader(GL_GEOMETRY_SHADER);
        glShaderSource(geometryShader, geometryShaderSource);
        glCompileShader(geometryShader);
        if (glGetShaderi(geometryShader, GL_COMPILE_STATUS) != GL_TRUE) {
            throw new RuntimeException("Geometry shader doesn't compile:\n" + glGetShaderInfoLog(geometryShader));
        }

        int fragmentShader = glCreateShader(GL_FRAGMENT_SHADER);
        glShaderSource(fragmentShader, fragmentShaderSource);
        glCompileShader(fragmentShader);
        if (glGetShaderi(fragmentShader, GL_COMPILE_STATUS) != GL_TRUE) {
            throw new RuntimeException("Fragment shader doesn't compile:\n" + glGetShaderInfoLog(fragmentShader));
        }

        shaderProgram = glCreateProgram();
        glAttachShader(shaderProgram, vertexShader);
        glAttachShader(shaderProgram, geometryShader);
        glAttachShader(shaderProgram, fragmentShader);
        glLinkProgram(shaderProgram);
        // TODO: Check for linking errors

        glDeleteShader(vertexShader);
        glDeleteShader(fragmentShader);
    }

    @Override
    public void activate() {
        if (activeProgram != shaderProgram) {
            glUseProgram(shaderProgram);
            activeProgram = shaderProgram;
        }
    }

    @Override
    public void deactivate() {
        if (activeProgram != 0) {
            glUseProgram(0);
            activeProgram = 0;
        }
    }

    private final HashMap<String, Integer> uniformLocations = new HashMap();

    private int getUniformLocation(String name) {
        if (!uniformLocations.containsKey(name)) {
            uniformLocations.put(name, glGetUniformLocation(shaderProgram, name));
        }
        return uniformLocations.get(name);
    }

    public void setUniform(String name, int value) {
        activate();
        int uniform = getUniformLocation(name);
        glUniform1i(uniform, value);
    }

    public void setUniform(String name, Vec3d value) {
        activate();
        int uniform = getUniformLocation(name);
        glUniform3fv(uniform, new float[]{(float) value.x, (float) value.y, (float) value.z});
    }

    public void setUniform(String name, Vec4d value) {
        activate();
        int uniform = getUniformLocation(name);
        glUniform4fv(uniform, new float[]{(float) value.x, (float) value.y, (float) value.z, (float) value.w});
    }

    public void setUniform(String name, Matrix4d mat) {
        activate();
        int uniform = getUniformLocation(name);
        glUniformMatrix4fv(uniform, false, new float[]{
            (float) mat.m00(), (float) mat.m01(), (float) mat.m02(), (float) mat.m03(),
            (float) mat.m10(), (float) mat.m11(), (float) mat.m12(), (float) mat.m13(),
            (float) mat.m20(), (float) mat.m21(), (float) mat.m22(), (float) mat.m23(),
            (float) mat.m30(), (float) mat.m31(), (float) mat.m32(), (float) mat.m33()});
    }
}
