package game.abilities;

import behaviors.PhysicsBehavior;
import definitions.BlockType;
import static game.GraphicsEffect.createGraphicsEffect;
import game.HeldItemController;
import game.creatures.CreatureBehavior;
import graphics.Model;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import static util.MathUtils.clamp;
import util.Quaternion;
import util.SplineAnimation;
import util.vectors.Vec3d;
import util.vectors.Vec4d;
import world.World;

public class WeaponSwingAbility extends Ability {

    private static final double MAX_SLASH_ANGLE = 2.5;

    public HeldItemController heldItemController;
    public World world;

    public double slashDuration;
    public double timer;
    public Set<CreatureBehavior> hit = new HashSet();

    @Override
    public Ability attemptTransitionTo(Ability nextAbility) {
        return timer < 0 ? new Wait(slashDuration / 2) : this;
    }

    @Override
    public void onStartUse() {
        heldItemController = abilityController.get(HeldItemController.class);
        world = abilityController.get(PhysicsBehavior.class).world;

        Vec3d normSwordPos = heldItemController.heldItemPos.normalize();
        Vec3d facing = heldItemController.eye.facing;
        double slashAngle = Math.acos(normSwordPos.dot(facing));
        slashAngle = Math.pow(clamp(slashAngle / MAX_SLASH_ANGLE, 0, 1), Math.pow(heldItemController.heldItemType.slashiness, -.5)) * MAX_SLASH_ANGLE;
        Vec3d slashRotation = normSwordPos.cross(facing).normalize().mul(slashAngle);
        if (slashAngle < 1) {
            slashRotation = new Vec3d(0, 0, 0);
        }
        Vec3d startPos = Quaternion.fromAngleAxis(slashRotation).inverse().applyTo(facing);
        Vec3d endPos = Quaternion.fromAngleAxis(slashRotation).applyTo(facing);
        double slashTime = (slashAngle / 3 + .8) * heldItemController.heldItemType.slashDuration * .67;
        Vec3d slashGoalVel = slashRotation.mul(2 * heldItemController.heldItemType.ext2 / slashTime);

        SplineAnimation anim = heldItemController.newAnim();
        anim.addKeyframe(heldItemController.heldItemType.slashDuration * .33, startPos.mul(heldItemController.heldItemType.ext2), slashGoalVel.cross(startPos));
        anim.addKeyframe(heldItemController.heldItemType.slashDuration * .33 + slashTime / 2, facing.mul(heldItemController.heldItemType.ext2), slashGoalVel.cross(facing));
        anim.addKeyframe(heldItemController.heldItemType.slashDuration * .33 + slashTime, endPos.mul(heldItemController.heldItemType.ext2), slashGoalVel.cross(endPos));
        heldItemController.makeTrail = true;

        slashDuration = timer = heldItemController.heldItemType.slashDuration * .33 + slashTime;
        hit.clear();
    }

    @Override
    public void onContinuousUse(double dt) {
        timer -= dt;

        for (double i = 0; i < 1; i += .1) {
            Vec3d pos = heldItemController.eye.eyePos.get().add(heldItemController.heldItemPos).lerp(heldItemController.position.position, i);
            if (world.getBlock(pos) == BlockType.getBlock("leaves")) {
                world.setBlock(pos, null);
            }
            for (CreatureBehavior c : new LinkedList<>(CreatureBehavior.ALL)) {
                if (c != heldItemController.creature) {
                    if (c.physics.containsPoint(pos)) {
                        if (!hit.contains(c)) {
                            hit.add(c);
                            c.damage(2, heldItemController.realHeldItemVel.mul(.02 * heldItemController.heldItemType.weight));
                            createGraphicsEffect(.2, t -> {
                                Model m = Model.load("fireball.vox");
                                m.render(pos, 0, 0, 1 / 16., m.size().div(2), new Vec4d(1, 1, 1, 1 - 5 * t));
                            });
                        }
                    }
                }
            }
        }
    }

    @Override
    public void onEndUse() {
        heldItemController.clearAnim();
        heldItemController.makeTrail = false;
    }
}
