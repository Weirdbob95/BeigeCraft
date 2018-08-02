package opengl;

import engine.Activatable;
import static engine.Activatable.using;
import static org.lwjgl.opengl.ARBVertexArrayObject.*;

public class VertexArrayObject implements Activatable {

    private final int VAO;

    public static VertexArrayObject createVAO(Runnable r) {
        VertexArrayObject VAO = new VertexArrayObject();
        using(r, VAO);
        return VAO;
    }

    private VertexArrayObject() {
        VAO = glGenVertexArrays();
    }

    @Override
    public void activate() {
        glBindVertexArray(VAO);
    }

    @Override
    public void deactivate() {
        glBindVertexArray(0);
    }

    public void destroy() {
        glDeleteVertexArrays(VAO);
    }
}
