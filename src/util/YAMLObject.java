package util;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;
import util.vectors.Vec2d;

public class YAMLObject {

    public String name;
    public String value;
    public List<YAMLObject> contents;

    public YAMLObject getSubObject(String name) {
        for (YAMLObject o : contents) {
            if (name.equals(o.name)) {
                return o;
            }
        }
        return null;
    }

    public static YAMLObject parse(String fileName) {
        String contents = Resources.loadFileAsString(fileName);
        YAMLObject root = new YAMLObject();
        Stack<YAMLObject> currentStack = new Stack();
        currentStack.add(root);
        for (String line : contents.split("\n")) {
            if (line.contains(":")) {
                int newDepth = line.indexOf(line.trim()) / 4 + 1;
                while (newDepth < currentStack.size()) {
                    currentStack.pop();
                }
                String[] parts = line.split(":");
                YAMLObject newObject = new YAMLObject();
                newObject.name = parts[0].trim();
                if (currentStack.peek().contents == null) {
                    currentStack.peek().contents = new LinkedList();
                }
                currentStack.peek().contents.add(newObject);
                currentStack.push(newObject);
                if (parts.length == 2) {
                    newObject.value = parts[1].trim();
                } else if (parts.length > 2) {
                    throw new RuntimeException("Malformed yaml: " + line);
                }
            }
        }
        return root;
    }

    @Override
    public String toString() {
        return "YAMLObject{" + "name=" + name + ", value=" + value + ", contents=" + contents + '}';
    }

    public double valueToDouble() {
        return Double.parseDouble(value);
    }

    public int valueToInt() {
        return Integer.parseInt(value);
    }

    public List<String> valueToList() {
        return Arrays.asList(value.substring(1, value.length() - 1).split(", "));
    }

    public Vec2d valueToVec2d() {
        String[] components = value.substring(1, value.length() - 1).split(", ");
        return new Vec2d(Double.parseDouble(components[0]), Double.parseDouble(components[1]));
    }
}
