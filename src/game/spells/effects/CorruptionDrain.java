package game.spells.effects;
import game.Player;
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
        if (info.target.targetsCreature) {
            info.target.creature.damage(3 * info.powerMultiplier, info.direction);
            //if (creature.currentHealth < creature.maxHealth)
        }
    }

    @Override
    public SpellElement element() {
        return CORRUPTION;
    }
}
