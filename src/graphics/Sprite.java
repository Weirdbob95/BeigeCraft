package graphics;

import de.matthiasmann.twl.utils.PNGDecoder;
import static engine.Activatable.using;
import java.io.FileInputStream;
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
    private static final ShaderProgram SPRITE_SHADER = Resources.loadShaderProgram("sprite");

    private static final VertexArrayObject SPRITE_VAO = VertexArrayObject.createVAO(() -> {
        BufferObject vbo = new BufferObject(GL_ARRAY_BUFFER, new float[]{
            0.5f, 0.5f, 0, 1, 0, // top right
            0.5f, -0.5f, 0, 1, 1, // bottom right
            -0.5f, -0.5f, 0, 0, 1, // bottom left
            -0.5f, 0.5f, 0, 0, 0 // top left
        });
        //BufferObject ebo = new BufferObject(GL_ELEMENT_ARRAY_BUFFER, SPRITE_INDICES);
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 20, 0);
        glEnableVertexAttribArray(0);
        glVertexAttribPointer(1, 2, GL_FLOAT, false, 20, 12);
        glEnableVertexAttribArray(1);
    });

    Texture texture;
    int width;
    int height;

    private Sprite(String fileName) {
        try {
            texture = new Texture("sprites/" + fileName);
            PNGDecoder decoder = new PNGDecoder(new FileInputStream("sprites/" + fileName));
            width = decoder.getWidth();
            height = decoder.getHeight();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void draw(Vec3d position, Vec3d normal, double rotation, Vec2d size, Vec4d color) {
        SPRITE_SHADER.setUniform("projectionMatrix", Camera.getProjectionMatrix());
//        SPRITE_SHADER.setUniform("modelViewMatrix", Camera.camera.getWorldMatrix(position));
        SPRITE_SHADER.setUniform("modelViewMatrix", Camera.camera.getWorldMatrix(position)
                .rotateTowards(normal.toJOML(), Camera.camera.up.toJOML()).rotate(rotation, normal.toJOML()).scale(new Vector3d(size.x, size.y, 1)));
        SPRITE_SHADER.setUniform("color", color);
        using(Arrays.asList(texture, SPRITE_SHADER, SPRITE_VAO), () -> {
            glDrawArrays(GL_TRIANGLE_FAN, 0, 4);
        });
    }
}
