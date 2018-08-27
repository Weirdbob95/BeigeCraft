package game.spells.shapes;

import game.spells.SpellInfo;
import game.spells.SpellPart.SpellShapeInitial;

/**
 * The S_Self class represents a Self initial shape, as described in the
 * spellcrafting design document.
 *
 * @author rsoiffer
 */
public class S_Self extends SpellShapeInitial {

    @Override
    public void cast(SpellInfo info) {
        hit(info);
    }
}
