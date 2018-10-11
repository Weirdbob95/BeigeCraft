package game.spells.effects;

import game.spells.SpellInfo;
import game.spells.SpellPart.SpellEffect;
import game.spells.TypeDefinitions.SpellElement;
import static game.spells.TypeDefinitions.SpellElement.LIFE;

/**
 *
 * @author Jake
 */
public class LifeHeal extends SpellEffect {

    @Override
    public void cast(SpellInfo info) {
        if (info.target.targetsCreature()) {
            info.target.creature.heal(info.powerMultiplier);
        }
        if (info.target.targetsTerrain()) {
            //TODO implement terrain corruption
            //info.target.terrain.corruption = 0;
        }
        if (info.target.targetsItem()) {
            //TODO implement item/shard corruption
            //info.target.item.corruption = 0;
        }
    }

    @Override
    public SpellElement element() {
        return LIFE;
    }
}
