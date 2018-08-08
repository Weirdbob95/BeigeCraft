package game;

import behaviors.AccelerationBehavior;
import behaviors.PhysicsBehavior;
import behaviors.PositionBehavior;
import behaviors.SpaceOccupierBehavior;
import behaviors.VelocityBehavior;
import engine.Behavior;
import engine.Input;
import graphics.Animation;
import graphics.Sprite;
import java.util.List;
import opengl.Camera;
import static opengl.Camera.camera;
import static org.lwjgl.glfw.GLFW.*;
import static util.MathUtils.vecMap;
import util.vectors.Vec2d;
import util.vectors.Vec3d;
import util.vectors.Vec4d;
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
    public Vec3d blockToBreak;
    public double breakTimer;

    public Player() {
        acceleration.acceleration = new Vec3d(0, 0, -32).mul(PLAYER_SCALE);
        physics.hitboxSize = new Vec3d(.3, .3, .8).mul(PLAYER_SCALE);
        physics.stepUp = true;
    }

    @Override
    public void render() {
        Animation blockBreak = Animation.load("blockbreak_anim");
        if (blockToBreak != null) {
            Sprite s = blockBreak.getSpriteOrNull("", (int) (4 * breakTimer / .5));
            s.draw(blockToBreak, new Vec3d(1, 0, 0), 0, new Vec2d(1, 1), new Vec4d(1, 1, 1, 1));
        }
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
        if (Input.mouseDown(0)) {
            Vec3d block = firstSolid();
            if (block != null) {
                if (blockToBreak == null || !vecMap(block, Math::floor).equals(vecMap(blockToBreak, Math::floor))) {
                    breakTimer = 0;
                }
                blockToBreak = block;
                breakTimer += dt;
                if (breakTimer > .5) {
                    physics.world.setBlock(block, null);
                    blockToBreak = null;
                    breakTimer = 0;
                }
            }
        } else {
            breakTimer = 0;
        }
        // Place block
        if (Input.mouseJustPressed(1)) {
            Vec3d block = lastEmpty();
            if (block != null) {
                physics.world.setBlock(block, BlockType.WOOD);
            }
        }
    }

    private Vec3d firstSolid() {
        List<Vec3d> raycast = raycastDistance(Camera.camera.position, Camera.camera.facing(), 5 * PLAYER_SCALE);
        for (int i = 0; i < raycast.size(); i++) {
            if (physics.world.getBlock(raycast.get(i)) != null) {
                return raycast.get(i);
            }
        }
        return null;
    }

    private Vec3d lastEmpty() {
        List<Vec3d> raycast = raycastDistance(Camera.camera.position, Camera.camera.facing(), 5 * PLAYER_SCALE);
        for (int i = 0; i < raycast.size() - 1; i++) {
            if (physics.world.getBlock(raycast.get(i)) != null) {
                return null;
            }
            if (physics.world.getBlock(raycast.get(i + 1)) != null) {
                return raycast.get(i);
            }
        }
        return null;
    }
}
