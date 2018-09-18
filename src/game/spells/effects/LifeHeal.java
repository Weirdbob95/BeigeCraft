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
        if (info.target.targetsCreature) {
            if (info.target.creature.currentHealth.get() < info.target.creature.maxHealth.get()) {
                info.target.creature.currentHealth.setBaseValue(Math.min(info.target.creature.maxHealth.get(),
                        info.target.creature.currentHealth.getBaseValue() + 3 * info.powerMultiplier));
            }
        }
    }

    @Override
    public SpellElement element() {
        return LIFE;
    }
}
