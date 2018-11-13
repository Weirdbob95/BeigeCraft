package game.archetypes;

import behaviors.PhysicsBehavior;
import behaviors.PositionBehavior;
import behaviors.VelocityBehavior;
import engine.Behavior;
import engine.Input;
import game.abilities.AbilityController;
import game.creatures.CreatureBehavior;
import game.items.HeldItemController;
import gui.GUIManager;
import opengl.Camera;
import static opengl.Camera.camera3d;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_A;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_D;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT_ALT;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT_CONTROL;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_S;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_SPACE;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_W;
import util.math.MathUtils;
import util.math.Vec3d;

public class Knight extends Behavior {

    public final PositionBehavior position = require(PositionBehavior.class);
    public final VelocityBehavior velocity = require(VelocityBehavior.class);
    public final CreatureBehavior creature = require(CreatureBehavior.class);
    public final PhysicsBehavior physics = require(PhysicsBehavior.class);
    public final HeldItemController heldItemController = require(HeldItemController.class);
    public final AbilityController abilityController = require(AbilityController.class);

    public GUIManager gui;
    public boolean flying;

    @Override
    public void createInner() {
        physics.canCrouch = true;
        physics.hitboxSize1 = new Vec3d(.6, .6, 1.8);
        physics.hitboxSize2 = new Vec3d(.6, .6, 1.8);
        physics.hitboxSize1Crouch = new Vec3d(.6, .6, 1.8);
        physics.hitboxSize2Crouch = new Vec3d(.6, .6, 1.0);
        heldItemController.eye.eyePos = () -> Camera.camera3d.position;
//        heldItemController.heldItemType = getItem("sword").weapon;
        heldItemController.rotateShouldersWithFacing = true;
        heldItemController.armWidth = 3. / 16;
    }

    @Override
    public void update(double dt) {
        // Update camera
        Vec3d desiredCameraPosition = position.position.add(new Vec3d(0, 0, physics.crouch ? .8 : 1.4));
        camera3d.position = (camera3d.position.add(velocity.velocity.mul(dt))).lerp(desiredCameraPosition, 1 - Math.pow(1e-8, dt));
        // Update other things
        gui.hud.update(this);
        heldItemController.eye.facing = Camera.camera3d.facing();
        // Modify velocity
        Vec3d idealVel = computeIdealVel();
        if (flying) {
            velocity.velocity = idealVel;
        } else {
            velocity.velocity = idealVel.setZ(velocity.velocity.z);
        }
        if (!gui.freezeMouse()) {
            // Look around
            camera3d.horAngle -= Input.mouseDelta().x / 300;
            camera3d.vertAngle += Input.mouseDelta().y / 300;
            camera3d.vertAngle = MathUtils.clamp(camera3d.vertAngle, -1.55, 1.55);

            // Weapon attack
            if (Input.mouseDown(0) && !Input.mouseDown(1)) {
                abilityController.tryAbilityType(new KnightPrepareAttack(this));
            } else if (abilityController.currentAbility() instanceof KnightPrepareAttack) {
                abilityController.finishAbility();
            }
            // Parry
            if (Input.mouseDown(1)) {
                abilityController.tryAbilityType(new KnightBlock(this));
            } else if (abilityController.currentAbility() instanceof KnightBlock
                    && abilityController.currentAbility().timer > .2) {
                abilityController.finishAbility();
            }
        }
        if (!gui.freezeMovement()) {
            // Fly
            if (Input.keyJustPressed(GLFW_KEY_LEFT_ALT)) {
                flying = !flying;
            }
            // Jump
            if (Input.keyDown(GLFW_KEY_SPACE)) {
                if (physics.onGround || flying) {
                    velocity.velocity = velocity.velocity.setZ(creature.jumpSpeed.get());
                }
            }
            // Crouch
            physics.shouldCrouch = Input.keyDown(GLFW_KEY_LEFT_CONTROL);
            // Set speeds
            creature.speed.setBaseValue(flying ? 200 : physics.crouch ? 4 : 8.);
            creature.jumpSpeed.setBaseValue(flying ? 200 : 12.);
        }
    }

    public Vec3d computeIdealVel() {
        Vec3d idealVel = new Vec3d(0, 0, 0);
        if (!gui.freezeMovement()) {
            Vec3d forwards = camera3d.facing();
            if (!flying) {
                forwards = forwards.setZ(0).normalize();
            }
            Vec3d sideways = camera3d.up.cross(forwards).normalize();
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
                idealVel = idealVel.setLength(creature.speed.get());
            }
        }
        return idealVel;
    }
}
