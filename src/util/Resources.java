package util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import opengl.ShaderProgram;

public class Resources {

    public static byte[] loadFileAsBytes(String path) {
        try {
            return Files.readAllBytes(Paths.get(path));
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    public static String loadFileAsString(String path) {
        return new String(loadFileAsBytes(path));
    }

    public static ShaderProgram loadShaderProgram(String name) {
        return new ShaderProgram(Resources.loadFileAsString("src/shaders/" + name + ".vert"), Resources.loadFileAsString("src/shaders/" + name + ".frag"));
    }
}
