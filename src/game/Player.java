package game;

import behaviors.AccelerationBehavior;
import behaviors.PhysicsBehavior;
import behaviors.PositionBehavior;
import behaviors.SpaceOccupierBehavior;
import behaviors.VelocityBehavior;
import engine.Behavior;
import engine.Input;
import java.util.List;
import opengl.Camera;
import static opengl.Camera.camera;
import static org.lwjgl.glfw.GLFW.*;
import util.vectors.Vec3d;
import world.BlockType;
import static world.Raycast.raycastDistance;

public class Player extends Behavior {

    private static final double PLAYER_SCALE = 1;

    public final PositionBehavior position = require(PositionBehavior.class);
    public final VelocityBehavior velocity = require(VelocityBehavior.class);
    public final AccelerationBehavior acceleration = require(AccelerationBehavior.class);
    public final PhysicsBehavior physics = require(PhysicsBehavior.class);
    public final SpaceOccupierBehavior spaceOccupier = require(SpaceOccupierBehavior.class);

    public boolean sprint;

    public Player() {
        acceleration.acceleration = new Vec3d(0, 0, -32).mul(PLAYER_SCALE);
        physics.hitboxSize = new Vec3d(.3, .3, .8).mul(PLAYER_SCALE);
    }

    @Override
    public void update(double dt) {

        Vec3d desCamPos = position.position.add(new Vec3d(0, 0, .6).mul(PLAYER_SCALE));
        //camera.position = desCamPos;
        camera.position = camera.position.lerp(desCamPos, 1 - Math.pow(1e-6, dt));

        // Look around
        camera.horAngle -= Input.mouseDelta().x / 500;
        camera.vertAngle += Input.mouseDelta().y / 500;

        if (camera.vertAngle > 1.5) {
            camera.vertAngle = 1.5f;
        }
        if (camera.vertAngle < -1.5) {
            camera.vertAngle = -1.5f;
        }

        // Move
        if (Input.keyJustPressed(GLFW_KEY_LEFT_CONTROL)) {
            sprint = !sprint;
        }
        double speed = (sprint ? 250 : 4.3) * PLAYER_SCALE;

        Vec3d forwards = camera.facing();
        if (!sprint) {
            forwards = forwards.setZ(0).normalize();
        }
        Vec3d sideways = camera.up.cross(forwards);

        Vec3d idealVel = new Vec3d(0, 0, 0);
        if (Input.keyDown(GLFW_KEY_W)) {
            idealVel = idealVel.add(forwards);
        }
        if (Input.keyDown(GLFW_KEY_A)) {
            idealVel = idealVel.add(sideways);
        }
        if (Input.keyDown(GLFW_KEY_S)) {
            idealVel = idealVel.sub(forwards);
        }
        if (Input.keyDown(GLFW_KEY_D)) {
            idealVel = idealVel.sub(sideways);
        }
        if (idealVel.lengthSquared() > 0) {
            idealVel = idealVel.normalize().mul(speed);
        }

        if (!sprint) {
            idealVel = idealVel.setZ(velocity.velocity.z);
        }

        velocity.velocity = velocity.velocity.lerp(idealVel, 1 - Math.pow(.005, dt));

        // Jump
        if (Input.keyDown(GLFW_KEY_SPACE)) {
            if (physics.onGround || sprint) {
                velocity.velocity = velocity.velocity.setZ((sprint ? 100 : Math.sqrt(4.3) * 5) * PLAYER_SCALE);
            }
        }

        // Break block
        if (Input.mouseJustPressed(0)) {
            List<Vec3d> raycast = raycastDistance(Camera.camera.position, Camera.camera.facing(), 10);
            for (int i = 0; i < raycast.size(); i++) {
                if (physics.world.getBlock(raycast.get(i)) != null) {
                    physics.world.setBlock(raycast.get(i), null);
                    break;
                }
            }
        }
        // Place block
        if (Input.mouseJustPressed(1)) {
            List<Vec3d> raycast = raycastDistance(Camera.camera.position, Camera.camera.facing(), 10);
            for (int i = 0; i < raycast.size() - 1; i++) {
                if (physics.world.getBlock(raycast.get(i)) == null && physics.world.getBlock(raycast.get(i + 1)) != null) {
                    physics.world.setBlock(raycast.get(i), BlockType.WOOD);
                    break;
                }
            }
        }
    }
}
