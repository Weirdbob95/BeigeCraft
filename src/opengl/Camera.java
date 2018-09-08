package opengl;

import org.joml.FrustumIntersection;
import org.joml.Matrix4d;
import org.joml.Matrix4f;
import org.joml.Vector2d;
import org.joml.Vector3d;
import util.vectors.Vec2d;
import util.vectors.Vec3d;

public interface Camera {

    public static final Camera2d camera2d = new Camera2d();
    public static final Camera3d camera3d = new Camera3d();

    public Matrix4d getProjectionMatrix();

    public Matrix4d getViewMatrix();

    public static class Camera2d implements Camera {

        public Vector2d position = new Vector2d(0, 0);
        public double rotation = 0;
        public double zoom = 1;

        @Override
        public Matrix4d getProjectionMatrix() {
            return getProjectionMatrix(Window.WIDTH * -.5, Window.WIDTH * .5, Window.HEIGHT * -.5, Window.HEIGHT * .5);
        }

        private Matrix4d getProjectionMatrix(double left, double right, double bottom, double top) {
            Matrix4d projectionMatrix = new Matrix4d();
            projectionMatrix.setOrtho2D(left, right, bottom, top);
            return projectionMatrix;
        }

        @Override
        public Matrix4d getViewMatrix() {
            return new Matrix4d()
                    .scale(zoom)
                    .rotate(rotation, 0, 0, 1)
                    .translate(new Vector3d(-position.x, -position.y, 0));
        }

        public Matrix4d getWorldMatrix(Vec2d position) {
            return getWorldMatrix(position, 0, 1, 1);
        }

        public Matrix4d getWorldMatrix(Vec2d position, double rotation, double scaleX, double scaleY) {
            return getViewMatrix()
                    .translate(new Vector3d(position.x, position.y, 0))
                    .rotate(rotation, 0, 0, 1)
                    .scale(scaleX, scaleY, 1);
        }
    }

    public static class Camera3d implements Camera {

        public Vec3d position = new Vec3d(0, 0, 0);
        public double horAngle, vertAngle;
        public Vec3d up = new Vec3d(0, 0, 1);

        private final FrustumIntersection viewFrustum = new FrustumIntersection();

        public Vec3d facing() {
            return new Vec3d(Math.cos(vertAngle) * Math.cos(horAngle), Math.cos(vertAngle) * Math.sin(horAngle), -Math.sin(vertAngle));
        }

        @Override
        public Matrix4d getProjectionMatrix() {
            return getProjectionMatrix(80, Window.WIDTH, Window.HEIGHT, .2f, 2000);
        }

        private Matrix4d getProjectionMatrix(double fov, double width, double height, double zNear, double zFar) {
            double aspectRatio = width / height;
            Matrix4d projectionMatrix = new Matrix4d();
            projectionMatrix.perspective(fov * Math.PI / 180, aspectRatio, zNear, zFar);
            return projectionMatrix;
        }

        public FrustumIntersection getViewFrustum() {
            viewFrustum.set(new Matrix4f(getProjectionMatrix().mul(getWorldMatrix(new Vec3d(0, 0, 0)))));
            return viewFrustum;
        }

        @Override
        public Matrix4d getViewMatrix() {
            return new Matrix4d()
                    .rotate(vertAngle - Math.PI / 2, new Vector3d(1, 0, 0))
                    .rotate(Math.PI / 2 - horAngle, new Vector3d(0, 0, 1))
                    .translate(position.toJOML().mul(-1, new Vector3d()));
            // Why am I adding/subtracting doubles from the angles? Idk, but it works.
        }

        public Matrix4d getWorldMatrix(Vec3d translate) {
            return getViewMatrix().translate(translate.toJOML());
        }

        public Matrix4d getWorldMatrix(Vec3d translate, double rotation, double scale) {
            return getViewMatrix().translate(translate.toJOML()).rotate(rotation, up.toJOML()).scale(scale);
        }

        public Matrix4d getWorldMatrix(Vec3d translate, double rotation1, double rotation2, double scale) {
            return getViewMatrix().translate(translate.toJOML()).rotate(rotation1, up.toJOML()).rotate(rotation2, new Vector3d(0, 1, 0)).scale(scale);
        }
    }
}
