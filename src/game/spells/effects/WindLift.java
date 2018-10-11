package game.spells.effects;

import game.combat.Status;
import static game.combat.Status.StackMode.MAX_DURATION;
import game.creatures.CreatureBehavior;
import game.spells.SpellInfo;
import game.spells.SpellPart.SpellEffect;
import game.spells.TypeDefinitions.SpellElement;
import static game.spells.TypeDefinitions.SpellElement.WIND;

/**
 *
 * @author Jake
 */
public class WindLift extends SpellEffect {

    @Override
    public void cast(SpellInfo info) {
        if (info.target.targetsCreature()) {
            new LiftStatus(info.target.creature, 3).start();
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
        return WIND;
    }

    public static class LiftStatus extends Status {

        public LiftStatus(CreatureBehavior creature, double maxTimer) {
            super(creature, maxTimer);
        }

        @Override
        protected void onStart() {
        }

        @Override
        protected void onUpdate(double dt) {
            if (creature.velocity.velocity.z < 8) {
                creature.velocity.velocity = creature.velocity.velocity.setZ(Math.min(creature.velocity.velocity.z + 100 * dt, 8));
            }
        }

        @Override
        protected StackMode stackMode() {
            return MAX_DURATION;
        }
    }
}
