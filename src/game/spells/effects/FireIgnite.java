package game.spells.effects;

import game.combat.Status;
import static game.combat.Status.StackMode.MAX_DURATION;
import game.creatures.CreatureBehavior;
import game.spells.SpellInfo;
import game.spells.SpellPart.SpellEffect;
import game.spells.TypeDefinitions.SpellElement;
import static game.spells.TypeDefinitions.SpellElement.FIRE;

/**
 *
 * @author Jake
 */
public class FireIgnite extends SpellEffect {
    
    private static final double TIME = 10.0;

    @Override
    public void cast(SpellInfo info) {
        if (info.target.targetsCreature()) {
            info.target.creature.damage(5 * info.powerMultiplier, info.direction);
            new Burning(info.target.creature, TIME, 5 * info.powerMultiplier).start();
        }
        if (info.target.targetsTerrain()) {
            //TODO
            //info.target.terrain
        }
        if (info.target.targetsItem()) {
            //TODO
            //info.target.item
        }
    }

    @Override
    public SpellElement element() {
        return FIRE;
    }
    
    public class Burning extends Status {
        
        private double power;
    
        public Burning(CreatureBehavior creature, double maxTimer, double heat) {
            super(creature, maxTimer);
            this.power = heat;
        }

        @Override
        protected void onStart() {
        }

        @Override
        protected void onUpdate(double dt) {
            creature.damage(power);
        }
        
        @Override
        protected void onFinish() {
            
        }

        @Override
        protected Status.StackMode stackMode() {
            return MAX_DURATION;
        }
    
    }
}
