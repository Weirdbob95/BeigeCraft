package definitions;

import definitions.Beans.Vec3dBean;
import graphics.Model;
import util.math.Vec3d;

public class WeaponType {

    public static final WeaponType FIST = new WeaponType();

    static {
        FIST.modelName = "fist.vox";
        FIST.modelTip = new Vec3dBean(-8, 2, 4);
        FIST.ext1 = 1;
        FIST.ext2 = 2.5;
        FIST.attackDuration = .5;
        FIST.knockback = 2;
        FIST.hand1 = 0;
        FIST.hand2 = 0;
    }

    public String modelName;
    public Vec3dBean modelTip;
    public double ext1, ext2;
    public double attackDuration;
    public double knockback;
    public double hand1 = Double.NaN, hand2 = Double.NaN;
    public Vec3dBean handlePos;
    public Vec3dBean restingPos;
    public double slashiness = 1;

    public Vec3d getHandlePos() {
        return handlePos.toVec3d();
    }

    public Model getModel() {
        return Model.load(modelName);
    }

    public Vec3d getModelTip() {
        return modelTip.toVec3d();
    }

    public Vec3d getRestingPos() {
        return restingPos.toVec3d();
    }
}
