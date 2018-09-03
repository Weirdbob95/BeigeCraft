package game.spells.effects;

import game.spells.SpellInfo;
import game.spells.SpellPart.SpellEffect;
import game.spells.TypeDefinitions.SpellElement;
import static game.spells.TypeDefinitions.SpellElement.WIND;
import util.vectors.Vec3d;

/**
 *
 * @author Jake
 */
public class WindLift extends SpellEffect {

    @Override
    public void cast(SpellInfo info) {
        if (info.target.targetsCreature) {
                   info.target.creature.velocity.velocity = info.target.creature.velocity.velocity.add(new Vec3d(0,0,5));
        }
    }

    @Override
    public SpellElement element() {
        return WIND;
    }
}
