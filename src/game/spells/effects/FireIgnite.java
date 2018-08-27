
package game.spells.effects;
import game.spells.SpellInfo;
import game.spells.TypeDefinitions.SpellEffect;
/**
 *
 * @author Jake
 */
public class FireIgnite implements SpellEffect {

    @Override
    public void cast(SpellInfo info) {
        if(info.target.targetsCreature){
        info.target.creature.damage(5 * info.powerMultiplier, info.direction);
    }
    }
    
}
