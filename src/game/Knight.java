package game;

import behaviors.PhysicsBehavior;
import behaviors.PositionBehavior;
import behaviors.VelocityBehavior;
import static definitions.Loader.getBlock;
import engine.Behavior;
import engine.Input;
import engine.Property.Modifier;
import game.abilities.Ability;
import game.abilities.Ability.TimedAbility;
import game.abilities.AbilityController;
import game.abilities.ParryAbility;
import game.abilities.Stun;
import game.combat.WeaponAttack;
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
import util.math.Quaternion;
import util.math.SplineAnimation;
import util.math.Vec3d;
import world.World;

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
//        heldItemController.heldItemType = getItem("greataxe").weapon;
        heldItemController.rotateShouldersWithFacing = true;
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
            if (Input.mouseDown(0)) {
                abilityController.attemptAbility(new FastAttack(this));
            } else {
                abilityController.attemptAbility(null);
            }
            // Parry
            if (Input.mouseJustPressed(1)) {
                abilityController.attemptAbility(new ParryAbility(this));
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

//        creature.currentHealth.setBaseValue(10.);
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

    public static class FastAttack extends TimedAbility {

        public final CreatureBehavior creature = user.get(CreatureBehavior.class);
        public final HeldItemController heldItemController = user.get(HeldItemController.class);
        public final World world = user.get(PhysicsBehavior.class).world;

        public final WeaponAttack weaponAttack;
        private Modifier speedModifier;
        private boolean isLeft = Math.random() < .5;

        public FastAttack(Behavior user) {
            super(user);
            weaponAttack = new WeaponAttack();
            weaponAttack.attacker = creature;
            weaponAttack.damage = 2;
            weaponAttack.blocksToBreak.add(getBlock("leaves"));
            weaponAttack.blocksToBreak.add(getBlock("log"));
        }

        @Override
        public Ability attemptTransitionTo(Ability nextAbility) {
            if (!weaponAttack.haveParriedThis.isEmpty()) {
                return new Stun(user, 1);
            }
            if (nextAbility instanceof FastAttack) {
                if (timer < 0) {
                    ((FastAttack) nextAbility).isLeft = !isLeft;
                    return nextAbility;
                } else {
                    return this;
                }
            }
            return super.attemptTransitionTo(nextAbility);
        }

        @Override
        public double duration() {
            return .7;
        }

        @Override
        public void onStartUse() {
            setAnim(heldItemController.newAnim());
            speedModifier = creature.speed.addModifier(x -> x * .8);
            super.onStartUse();
        }

        @Override
        public void onContinuousUse(double dt) {
            setAnim(heldItemController.currentAnim);
            heldItemController.makeTrail = timer > duration() * .3 && timer < duration() * .7;

            if (heldItemController.makeTrail) {
                for (double i = .2; i <= 1; i += .05) {
                    Vec3d pos = heldItemController.eye.eyePos.get().add(heldItemController.heldItemPos).lerp(heldItemController.position.position, 1 - i);
                    weaponAttack.knockback = heldItemController.realHeldItemVel.mul(.02 * heldItemController.heldItemType.weight);
                    weaponAttack.hitAtPos(pos);
                }
            }
            super.onContinuousUse(dt);
        }

        @Override
        public void onEndUse() {
            heldItemController.clearAnim();
            heldItemController.makeTrail = false;
            speedModifier.remove();
            weaponAttack.hasFinished = true;
        }

        public void setAnim(SplineAnimation anim) {
            Vec3d facing = heldItemController.eye.facing;
            Vec3d side = facing.cross(new Vec3d(0, 0, 1)).normalize();
            Vec3d up = side.cross(facing);
            Vec3d normSwordPos = facing.add(side.mul(isLeft ? -1 : 1)).add(up).normalize();

            double slashAngle = 1;
            Vec3d slashRotation = normSwordPos.cross(facing).setLength(slashAngle);
            Vec3d startPos = Quaternion.fromAngleAxis(slashRotation).inverse().applyTo(facing);
            Vec3d endPos = Quaternion.fromAngleAxis(slashRotation).applyTo(facing);
            double slashTime = duration() * .4;
            Vec3d slashGoalVel = slashRotation.mul(2 * heldItemController.heldItemType.ext2 / slashTime);

            anim.clearKeyframesAfter(0);
            anim.addKeyframe(duration() * .3, startPos.mul(heldItemController.heldItemType.ext2), slashGoalVel.cross(startPos));
            anim.addKeyframe(duration() * .5, facing.mul(heldItemController.heldItemType.ext2), slashGoalVel.cross(facing));
            anim.addKeyframe(duration() * .7, endPos.mul(heldItemController.heldItemType.ext2), slashGoalVel.cross(endPos));
            anim.addKeyframe(duration() * 1, endPos.mul(heldItemController.heldItemType.ext2), new Vec3d(0, 0, 0));
        }
    }
}
