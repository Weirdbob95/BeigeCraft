package definitions;

import definitions.Beans.Vec2iBean;
import util.math.Vec3d;

public class BlockType {

    public int id;
    public String gameName = null;
    public String displayName = "Missing name";
    public DiffTopBotTextureType diffTopBotTexture = null;
    public UniformTextureType uniformTexture = null;
    public double durability = 1;
    public String tool = null;
    public int toolLevel = 0;
    public String onBreak = "same";

    public BlockType getOnBreak() {
        if (onBreak.equals("none")) {
            return null;
        }
        if (onBreak.equals("same")) {
            return this;
        }
        return Loader.getBlock(onBreak);
    }

    public int getTexID(Vec3d dir) {
        if (diffTopBotTexture != null) {
            if (dir.z > 0) {
                return (int) diffTopBotTexture.top.x + (int) diffTopBotTexture.top.y * 256;
            } else if (dir.z < 0) {
                return (int) diffTopBotTexture.bot.x + (int) diffTopBotTexture.bot.y * 256;
            } else {
                return (int) diffTopBotTexture.side.x + (int) diffTopBotTexture.side.y * 256;
            }
        }
        if (uniformTexture != null) {
            return (int) uniformTexture.pos.x + (int) uniformTexture.pos.y * 256;
        }
        throw new RuntimeException("Texture not defined for block type: " + gameName);
    }

    public static class DiffTopBotTextureType {

        public Vec2iBean top, bot, side;
    }

    public static class UniformTextureType {

        public Vec2iBean pos;
    }
}
