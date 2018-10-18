package game.spells.effects;

import game.spells.SpellInfo;
import game.spells.SpellPart.SpellEffect;
import game.spells.TypeDefinitions.SpellElement;
import static game.spells.TypeDefinitions.SpellElement.WIND;

/**
 *
 * @author Jake
 */
public class WindGust extends SpellEffect {

    private static final double DAMAGE_MODIFIER = 1.0;
    
    @Override
    public void cast(SpellInfo info) {
        if (info.target.targetsCreature()) {
            info.target.creature.damage(DAMAGE_MODIFIER * info.powerMultiplier, info.direction.mul(3));
            hit(info);
        }
        if (info.target.targetsTerrain()) {
            //TODO
            //info.target.terrain
            hit(info);
        }
        if (info.target.targetsItem()) {
            //TODO
            //info.target.item
            hit(info);
        }
    }

    @Override
    public SpellElement element() {
        return WIND;
    }
}
