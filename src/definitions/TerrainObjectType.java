package definitions;

import definitions.Beans.Vec3iBean;
import graphics.Model;
import static java.lang.Math.round;
import static util.math.MathUtils.vecMap;
import util.math.Vec3d;

public class TerrainObjectType {

    public int id;
    public String gameName = null;
    public String displayName = "Missing name";
    public String modelName = null;
    public Vec3iBean size = null;
    public boolean solid = false;
    public double durability = 1;
    public String tool = null;
    public int toolLevel = 0;
    public String onBreak = "same";

    public Model getModel() {
        return Model.load(modelName);
    }

    public Vec3d getSize() {
        if (size == null) {
            return vecMap(getModel().originalSize().div(16), x -> (double) round(x));
        } else {
            return size.toVec3d();
        }
    }
}
