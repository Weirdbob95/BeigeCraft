package game.spells;

import game.creatures.Creature;
import game.spells.TypeDefinitions.SpellEffectType;
import static game.spells.TypeDefinitions.SpellEffectType.*;
import game.spells.TypeDefinitions.SpellElement;
import static game.spells.TypeDefinitions.SpellElement.*;
import util.vectors.Vec3d;

public abstract class EffectDefinitions {

    public static void hitCreature(SpellElement element, SpellEffectType effectType, Creature creature, double powerMultiplier, Vec3d direction) {
        switch (element) {
            case FIRE:
                switch (effectType) {
                    case IGNITE:
                        creature.damage(5 * powerMultiplier, direction);
                        return;
                }
        }
        throw new RuntimeException("Unknown element/effectType combination");
    }

    public static void hitTerrain(SpellElement element, SpellEffectType effectType, Vec3d terrain, double powerMultiplier, Vec3d direction) {
        throw new RuntimeException("Unknown element/effectType combination");
    }
}
