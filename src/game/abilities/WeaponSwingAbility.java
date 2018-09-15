package game.abilities;

import behaviors.PhysicsBehavior;
import engine.Behavior;
import game.combat.WeaponAttack;
import game.creatures.CreatureBehavior;
import game.items.HeldItemController;
import java.util.HashSet;
import java.util.Set;
import static util.math.MathUtils.clamp;
import util.math.Quaternion;
import util.math.SplineAnimation;
import util.math.Vec3d;
import world.World;

public class WeaponSwingAbility extends Ability {

    private static final double MAX_SLASH_ANGLE = 2.5;

    public final CreatureBehavior creature = user.get(CreatureBehavior.class);
    public final HeldItemController heldItemController = user.get(HeldItemController.class);
    public final World world = user.get(PhysicsBehavior.class).world;

    public final WeaponAttack weaponAttack;

    public double slashDuration;
    public double timer;
    public Set<CreatureBehavior> hit = new HashSet();

    public WeaponSwingAbility(Behavior user, WeaponAttack weaponAttack) {
        super(user);
        this.weaponAttack = weaponAttack;
    }

    @Override
    public Ability attemptTransitionTo(Ability nextAbility) {
        if (!weaponAttack.haveParriedThis.isEmpty()) {
            return new Wait(user, .5 + slashDuration / 2);
        }
//        return timer < 0 ? nextAbility : this;
        return timer < 0 ? new Wait(user, slashDuration / 2) : this;
    }

    @Override
    public void onStartUse() {
        Vec3d normSwordPos = heldItemController.heldItemPos.normalize();
        Vec3d facing = heldItemController.eye.facing;
        double slashAngle = Math.acos(clamp(normSwordPos.dot(facing), -1, 1));
        slashAngle = Math.pow(clamp(slashAngle / MAX_SLASH_ANGLE, 0, 1), Math.pow(heldItemController.heldItemType.slashiness * 2, -.5)) * MAX_SLASH_ANGLE;
        Vec3d slashRotation = normSwordPos.cross(facing).setLength(slashAngle);
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

        creature.speedMultiplier = .8;
    }

    @Override
    public void onContinuousUse(double dt) {
        timer -= dt;
        for (double i = .2; i <= 1; i += .05) {
            Vec3d pos = heldItemController.eye.eyePos.get().add(heldItemController.heldItemPos).lerp(heldItemController.position.position, 1 - i);
            weaponAttack.knockback = heldItemController.realHeldItemVel.mul(.02 * heldItemController.heldItemType.weight);
            weaponAttack.hitAtPos(pos);
        }
    }

    @Override
    public void onEndUse() {
        heldItemController.clearAnim();
        heldItemController.makeTrail = false;

        creature.speedMultiplier = 1;
        weaponAttack.hasFinished = true;
    }
}
