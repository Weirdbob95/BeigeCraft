package opengl;

import static graphics.Sprite.SPRITE_VAO;
import static org.lwjgl.opengl.GL11.GL_LINEAR;
import static org.lwjgl.opengl.GL11.GL_RGBA;
import static org.lwjgl.opengl.GL11.GL_RGBA8;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MAG_FILTER;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MIN_FILTER;
import static org.lwjgl.opengl.GL11.GL_TRIANGLE_FAN;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_BYTE;
import static org.lwjgl.opengl.GL11.glDrawArrays;
import static org.lwjgl.opengl.GL11.glTexImage2D;
import static org.lwjgl.opengl.GL20.glDrawBuffers;
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
import util.vectors.Vec2d;
import util.vectors.Vec4d;

public class Framebuffer extends GLObject {

    public final Texture colorBuffer;
    public final Texture colorBuffer2;
    public final Texture depthStencilBuffer;

    public Framebuffer(boolean useColorBuffer, boolean useColorBuffer2, boolean useDepthStencilBuffer) {
        super(glGenFramebuffers());
        Framebuffer oldFramebuffer = GLState.getFramebuffer();
        bind();

        if (useColorBuffer) {
            colorBuffer = new Texture(GL_TEXTURE_2D);
            colorBuffer.bind();
            glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, Window.WIDTH, Window.HEIGHT, 0, GL_RGBA, GL_UNSIGNED_BYTE, 0);
            colorBuffer.setParameter(GL_TEXTURE_MIN_FILTER, GL_LINEAR);
            colorBuffer.setParameter(GL_TEXTURE_MAG_FILTER, GL_LINEAR);
            glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, colorBuffer.id, 0);
        } else {
            colorBuffer = null;
        }

        if (useColorBuffer2) {
            colorBuffer2 = new Texture(GL_TEXTURE_2D);
            colorBuffer2.bind();
            glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, Window.WIDTH, Window.HEIGHT, 0, GL_RGBA, GL_UNSIGNED_BYTE, 0);
            colorBuffer2.setParameter(GL_TEXTURE_MIN_FILTER, GL_LINEAR);
            colorBuffer2.setParameter(GL_TEXTURE_MAG_FILTER, GL_LINEAR);
            glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT1, GL_TEXTURE_2D, colorBuffer2.id, 0);

            glDrawBuffers(new int[]{GL_COLOR_ATTACHMENT0, GL_COLOR_ATTACHMENT1});
        } else {
            colorBuffer2 = null;
        }

        if (useDepthStencilBuffer) {
            depthStencilBuffer = new Texture(GL_TEXTURE_2D);
            depthStencilBuffer.bind();
            glTexImage2D(GL_TEXTURE_2D, 0, GL_DEPTH24_STENCIL8, Window.WIDTH, Window.HEIGHT, 0, GL_DEPTH_STENCIL, GL_UNSIGNED_INT_24_8, 0);
            depthStencilBuffer.setParameter(GL_TEXTURE_MIN_FILTER, GL_LINEAR);
            depthStencilBuffer.setParameter(GL_TEXTURE_MAG_FILTER, GL_LINEAR);
            glFramebufferTexture2D(GL_FRAMEBUFFER, GL_DEPTH_STENCIL_ATTACHMENT, GL_TEXTURE_2D, depthStencilBuffer.id, 0);
        } else {
            depthStencilBuffer = null;
        }

        GLState.bindFramebuffer(oldFramebuffer);
    }

    @Override
    public void bind() {
        GLState.bindFramebuffer(this);
    }

    @Override
    public void destroy() {
        glDeleteFramebuffers(id);
    }

    public static void draw(Texture texture, ShaderProgram shader) {
        shader.setUniform("projectionMatrix", Camera.camera2d.getProjectionMatrix());
        shader.setUniform("modelViewMatrix", Camera.camera2d.getWorldMatrix(new Vec2d(0, 0), 0, Window.WIDTH, Window.HEIGHT));
        shader.setUniform("color", new Vec4d(1, 1, 1, 1));
        bindAll(texture, shader, SPRITE_VAO);
        glDrawArrays(GL_TRIANGLE_FAN, 0, 4);
    }
}
