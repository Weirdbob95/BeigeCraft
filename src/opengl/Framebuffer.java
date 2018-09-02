package opengl;

import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL11.GL_LINEAR;
import static org.lwjgl.opengl.GL11.GL_RGBA;
import static org.lwjgl.opengl.GL11.GL_RGBA8;
import static org.lwjgl.opengl.GL11.GL_STENCIL_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MAG_FILTER;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MIN_FILTER;
import static org.lwjgl.opengl.GL11.GL_TRIANGLE_FAN;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_BYTE;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glClearColor;
import static org.lwjgl.opengl.GL11.glDrawArrays;
import static org.lwjgl.opengl.GL11.glTexImage2D;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.GL_COLOR_ATTACHMENT0;
import static org.lwjgl.opengl.GL30.GL_COLOR_ATTACHMENT1;
import static org.lwjgl.opengl.GL30.GL_DEPTH24_STENCIL8;
import static org.lwjgl.opengl.GL30.GL_DEPTH_STENCIL;
import static org.lwjgl.opengl.GL30.GL_DEPTH_STENCIL_ATTACHMENT;
import static org.lwjgl.opengl.GL30.GL_FRAMEBUFFER;
import static org.lwjgl.opengl.GL30.GL_UNSIGNED_INT_24_8;
import static org.lwjgl.opengl.GL30.glDeleteFramebuffers;
import static org.lwjgl.opengl.GL30.glFramebufferTexture2D;
import static org.lwjgl.opengl.GL30.glGenFramebuffers;
import util.vectors.Vec4d;

public class Framebuffer extends GLObject {

    private static final VertexArrayObject FRAMEBUFFER_VAO = VertexArrayObject.createVAO(() -> {
        BufferObject vbo = new BufferObject(GL_ARRAY_BUFFER, new float[]{
            -1, -1, 0, 0,
            1, -1, 1, 0,
            1, 1, 1, 1,
            -1, 1, 0, 1
        });
        glVertexAttribPointer(0, 2, GL_FLOAT, false, 16, 0);
        glEnableVertexAttribArray(0);
        glVertexAttribPointer(1, 2, GL_FLOAT, false, 16, 8);
        glEnableVertexAttribArray(1);
    });

    public final Texture colorBuffer;
    public final Texture colorBuffer2;
    public final Texture depthStencilBuffer;

    public Framebuffer(boolean useColorBuffer, boolean useColorBuffer2, boolean useDepthStencilBuffer) {
        super(glGenFramebuffers());
        Framebuffer oldFramebuffer = GLState.getFramebuffer();
        bind();

        if (useColorBuffer) {
            colorBuffer = fullscreenTexture(GL_RGBA8, GL_RGBA, GL_UNSIGNED_BYTE, GL_COLOR_ATTACHMENT0);
        } else {
            colorBuffer = null;
        }

        if (useColorBuffer2) {
            colorBuffer2 = fullscreenTexture(GL_RGBA8, GL_RGBA, GL_UNSIGNED_BYTE, GL_COLOR_ATTACHMENT1);
        } else {
            colorBuffer2 = null;
        }

        if (useDepthStencilBuffer) {
            depthStencilBuffer = fullscreenTexture(GL_DEPTH24_STENCIL8, GL_DEPTH_STENCIL, GL_UNSIGNED_INT_24_8, GL_DEPTH_STENCIL_ATTACHMENT);
        } else {
            depthStencilBuffer = null;
        }

        GLState.bindFramebuffer(oldFramebuffer);
    }

    @Override
    public void bind() {
        GLState.bindFramebuffer(this);
    }

    public void clear(Vec4d color) {
        bind();
        glClearColor((float) color.x, (float) color.y, (float) color.z, (float) color.w);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT | GL_STENCIL_BUFFER_BIT);
    }

    public static void clearWindow(Vec4d color) {
        GLState.bindFramebuffer(null);
        glClearColor((float) color.x, (float) color.y, (float) color.z, (float) color.w);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT | GL_STENCIL_BUFFER_BIT);
    }

    @Override
    public void destroy() {
        glDeleteFramebuffers(id);
    }

    public void drawToSelf(Texture texture, ShaderProgram shader) {
        bindAll(this, texture, shader, FRAMEBUFFER_VAO);
        glDrawArrays(GL_TRIANGLE_FAN, 0, 4);
    }

    public static void drawToWindow(Texture texture, ShaderProgram shader) {
        GLState.bindFramebuffer(null);
        bindAll(texture, shader, FRAMEBUFFER_VAO);
        glDrawArrays(GL_TRIANGLE_FAN, 0, 4);
    }

    private static Texture fullscreenTexture(int gpuFormat, int storageType, int cpuFormat, int attachmentType) {
        Texture t = new Texture(GL_TEXTURE_2D);
        t.bind();
        glTexImage2D(GL_TEXTURE_2D, 0, gpuFormat, Window.WIDTH, Window.HEIGHT, 0, storageType, cpuFormat, 0);
        t.setParameter(GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        t.setParameter(GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glFramebufferTexture2D(GL_FRAMEBUFFER, attachmentType, GL_TEXTURE_2D, t.id, 0);
        return t;
    }
}
