package util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import opengl.ShaderProgram;

public class Resources {

    public static String loadFileAsString(String path) {
        try {
            return Files.readAllLines(Paths.get(path)).stream().reduce("", (a, b) -> a + "\n" + b);
        } catch (IOException ex) {
            throw new RuntimeException("File not found: " + path);
        }
    }

    public static ShaderProgram loadShaderProgram(String name) {
        return new ShaderProgram(Resources.loadFileAsString("src/shaders/" + name + ".vert"), Resources.loadFileAsString("src/shaders/" + name + ".frag"));
    }
}
