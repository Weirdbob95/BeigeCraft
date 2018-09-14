package game;

import behaviors.PhysicsBehavior;
import behaviors.PositionBehavior;
import behaviors.VelocityBehavior;
import definitions.ItemType;
import engine.Behavior;
import engine.Input;
import static game.abilities.Ability.DO_NOTHING;
import game.abilities.AbilityController;
import game.abilities.ParryAbility;
import game.abilities.WeaponChargeAbility;
import game.creatures.CreatureBehavior;
import game.inventory.ItemSlot;
import graphics.Animation;
import graphics.Sprite;
import static graphics.VoxelRenderer.DIRS;
import gui.GUIManager;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import opengl.Camera;
import static opengl.Camera.camera3d;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_A;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_C;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_D;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT_CONTROL;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT_SHIFT;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_S;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_SPACE;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_W;
import static util.math.MathUtils.round;
import static util.math.MathUtils.vecMap;
import util.math.Vec2d;
import util.math.Vec3d;
import util.math.Vec4d;
import world.Raycast.RaycastHit;
import static world.Raycast.raycastDistance;

public class Player extends Behavior {

    public final PositionBehavior position = require(PositionBehavior.class);
    public final VelocityBehavior velocity = require(VelocityBehavior.class);
    public final CreatureBehavior creature = require(CreatureBehavior.class);
    public final PhysicsBehavior physics = require(PhysicsBehavior.class);
    public final HeldItemController heldItemController = require(HeldItemController.class);
    public final AbilityController abilityController = require(AbilityController.class);

    public GUIManager gui;
    public boolean flying;
    public boolean breakingBlocks;
    public Map<Vec3d, Double> blocksToBreak = new HashMap();

    public double sprintTimer;

    public Vec3d computeIdealVel() {
        Vec3d idealVel = new Vec3d(0, 0, 0);

        if (!gui.freezeMovement()) {
            Vec3d forwards = camera3d.facing();
            if (!flying) {
                forwards = forwards.setZ(0).normalize();
            }
            Vec3d sideways = camera3d.up.cross(forwards);

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
                idealVel = idealVel.setLength(creature.getSpeed());
            } else {
                if (sprintTimer > 0) {
                    idealVel = idealVel.add(forwards);
                    idealVel = idealVel.setLength(creature.getSpeed());
                }
            }
        }

        if (!flying) {
            idealVel = idealVel.setZ(velocity.velocity.z);
        }

        return idealVel;
    }

    @Override
    public void createInner() {
        physics.canCrouch = true;
        physics.hitboxSize1 = new Vec3d(.6, .6, 1.8);
        physics.hitboxSize2 = new Vec3d(.6, .6, 1.8);
        physics.hitboxSize1Crouch = new Vec3d(.6, .6, 1.8);
        physics.hitboxSize2Crouch = new Vec3d(.6, .6, 1.0);
        heldItemController.eye.eyePos = () -> Camera.camera3d.position;
    }

    public RaycastHit firstSolid() {
        List<RaycastHit> raycast = raycastDistance(Camera.camera3d.position, Camera.camera3d.facing(), 8);
        for (int i = 0; i < raycast.size(); i++) {
            if (physics.world.getBlock(raycast.get(i).hitPos) != null) {
                return raycast.get(i);
            }
        }
        return null;
    }

    public RaycastHit lastEmpty() {
        List<RaycastHit> raycast = raycastDistance(Camera.camera3d.position, Camera.camera3d.facing(), 8);
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
        if (breakingBlocks) {
            Animation blockBreak = Animation.load("blockbreak_anim");
            for (Entry<Vec3d, Double> e : blocksToBreak.entrySet()) {
                if (e.getValue() > .05) {
                    Sprite s = blockBreak.getSpriteOrNull("", (int) (blockBreak.length * (e.getValue() - .05)));
                    for (Vec3d dir : DIRS) {
                        Vec3d drawPos = e.getKey().floor().add(new Vec3d(.5, .5, .5)).sub(dir.mul(.501));
                        s.draw3d(drawPos, dir, 0, new Vec2d(1, 1), new Vec4d(1, 1, 1, .5));
                    }
                }
            }
        } else {
            blocksToBreak.clear();
        }
    }

    @Override
    public double renderLayer() {
        return 3;
    }

    @Override
    public void update(double dt) {
        sprintTimer -= dt;
        breakingBlocks = false;
        gui.hud.update(this);

        Vec3d desCamPos = position.position.add(new Vec3d(0, 0, physics.crouch ? .8 : 1.4));
        camera3d.position = camera3d.position.lerp(desCamPos, 1 - Math.pow(1e-8, dt));

        if (!gui.freezeMouse()) {
            // Look around
            camera3d.horAngle -= Input.mouseDelta().x / 300;
            camera3d.vertAngle += Input.mouseDelta().y / 300;

            if (camera3d.vertAngle > 1.55) {
                camera3d.vertAngle = 1.55f;
            }
            if (camera3d.vertAngle < -1.55) {
                camera3d.vertAngle = -1.55f;
            }
        }

        heldItemController.eye.facing = Camera.camera3d.facing();
        if (Input.mouseJustPressed(0)) {
            abilityController.attemptAbility(new WeaponChargeAbility());
        }
        if (Input.mouseJustReleased(0)) {
            abilityController.attemptAbility(DO_NOTHING);
        }
        if (Input.mouseJustPressed(1)) {
            abilityController.attemptAbility(new ParryAbility());
        }

        if (!gui.freezeMovement()) {

            if (Input.keyJustPressed(GLFW_KEY_LEFT_CONTROL)) {
                flying = !flying;
            }

            creature.speed = flying ? 200 : physics.crouch ? 4 : sprintTimer > 0 ? 40 : 8;
            creature.jumpSpeed = flying ? 200 : 24;

            // Crouch
            physics.shouldCrouch = Input.keyDown(GLFW_KEY_LEFT_SHIFT);

//            if (Input.mouseJustPressed(1)) {
////                sprintTimer = .2;
//                parryTimer = .4;
//            }
        }

        velocity.velocity = computeIdealVel();

        if (!gui.freezeMovement()) {
            // Jump
            if (Input.keyDown(GLFW_KEY_SPACE)) {
                if (physics.onGround || flying) {
                    velocity.velocity = velocity.velocity.setZ(creature.jumpSpeed);
                }
            }
        }

        if (!gui.freezeMouse()) {
            // Use items
            ItemType mainItem = ItemSlot.MAIN_HAND == null ? null : ItemSlot.MAIN_HAND.item();
            if (Input.mouseJustPressed(0)) {
                if (mainItem != null) {
                    mainItem.use().useItemPress(this, true);
                }
            }
            if (Input.mouseDown(0)) {
                if (mainItem != null) {
                    mainItem.use().useItemHold(this, true, dt);
                }
            }
            ItemType offItem = ItemSlot.OFF_HAND == null ? null : ItemSlot.OFF_HAND.item();
            if (Input.mouseJustPressed(1)) {
                if (offItem != null) {
                    offItem.use().useItemPress(this, false);
                }
            }
            if (Input.mouseDown(1)) {
                if (offItem != null) {
                    offItem.use().useItemHold(this, false, dt);
                }
            }
        }

        if (Input.keyJustPressed(GLFW_KEY_C)) {
            RaycastHit block = firstSolid();
            if (block != null) {
                Chest c = new Chest();
                c.model.position.position = vecMap(block.hitPos, x -> (double) round(x)).sub(block.hitDir);
                c.create();
            }
        }
    }
}
