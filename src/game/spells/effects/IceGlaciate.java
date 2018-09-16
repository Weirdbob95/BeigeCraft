package game.spells.effects;

import game.spells.SpellInfo;
import game.spells.SpellPart.SpellEffect;
import game.spells.TypeDefinitions.SpellElement;
import static game.spells.TypeDefinitions.SpellElement.ICE;

/**
 *
 * @author Jake
 */
public class IceGlaciate extends SpellEffect {

    @Override
    public void cast(SpellInfo info) {
        if (info.target.targetsCreature) {
          info.target.creature.frzStatusTimer = 5;
            
        }
    }

    @Override
    public SpellElement element() {
        return ICE;
    }
}
