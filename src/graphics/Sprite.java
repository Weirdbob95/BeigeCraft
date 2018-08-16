package graphics;

import static engine.Activatable.using;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import opengl.BufferObject;
import opengl.Camera;
import opengl.ShaderProgram;
import opengl.Texture;
import opengl.VertexArrayObject;
import org.joml.Vector3d;
import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL11.GL_TRIANGLE_FAN;
import static org.lwjgl.opengl.GL11.glDrawArrays;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import util.Resources;
import util.vectors.Vec2d;
import util.vectors.Vec3d;
import util.vectors.Vec4d;

public class Sprite {

    private static final Map<String, Sprite> SPRITE_CACHE = new HashMap();

    public static Sprite load(String fileName) {
        if (!SPRITE_CACHE.containsKey(fileName)) {
            Sprite s = new Sprite(fileName);
            SPRITE_CACHE.put(fileName, s);
        }
        return SPRITE_CACHE.get(fileName);
    }

    static final ShaderProgram SPRITE_SHADER = Resources.loadShaderProgram("sprite");

    private static final VertexArrayObject SPRITE_VAO = VertexArrayObject.createVAO(() -> {
        BufferObject vbo = new BufferObject(GL_ARRAY_BUFFER, new float[]{
            0.5f, 0.5f, 0, 1, 1, 1, 1, 1, 1,
            0.5f, -0.5f, 0, 1, 0, 1, 1, 1, 1,
            -0.5f, -0.5f, 0, 0, 0, 1, 1, 1, 1,
            -0.5f, 0.5f, 0, 0, 1, 1, 1, 1, 1
        });
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 36, 0);
        glEnableVertexAttribArray(0);
        glVertexAttribPointer(1, 2, GL_FLOAT, false, 36, 12);
        glEnableVertexAttribArray(1);
        glVertexAttribPointer(2, 4, GL_FLOAT, false, 36, 20);
        glEnableVertexAttribArray(2);
    });

    private final Texture texture;

    private Sprite(String fileName) {
        this.texture = Texture.load(fileName);
    }

    public void draw2d(Vec2d position, double rotation, double scale, Vec4d color) {
        SPRITE_SHADER.setUniform("projectionMatrix", Camera.camera2d.getProjectionMatrix());
        SPRITE_SHADER.setUniform("modelViewMatrix", Camera.camera2d.getWorldMatrix(position, rotation, scale * getWidth(), scale * getHeight()));
        SPRITE_SHADER.setUniform("color", color);
        using(Arrays.asList(texture, SPRITE_SHADER, SPRITE_VAO), () -> {
            glDrawArrays(GL_TRIANGLE_FAN, 0, 4);
        });
    }

    public void draw3d(Vec3d position, Vec3d normal, double rotation, Vec2d size, Vec4d color) {
        SPRITE_SHADER.setUniform("projectionMatrix", Camera.camera3d.getProjectionMatrix());
        if (normal.x != 0 || normal.y != 0) {
            SPRITE_SHADER.setUniform("modelViewMatrix", Camera.camera3d.getWorldMatrix(position)
                    .rotateTowards(normal.toJOML(), Camera.camera3d.up.toJOML()).rotate(rotation, normal.toJOML()).scale(new Vector3d(size.x, size.y, 1)));
        } else {
            SPRITE_SHADER.setUniform("modelViewMatrix", Camera.camera3d.getWorldMatrix(position)
                    .rotate(rotation, normal.toJOML()).scale(new Vector3d(size.x, size.y, 1)));
        }
        SPRITE_SHADER.setUniform("color", color);
        using(Arrays.asList(texture, SPRITE_SHADER, SPRITE_VAO), () -> {
            glDrawArrays(GL_TRIANGLE_FAN, 0, 4);
        });
    }

    public int getHeight() {
        return texture.getHeight();
    }

    public int getWidth() {
        return texture.getWidth();
    }
}
