package game.spells.effects;

import game.spells.SpellInfo;
import game.spells.SpellPart.SpellEffect;
import game.spells.TypeDefinitions.SpellElement;
import static game.spells.TypeDefinitions.SpellElement.CORRUPTION;

/**
 *
 * @author Jake
 */
public class CorruptionDrain extends SpellEffect {

    @Override
    public void cast(SpellInfo info) {
        if (info.target.targetsCreature()) {
            double healthStolen = info.target.creature.damage(3 * info.powerMultiplier);
            info.caster.heal(healthStolen);
        }
        if (info.target.targetsTerrain()) {
            //TODO
            //info.target.terrain add health and destroy vegetation if vegetation
        }
        if (info.target.targetsItem()) {
            //TODO
            //info.target.item consumes food / item
        }
    }

    @Override
    public SpellElement element() {
        return CORRUPTION;
    }
}
