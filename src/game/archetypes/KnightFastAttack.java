package game.archetypes;

import behaviors.PhysicsBehavior;
import static definitions.Loader.getBlock;
import engine.Behavior;
import engine.Queryable.Modifier;
import game.abilities.Ability;
import game.combat.WeaponAttack;
import game.creatures.CreatureBehavior;
import game.items.HeldItemController;
import util.math.Quaternion;
import util.math.SplineAnimation;
import util.math.Vec3d;
import world.World;

public class KnightFastAttack extends Ability.TimedAbility {

    public final CreatureBehavior creature = user.get(CreatureBehavior.class);
    public final HeldItemController heldItemController = user.get(HeldItemController.class);
    public final World world = user.get(PhysicsBehavior.class).world;

    public final WeaponAttack weaponAttack;
    public boolean isLeft = Math.random() < .5;
    private Modifier speedModifier;

    public KnightFastAttack(Behavior user) {
        super(user);
        weaponAttack = new WeaponAttack();
        weaponAttack.attacker = creature;
        weaponAttack.damage = 2;
        weaponAttack.blocksToBreak.add(getBlock("leaves"));
        weaponAttack.blocksToBreak.add(getBlock("leavesYellow"));
        weaponAttack.blocksToBreak.add(getBlock("leavesOrange"));
        weaponAttack.blocksToBreak.add(getBlock("leavesRed"));
        weaponAttack.blocksToBreak.add(getBlock("log"));
    }

    @Override
    public double duration() {
        return .7;
    }

    @Override
    public void finish(boolean interrupted) {
        heldItemController.clearAnim();
        heldItemController.makeTrail = false;
        speedModifier.remove();
        super.finish(interrupted);
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

    @Override
    public void start() {
        setAnim(heldItemController.newAnim());
        speedModifier = creature.speed.addModifier(x -> x * .8);
        super.start();
    }

    @Override
    public void update(double dt) {
        setAnim(heldItemController.currentAnim);
        heldItemController.makeTrail = timer > duration() * .3 && timer < duration() * .7;

        if (heldItemController.makeTrail) {
            for (double i = .2; i <= 1; i += .05) {
                Vec3d pos = heldItemController.eye.eyePos.get().add(heldItemController.heldItemPos).lerp(heldItemController.position.position, 1 - i);
                weaponAttack.knockback = heldItemController.realHeldItemVel.mul(.02 * heldItemController.heldItemType.weight);
                weaponAttack.hitAtPos(pos);
            }
        }
        super.update(dt);
    }
}
