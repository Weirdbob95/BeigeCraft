package opengl;

import engine.Activatable;
import java.nio.ByteBuffer;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.GL_TEXTURE_MAX_LEVEL;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;
import static org.lwjgl.opengl.GL30.glGenerateMipmap;
import static org.lwjgl.stb.STBImage.stbi_failure_reason;
import static org.lwjgl.stb.STBImage.stbi_load;
import static org.lwjgl.stb.STBImage.stbi_set_flip_vertically_on_load;

public class Texture implements Activatable {

    private final int texture, type;
    private int width, height;

    public Texture(int type) {
        texture = glGenTextures();
        this.type = type;
    }

    @Override
    public void activate() {
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(type, texture);
    }

    @Override
    public void deactivate() {
        glBindTexture(type, 0);
    }

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }

    private void setParameter(int name, int value) {
        glTexParameteri(type, name, value);
    }

    private void uploadData(int width, int height, ByteBuffer data) {
        this.width = width;
        this.height = height;
        glTexImage2D(type, 0, GL_RGBA8, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, data);
        glGenerateMipmap(type);
    }

    public static Texture load(String fileName) {
        int[] widthArray = new int[1];
        int[] heightArray = new int[1];
        int[] compArray = new int[1];
        stbi_set_flip_vertically_on_load(true);
        ByteBuffer image = stbi_load("sprites/" + fileName, widthArray, heightArray, compArray, 4);
        if (image == null) {
            throw new RuntimeException("Failed to load image " + fileName + " : " + stbi_failure_reason());
        }

        Texture t = new Texture(GL_TEXTURE_2D);
        t.activate();
        t.setParameter(GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        t.setParameter(GL_TEXTURE_MIN_FILTER, GL_NEAREST_MIPMAP_LINEAR);
        t.setParameter(GL_TEXTURE_MAX_LEVEL, 4);
        t.uploadData(widthArray[0], heightArray[0], image);
        return t;
    }
}
