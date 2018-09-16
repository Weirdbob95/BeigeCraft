package definitions;

import graphics.BlockGUI;
import graphics.Sprite;
import util.math.Vec2d;
import util.math.Vec4d;

public class ItemType {

    public int id;
    public String gameName = null;
    public String displayName = "Missing name";
    public String texture = null;
    public int maxStackSize = 100;
    public WeaponType weapon = null;
    public ToolType tool = null;
    public BlockType blockType = null;
    public TerrainObjectType terrainObjectType = null;

    public void renderGUI(Vec2d pos) {
        if (texture != null) {
            Sprite.load(texture).draw2d(pos, 0, 4, new Vec4d(1, 1, 1, 1));
        }
        if (blockType != null) {
            BlockGUI.load(blockType).draw(pos, 24);
        }
    }
}
