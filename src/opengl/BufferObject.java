package opengl;

import engine.Activatable;
import static org.lwjgl.opengl.GL15.*;

public class BufferObject implements Activatable {

    public final int bufferObject;
    private final int type;

    public BufferObject(int type, float[] data) {
        this(type);
        putData(data);
    }

    public BufferObject(int type, int[] data) {
        this(type);
        putData(data);
    }

    public BufferObject(int type) {
        bufferObject = glGenBuffers();
        this.type = type;
        activate();
    }

    @Override
    public void activate() {
        glBindBuffer(type, bufferObject);
    }

    @Override
    public void deactivate() {
        glBindBuffer(type, 0);
    }

    public void destroy() {
        glDeleteBuffers(bufferObject);
    }

    public final void putData(float[] data) {
        glBufferData(type, data, GL_STATIC_DRAW);
    }

    public final void putData(int[] data) {
        glBufferData(type, data, GL_STATIC_DRAW);
    }
}
