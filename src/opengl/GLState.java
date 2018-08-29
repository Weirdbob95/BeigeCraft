package opengl;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL11.glBlendFunc;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL20.glUseProgram;
import static org.lwjgl.opengl.GL30.GL_FRAMEBUFFER;
import static org.lwjgl.opengl.GL30.glBindFramebuffer;
import static org.lwjgl.opengl.GL30.glBindVertexArray;

public class GLState {

    private static GLState state = new GLState();

    private int blendFunc1, blendFunc2;
    private final Map<Integer, BufferObject> buffers = new HashMap();
    private final Map<Integer, Boolean> flags = new HashMap();
    private Framebuffer framebuffer = null;
    private ShaderProgram shader = null;
    private Texture texture = null;
    private VertexArrayObject vao = null;

    public static void bindBuffer(BufferObject buffer) {
        if (state.buffers.get(buffer.type) != buffer) {
            state.buffers.put(buffer.type, buffer);
            glBindBuffer(buffer.type, buffer.id);
        }
    }

    public static void bindFramebuffer(Framebuffer framebuffer) {
        if (framebuffer != state.framebuffer) {
            state.framebuffer = framebuffer;
            glBindFramebuffer(GL_FRAMEBUFFER, framebuffer == null ? 0 : framebuffer.id);
        }
    }

    public static void bindShaderProgram(ShaderProgram shader) {
        if (shader != state.shader) {
            state.shader = shader;
            glUseProgram(shader == null ? 0 : shader.id);
        }
    }

    public static void bindTexture(Texture texture) {
        if (texture != state.texture) {
            state.texture = texture;
            if (texture != null) {
                glActiveTexture(GL_TEXTURE0 + texture.num);
                glBindTexture(texture.type, texture.id);
            }
        }
    }

    public static void bindVertexArrayObject(VertexArrayObject vao) {
        if (vao != state.vao) {
            state.vao = vao;
            glBindVertexArray(vao == null ? 0 : vao.id);
        }
    }

    private GLState copy() {
        GLState copy = new GLState();
        copy.buffers.putAll(buffers);
        copy.flags.putAll(flags);
        copy.framebuffer = framebuffer;
        copy.shader = shader;
        copy.texture = texture;
        copy.vao = vao;
        return copy;
    }

    public static void disable(int... flags) {
        for (int flag : flags) {
            setFlag(flag, false);
        }
    }

    public static void enable(int... flags) {
        for (int flag : flags) {
            setFlag(flag, true);
        }
    }

    public static BufferObject getBuffer(int type) {
        return state.buffers.get(type);
    }

    public static Boolean getFlag(int flag) {
        return state.flags.get(flag);
    }

    public static Framebuffer getFramebuffer() {
        return state.framebuffer;
    }

    public static ShaderProgram getShaderProgram() {
        return state.shader;
    }

    public static Texture getTexture() {
        return state.texture;
    }

    public static VertexArrayObject getVertexArrayObject() {
        return state.vao;
    }

    public static void setBlendFunc(int blendFunc1, int blendFunc2) {
        if (blendFunc1 != state.blendFunc1 || blendFunc2 != state.blendFunc2) {
            state.blendFunc1 = blendFunc1;
            state.blendFunc2 = blendFunc2;
            glBlendFunc(blendFunc1, blendFunc2);
        }
    }

    public static void setFlag(int flag, boolean value) {
        if (!Objects.equals(state.flags.get(flag), value)) {
            state.flags.put(flag, value);
            if (value) {
                glEnable(flag);
            } else {
                glDisable(flag);
            }
        }
    }

    public static void inTempState(Runnable r) {
        GLState oldState = state.copy();
        r.run();
        for (BufferObject buffer : oldState.buffers.values()) {
            bindBuffer(buffer);
        }
        for (Entry<Integer, Boolean> e : oldState.flags.entrySet()) {
            if (e.getValue() != null) {
                setFlag(e.getKey(), e.getValue());
            }
        }
        bindFramebuffer(oldState.framebuffer);
        bindShaderProgram(oldState.shader);
        bindTexture(oldState.texture);
        bindVertexArrayObject(oldState.vao);
    }
}
