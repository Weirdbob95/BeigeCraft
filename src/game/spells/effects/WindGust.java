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

    @Override
    public void cast(SpellInfo info) {
        if (info.target.targetsCreature) {
            info.target.creature.damage(2 * info.powerMultiplier, info.direction.mul(1.5));
        }
    }

    @Override
    public SpellElement element() {
        return WIND;
    }
}
