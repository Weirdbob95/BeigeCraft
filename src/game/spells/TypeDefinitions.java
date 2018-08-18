package game.spells;

import util.vectors.Vec3d;

public abstract class TypeDefinitions {

    public static enum SpellCastingType {
        INSTANT,
        CHARGED,
        CHANNELED
    }

    public static enum SpellEffectType {
        DESTRUCTION,
        CONTROL,
        EMPOWERMENT
    }

    public static enum SpellElement {
        FIRE,
        ICE
    }

    public static SpellShapeInitial constructSpell(SpellElement element, SpellEffectType effectType, SpellShapeInitial shapeInitial, SpellShapeModifier... shapeModifiers) {
        if (shapeModifiers.length == 0) {
            shapeInitial.onHit(new SpellEffectFinal());
        } else {
            shapeInitial.onHit(shapeModifiers[0]);
            for (int i = 0; i < shapeModifiers.length - 1; i++) {
                shapeModifiers[i].onHit(shapeModifiers[i + 1]);
            }
            shapeModifiers[shapeModifiers.length - 1].onHit(new SpellEffectFinal());
        }
        return shapeInitial;
    }

    public static interface SpellEffect {

        public void cast(SpellInfo info);
    }

    public static class SpellEffectFinal implements SpellEffect {

        @Override
        public void cast(SpellInfo info) {
            if (info.target.targetsCreature) {
                EffectDefinitions.hitCreature(info.element, info.effectType, info.target.creature, info.powerMultiplier, info.direction);
            } else {
                EffectDefinitions.hitTerrain(info.element, info.effectType, info.target.terrain, info.powerMultiplier, info.direction);
            }
        }
    }

    public abstract static class SpellShape<T extends SpellShape> {

        private SpellEffect onHit;

        public void hit(SpellInfo info) {
            onHit.cast(info);
        }

        public T onHit(SpellEffect onHit) {
            this.onHit = onHit;
            return (T) this;
        }
    }

    public abstract static class SpellShapeInitial extends SpellShape<SpellShapeInitial> {

        public abstract void cast(SpellInfo info, Vec3d goal);
    }

    public abstract static class SpellShapeModifier extends SpellShape<SpellShapeModifier> implements SpellEffect {
    }
}
