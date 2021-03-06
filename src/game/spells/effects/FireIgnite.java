package game.spells.effects;

import game.spells.SpellInfo;
import game.spells.SpellPart.SpellEffect;
import game.spells.TypeDefinitions.SpellElement;
import static game.spells.TypeDefinitions.SpellElement.FIRE;

/**
 *
 * @author Jake
 */
public class FireIgnite extends SpellEffect {

    @Override
    public void cast(SpellInfo info) {
        if (info.target.targetsCreature) {
            info.target.creature.damage(5 * info.powerMultiplier, info.direction);
        }
    }

    @Override
    public SpellElement element() {
        return FIRE;
    }
}
