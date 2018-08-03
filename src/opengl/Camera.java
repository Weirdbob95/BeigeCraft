package opengl;

import org.joml.FrustumIntersection;
import org.joml.Matrix4d;
import org.joml.Matrix4f;
import org.joml.Vector3d;
import util.vectors.Vec3d;

public class Camera {

    public static Camera camera = new Camera();

    public Vec3d position = new Vec3d(0, 0, 0);
    public double horAngle, vertAngle;
    public Vec3d up = new Vec3d(0, 0, 1);

    private final FrustumIntersection viewFrustum = new FrustumIntersection();

    public FrustumIntersection getViewFrustum() {
        viewFrustum.set(new Matrix4f(Camera.getProjectionMatrix().mul(Camera.camera.getWorldMatrix(new Vec3d(0, 0, 0)))));
        return viewFrustum;
    }

    private Matrix4d getViewMatrix() {
        return new Matrix4d()
                .rotate(vertAngle - Math.PI / 2, new Vector3d(1, 0, 0))
                .rotate(Math.PI / 2 - horAngle, new Vector3d(0, 0, 1))
                .translate(position.toJOML().mul(-1, new Vector3d()));

        // Why am I adding/subtracting doubles from the angles? Idk, but it works.
    }

    public Matrix4d getWorldMatrix(Vec3d translate) {
        return getViewMatrix().translate(translate.toJOML());
    }

    public static Matrix4d getProjectionMatrix() {
        return getProjectionMatrix(Math.PI / 2, Window.WIDTH, Window.HEIGHT, .2f, 2000);
    }

    private static Matrix4d getProjectionMatrix(double fov, double width, double height, double zNear, double zFar) {
        double aspectRatio = width / height;
        Matrix4d projectionMatrix = new Matrix4d();
        projectionMatrix.perspective(fov, aspectRatio, zNear, zFar);
        return projectionMatrix;
    }

    public Vec3d facing() {
        return new Vec3d(Math.cos(vertAngle) * Math.cos(horAngle), Math.cos(vertAngle) * Math.sin(horAngle), -Math.sin(vertAngle));
    }
}
