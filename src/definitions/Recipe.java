package definitions;

import static definitions.ItemType.getItem;
import game.items.ItemSlot;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import util.YAMLObject;
import util.math.Vec2d;

public class Recipe {

    private static final ArrayList<Recipe> RECIPE_LIST = new ArrayList();

    static {
        YAMLObject root = YAMLObject.parse("definitions/crafting_definitions.txt");
        for (YAMLObject recipe : root.contents) {
            Recipe r = new Recipe();
            r.shapeless = recipe.name.contains("shapeless");
            processParams(r, root, recipe.contents);
            RECIPE_LIST.add(r);
        }
    }

    private static void processParams(Recipe r, YAMLObject root, List<YAMLObject> params) {
        for (YAMLObject param : params) {
            switch (param.name) {
                case "input":
                    if (r.shapelessInput == null) {
                        r.shapelessInput = new LinkedList();
                    }
                    r.shapelessInput.add(getItem(param.value));
                    break;
                case "output":
                    r.output = getItem(param.value);
                    break;
                case "output_num":
                    r.outputNum = param.valueToInt();
                    break;
                case "size":
                    Vec2d size = param.valueToVec2d();
                    r.sizeX = (int) size.x;
                    r.sizeY = (int) size.y;
                    break;
                case "row1":
                    r.row1 = param.valueToList().stream().map(ItemType::getItem).collect(Collectors.toList());
                    break;
                case "row2":
                    r.row2 = param.valueToList().stream().map(ItemType::getItem).collect(Collectors.toList());
                    break;
                case "row3":
                    r.row3 = param.valueToList().stream().map(ItemType::getItem).collect(Collectors.toList());
                    break;
                default:
                    throw new RuntimeException("Unknown parameter: " + param.name);
            }
        }
    }

    private boolean shapeless;
    private List<ItemType> shapelessInput;
    private ItemType output;
    private int outputNum;
    private int sizeX, sizeY;
    private List<ItemType> row1, row2, row3;

    private Recipe() {
    }

    public static Recipe findMatching(ItemSlot[] grid) {
        return RECIPE_LIST.stream().filter(i -> i.matches(grid)).findFirst().orElse(null);
    }

    public boolean matches(ItemSlot[] grid) {
        if (shapeless) {
            List<ItemType> inputs = new ArrayList();
            for (ItemSlot i : grid) {
                if (!i.isEmpty()) {
                    inputs.add(i.item());
                }
            }
            return inputs.equals(shapelessInput);
        } else {
            int gridSize = (int) Math.sqrt(grid.length);
            for (int x1 = 0; x1 <= gridSize - sizeX; x1++) {
                for (int y1 = 0; y1 <= gridSize - sizeY; y1++) {
                    boolean posWorks = true;
                    for (int x = 0; x < gridSize; x++) {
                        for (int y = 0; y < gridSize; y++) {
                            boolean inGuess = x >= x1 && x < x1 + sizeX && y >= y1 && y < y1 + sizeY;
                            ItemType expected = inGuess ? (((y == y1) ? row1 : (y == y1 + 1) ? row2 : row3).get(x - x1)) : null;
                            if (expected != grid[x + gridSize * y].item()) {
                                posWorks = false;
                            }
                        }
                    }
                    if (posWorks) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public ItemType output() {
        return output;
    }

    public int outputNum() {
        return outputNum;
    }
}
