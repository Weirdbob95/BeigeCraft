package game.spells;

import game.creatures.Creature;
import game.spells.TypeDefinitions.SpellEffectType;
import game.spells.TypeDefinitions.SpellElement;
import util.vectors.Vec3d;
import util.vectors.Vec4d;
import world.World;

public class SpellInfo {

    public final SpellTarget target;
    public final Vec3d direction;
    public final double powerMultiplier;
    public final SpellElement element;
    public final SpellEffectType effectType;
    public final World world;

    public SpellInfo(Creature caster, Vec3d goal, double powerMultiplier, SpellElement element, SpellEffectType effectType, World world) {
        this.target = new SpellTarget(caster);
        this.direction = goal.sub(caster.position.position);
        this.powerMultiplier = powerMultiplier;
        this.element = element;
        this.effectType = effectType;
        this.world = world;
    }

    public SpellInfo(SpellTarget target, Vec3d direction, double strengthMultiplier, SpellElement element, SpellEffectType effectType, World world) {
        this.target = target;
        this.direction = direction;
        this.powerMultiplier = strengthMultiplier;
        this.element = element;
        this.effectType = effectType;
        this.world = world;
    }

    public Vec4d color() {
        return new Vec4d(1, .2, 0, 1);
    }

//    public Vec4d colorTransparent(double alpha) {
//        return new Vec4d(1, 1, 1, alpha).mul(color());
//    }
    public SpellInfo multiplyPower(double mult) {
        return new SpellInfo(target, direction, powerMultiplier * mult, element, effectType, world);
    }

    public Vec3d position() {
        if (target.targetsCreature) {
            return target.creature.position.position;
        } else {
            return target.terrain;
        }
    }

    public SpellInfo setTarget(Creature creature) {
        return new SpellInfo(new SpellTarget(creature), direction, powerMultiplier, element, effectType, world);
    }

    public SpellInfo setTarget(Vec3d terrain) {
        return new SpellInfo(new SpellTarget(terrain), direction, powerMultiplier, element, effectType, world);
    }

    public static class SpellTarget {

        public final boolean targetsCreature;
        public final Creature creature;
        public final Vec3d terrain;

        public SpellTarget(Creature creature) {
            this.targetsCreature = true;
            this.creature = creature;
            this.terrain = null;
        }

        public SpellTarget(Vec3d terrain) {
            this.targetsCreature = false;
            this.creature = null;
            this.terrain = terrain;
        }
    }
}
