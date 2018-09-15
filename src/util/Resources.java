package util;

import com.esotericsoftware.yamlbeans.YamlException;
import com.esotericsoftware.yamlbeans.YamlReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
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
        return new ShaderProgram(Resources.loadFileAsString("src/shaders/" + name + ".vert"),
                Resources.loadFileAsString("src/shaders/" + name + ".frag"));
    }

    public static ShaderProgram loadShaderProgram(String vertName, String fragName) {
        return new ShaderProgram(Resources.loadFileAsString("src/shaders/" + vertName + ".vert"),
                Resources.loadFileAsString("src/shaders/" + fragName + ".frag"));
    }

    public static ShaderProgram loadShaderProgramGeom(String name) {
        return new ShaderProgram(Resources.loadFileAsString("src/shaders/" + name + ".vert"),
                Resources.loadFileAsString("src/shaders/" + name + ".geom"),
                Resources.loadFileAsString("src/shaders/" + name + ".frag"));
    }

    public static <T> List<T> loadYamlFile(String path, Class<T> c) {
        try {
            List<T> r = new LinkedList();
            YamlReader reader = new YamlReader(Resources.loadFileAsString(path));
            while (true) {
                T object = reader.read(c);
                if (object == null) {
                    break;
                } else {
                    r.add(object);
                }
            }
            return r;
        } catch (YamlException ex) {
            throw new RuntimeException(ex);
        }
    }
}
