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
            if (info.target.creature.currentHealth < info.target.creature.maxHealth){
                info.target.creature.damage(-3 * info.powerMultiplier, info.direction.mul(0));
                if (info.target.creature.currentHealth > info.target.creature.maxHealth) {
                    info.target.creature.currentHealth = info.target.creature.maxHealth; 
                }
            }
        }
    }

    @Override
    public SpellElement element() {
        return LIFE;
    }
}
