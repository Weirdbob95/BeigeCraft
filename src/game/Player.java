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
import static graphics.VoxelRenderer.DIRS;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import opengl.Camera;
import static opengl.Camera.camera3d;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_A;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_D;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT_CONTROL;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT_SHIFT;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_S;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_SPACE;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_W;
import static util.MathUtils.ceil;
import util.vectors.Vec2d;
import util.vectors.Vec3d;
import util.vectors.Vec4d;
import world.BlockType;
import world.Raycast.RaycastHit;
import static world.Raycast.raycastDistance;

public class Player extends Behavior {

    private static final double PLAYER_SCALE = 2;

    public final PositionBehavior position = require(PositionBehavior.class);
    public final VelocityBehavior velocity = require(VelocityBehavior.class);
    public final AccelerationBehavior acceleration = require(AccelerationBehavior.class);
    public final PhysicsBehavior physics = require(PhysicsBehavior.class);
    public final SpaceOccupierBehavior spaceOccupier = require(SpaceOccupierBehavior.class);

    public boolean flying;
    public boolean crouching;
    public Map<Vec3d, Double> blocksToBreak = new HashMap();

    @Override
    public void createInner() {
        acceleration.acceleration = new Vec3d(0, 0, -32).mul(PLAYER_SCALE);
        physics.hitboxSize1 = new Vec3d(.3, .3, .8).mul(PLAYER_SCALE);
        physics.hitboxSize2 = new Vec3d(.3, .3, .8).mul(PLAYER_SCALE);
        physics.stepUp = true;
    }

    private RaycastHit firstSolid() {
        List<RaycastHit> raycast = raycastDistance(Camera.camera3d.position, Camera.camera3d.facing(), 4 * PLAYER_SCALE);
        for (int i = 0; i < raycast.size(); i++) {
            if (physics.world.getBlock(raycast.get(i).hitPos) != null) {
                return raycast.get(i);
            }
        }
        return null;
    }

    private RaycastHit lastEmpty() {
        List<RaycastHit> raycast = raycastDistance(Camera.camera3d.position, Camera.camera3d.facing(), 4 * PLAYER_SCALE);
        for (int i = 0; i < raycast.size() - 1; i++) {
            if (physics.world.getBlock(raycast.get(i).hitPos) != null) {
                return null;
            }
            if (physics.world.getBlock(raycast.get(i + 1).hitPos) != null) {
                return raycast.get(i);
            }
        }
        return null;
    }

    @Override
    public void render() {
        Animation blockBreak = Animation.load("blockbreak_anim");
        for (Entry<Vec3d, Double> e : blocksToBreak.entrySet()) {
            if (e.getValue() > .05) {
                Sprite s = blockBreak.getSpriteOrNull("", (int) (4 * (e.getValue() - .05) / .5));
                for (Vec3d dir : DIRS) {
                    Vec3d drawPos = e.getKey().floor().add(new Vec3d(.5, .5, .5)).sub(dir.mul(.501));
                    s.draw3d(drawPos, dir, 0, new Vec2d(1, 1), new Vec4d(1, 1, 1, .5));
                }
            }
        }
    }

    @Override
    public void update(double dt) {

        Vec3d desCamPos = position.position.add(new Vec3d(0, 0, crouching ? .4 : .65).mul(PLAYER_SCALE));
        //camera.position = desCamPos;
        camera3d.position = camera3d.position.lerp(desCamPos, 1 - Math.pow(1e-6, dt));

        // Look around
        camera3d.horAngle -= Input.mouseDelta().x / 500;
        camera3d.vertAngle += Input.mouseDelta().y / 500;

        if (camera3d.vertAngle > 1.55) {
            camera3d.vertAngle = 1.55f;
        }
        if (camera3d.vertAngle < -1.55) {
            camera3d.vertAngle = -1.55f;
        }

        // Move
        if (Input.keyJustPressed(GLFW_KEY_LEFT_CONTROL)) {
            flying = !flying;
        }
        double speed = (flying ? 100 : crouching ? 2 : 4) * PLAYER_SCALE;

        Vec3d forwards = camera3d.facing();
        if (!flying) {
            forwards = forwards.setZ(0).normalize();
        }
        Vec3d sideways = camera3d.up.cross(forwards);

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

        if (!flying) {
            idealVel = idealVel.setZ(velocity.velocity.z);
        }

        velocity.velocity = velocity.velocity.lerp(idealVel, 1 - Math.pow(.005, dt));

        // Jump
        if (Input.keyDown(GLFW_KEY_SPACE)) {
            if (physics.onGround || flying) {
                velocity.velocity = velocity.velocity.setZ((flying ? 100 : Math.sqrt(4.3) * 5) * PLAYER_SCALE);
            }
        }

        // Crouch
        if (Input.keyDown(GLFW_KEY_LEFT_SHIFT)) {
            if (!crouching) {
                crouching = true;
                physics.hitboxSize2 = new Vec3d(.3, .3, .6).mul(PLAYER_SCALE);
            }
        } else {
            if (crouching) {
                if (physics.couldChangeHitboxSize(physics.hitboxSize1, new Vec3d(.3, .3, .8).mul(PLAYER_SCALE))) {
                    crouching = false;
                    physics.hitboxSize2 = new Vec3d(.3, .3, .8).mul(PLAYER_SCALE);
                }
            }
        }

        // Break block
        if (Input.mouseDown(0)) {
            RaycastHit block = firstSolid();
            if (block != null) {
                int handSize = ceil(PLAYER_SCALE);
                Vec3d origin = block.hitPos
                        //.add(block.hitDir.mul(handSize / 2.))
                        .sub(new Vec3d(1, 1, 1).mul(.5 * (handSize - 1)));
                List<Vec3d> targets = new ArrayList();
                for (int x = 0; x < handSize; x++) {
                    for (int y = 0; y < handSize; y++) {
                        for (int z = 0; z < handSize; z++) {
                            if (physics.world.getBlock(origin.add(new Vec3d(x, y, z))) != null) {
                                targets.add(origin.add(new Vec3d(x, y, z)).floor());
                            }
                        }
                    }
                }
                for (Vec3d v : new ArrayList<>(blocksToBreak.keySet())) {
                    if (!targets.contains(v)) {
                        blocksToBreak.remove(v);
                    }
                }
                for (Vec3d v : targets) {
                    blocksToBreak.putIfAbsent(v, 0.);
                    blocksToBreak.put(v, blocksToBreak.get(v) + dt * 8 / targets.size());
                    if (blocksToBreak.get(v) > .5) {
                        physics.world.setBlock(v, null);
                        blocksToBreak.remove(v);
                    }
                }
            } else {
                blocksToBreak.clear();
            }
        } else {
            blocksToBreak.clear();
        }

        // Place block
        if (Input.mouseJustPressed(1)) {
            RaycastHit block = lastEmpty();
            if (block != null) {
                physics.world.setBlock(block.hitPos, BlockType.WOOD);
            }
        }
    }
}
