package game.spells.effects;

import definitions.BlockType;
import game.spells.SpellInfo;
import game.spells.SpellPart.SpellEffect;
import game.spells.TypeDefinitions.SpellElement;
import static game.spells.TypeDefinitions.SpellElement.CORRUPTION;

/**
 *
 * @author Jake
 */
public class CorruptionDrain extends SpellEffect {
    
    private static final double DAMAGE_MODIFIER = 3.0;

    @Override
    public void cast(SpellInfo info) {
        if (info.target.targetsCreature()) {
            double healthStolen = info.target.creature.damage(DAMAGE_MODIFIER * info.powerMultiplier);
            info.caster.heal(healthStolen);
            hit(info);
        }
        if (info.target.targetsTerrain()) {
            //TODO
            BlockType block = info.world.getBlock(info.target.terrain);
            //add health and destroy vegetation if vegetation
            hit(info);
        }
        if (info.target.targetsItem()) {
            //TODO
            //info.target.item consumes food / item
            hit(info);
        }
    }

    @Override
    public SpellElement element() {
        return CORRUPTION;
    }
}
