package definitions;

import util.math.Vec2d;
import util.math.Vec3d;

public abstract class Beans {

    public static class Vec2dBean {

        public double x, y;

        public Vec2d toVec2d() {
            return new Vec2d(x, y);
        }
    }

    public static class Vec3dBean {

        public double x, y, z;

        public Vec3d toVec3d() {
            return new Vec3d(x, y, z);
        }
    }
}
