package definitions;

import definitions.Beans.Vec3dBean;
import graphics.Model;
import util.math.Vec3d;

public class WeaponType {

    public static final WeaponType FIST = new WeaponType();

    static {
        FIST.modelName = "fist.vox";
        FIST.modelTip = new Vec3dBean();
        FIST.modelTip.x = -8;
        FIST.modelTip.y = 2;
        FIST.modelTip.z = 4;
        FIST.ext1 = 1;
        FIST.ext2 = 2.5;
        FIST.slashDuration = .15;
        FIST.weight = 2;
        FIST.slashiness = .2;
    }

    public String modelName;
    public Vec3dBean modelTip;
    public double ext1, ext2;
    public double slashDuration;
    public double weight;
    public double slashiness;

    public Model getModel() {
        return Model.load(modelName);
    }

    public Vec3d getModelTip() {
        return modelTip.toVec3d();
    }
}
