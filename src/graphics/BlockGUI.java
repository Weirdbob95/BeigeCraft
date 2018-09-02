package graphics;

import static graphics.Sprite.SPRITE_SHADER;
import java.util.HashMap;
import java.util.Map;
import opengl.BufferObject;
import opengl.Camera;
import static opengl.GLObject.bindAll;
import opengl.VertexArrayObject;
import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_INT;
import static org.lwjgl.opengl.GL11.glDrawElements;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_ELEMENT_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import util.vectors.Vec2d;
import util.vectors.Vec3d;
import util.vectors.Vec4d;
import definitions.BlockType;
import static world.World.TERRAIN_TEXTURE;

public class BlockGUI {

    private static final Map<BlockType, BlockGUI> BLOCKGUI_CACHE = new HashMap();

    public static BlockGUI load(BlockType bt) {
        if (!BLOCKGUI_CACHE.containsKey(bt)) {
            BlockGUI b = new BlockGUI(bt);
            BLOCKGUI_CACHE.put(bt, b);
        }
        return BLOCKGUI_CACHE.get(bt);
    }

    private final VertexArrayObject vao;

    private BlockGUI(BlockType bt) {
        int texID1 = bt.getTexID(new Vec3d(1, 0, 0));
        int texID2 = bt.getTexID(new Vec3d(0, 1, 0));
        int texID3 = bt.getTexID(new Vec3d(0, 0, 1));
        vao = VertexArrayObject.createVAO(() -> {
            float h = (float) Math.sqrt(3) / 2;
            float ep = .01f;
            BufferObject vbo = new BufferObject(GL_ARRAY_BUFFER, new float[]{
                -h, -.5f, 0, (texID1 % 256 + ep) / 5f, (texID1 / 256 + ep) / 4f, .8f, .8f, .8f, 1,
                -h, .5f, 0, (texID1 % 256 + ep) / 5f, (texID1 / 256 + 1 - ep) / 4f, .8f, .8f, .8f, 1,
                0, 0, 0, (texID1 % 256 + 1 - ep) / 5f, (texID1 / 256 + 1 - ep) / 4f, .8f, .8f, .8f, 1,
                0, -1, 0, (texID1 % 256 + 1 - ep) / 5f, (texID1 / 256 + ep) / 4f, .8f, .8f, .8f, 1,
                h, -.5f, 0, (texID2 % 256 + ep) / 5f, (texID2 / 256 + ep) / 4f, .6f, .6f, .6f, 1,
                h, .5f, 0, (texID2 % 256 + ep) / 5f, (texID2 / 256 + 1 - ep) / 4f, .6f, .6f, .6f, 1,
                0, 0, 0, (texID2 % 256 + 1 - ep) / 5f, (texID2 / 256 + 1 - ep) / 4f, .6f, .6f, .6f, 1,
                0, -1, 0, (texID2 % 256 + 1 - ep) / 5f, (texID2 / 256 + ep) / 4f, .6f, .6f, .6f, 1,
                -h, .5f, 0, (texID3 % 256 + ep) / 5f, (texID3 / 256 + ep) / 4f, 1, 1, 1, 1,
                0, 1, 0, (texID3 % 256 + ep) / 5f, (texID3 / 256 + 1 - ep) / 4f, 1, 1, 1, 1,
                h, .5f, 0, (texID3 % 256 + 1 - ep) / 5f, (texID3 / 256 + 1 - ep) / 4f, 1, 1, 1, 1,
                0, 0, 0, (texID3 % 256 + 1 - ep) / 5f, (texID3 / 256 + ep) / 4f, 1, 1, 1, 1
            });
            BufferObject ebo = new BufferObject(GL_ELEMENT_ARRAY_BUFFER, new int[]{
                0, 1, 2, 0, 2, 3,
                4, 5, 6, 4, 6, 7,
                8, 9, 10, 8, 10, 11
            });
            glVertexAttribPointer(0, 3, GL_FLOAT, false, 36, 0);
            glEnableVertexAttribArray(0);
            glVertexAttribPointer(1, 2, GL_FLOAT, false, 36, 12);
            glEnableVertexAttribArray(1);
            glVertexAttribPointer(2, 4, GL_FLOAT, false, 36, 20);
            glEnableVertexAttribArray(2);
        });
    }

    public void draw(Vec2d position, double scale) {
        SPRITE_SHADER.setUniform("projectionMatrix", Camera.camera2d.getProjectionMatrix());
        SPRITE_SHADER.setUniform("modelViewMatrix", Camera.camera2d.getWorldMatrix(position, 0, scale, scale));
        SPRITE_SHADER.setUniform("color", new Vec4d(1, 1, 1, 1));
        bindAll(TERRAIN_TEXTURE, SPRITE_SHADER, vao);
        glDrawElements(GL_TRIANGLES, 18, GL_UNSIGNED_INT, 0);
    }
}
