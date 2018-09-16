package definitions;

import util.math.Vec2d;
import util.math.Vec3d;

public abstract class Beans {

    public static class Vec2dBean {

        public double x, y;

        public Vec2dBean() {
        }

        public Vec2dBean(double x, double y) {
            this.x = x;
            this.y = y;
        }

        public Vec2d toVec2d() {
            return new Vec2d(x, y);
        }
    }

    public static class Vec2iBean {

        public int x, y;

        public Vec2iBean() {
        }

        public Vec2iBean(int x, int y) {
            this.x = x;
            this.y = y;
        }

        public Vec2d toVec2d() {
            return new Vec2d(x, y);
        }
    }

    public static class Vec3dBean {

        public double x, y, z;

        public Vec3dBean() {
        }

        public Vec3dBean(double x, double y, double z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }

        public Vec3d toVec3d() {
            return new Vec3d(x, y, z);
        }
    }

    public static class Vec3iBean {

        public int x, y, z;

        public Vec3iBean() {
        }

        public Vec3iBean(int x, int y, int z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }

        public Vec3d toVec3d() {
            return new Vec3d(x, y, z);
        }
    }
}
