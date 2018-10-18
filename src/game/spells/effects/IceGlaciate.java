package game.spells.effects;

import engine.Property.Modifier;
import game.combat.Status;
import static game.combat.Status.StackMode.MAX_DURATION;
import game.creatures.CreatureBehavior;
import game.creatures.MonsterBehavior;
import game.spells.SpellInfo;
import game.spells.SpellPart.SpellEffect;
import game.spells.TypeDefinitions.SpellElement;
import static game.spells.TypeDefinitions.SpellElement.ICE;
import util.math.Vec4d;

/**
 *
 * @author Jake
 */
public class IceGlaciate extends SpellEffect {

    @Override
    public void cast(SpellInfo info) {
        if (info.target.targetsCreature()) {
            new FreezeStatus(info.target.creature, 5).start();
            hit(info);
        }
        if (info.target.targetsTerrain()) {
            //TODO
            //info.target.terrain
            hit(info);
        }
        if (info.target.targetsItem()) {
            //TODO
            //info.target.item
            hit(info);
        }
    }

    @Override
    public SpellElement element() {
        return ICE;
    }

    public static class FreezeStatus extends Status {

        public FreezeStatus(CreatureBehavior creature, double timer) {
            super(creature, timer);
        }

        @Override
        public void onStart() {
            Modifier preventMovement = creature.canMove.addModifier(b -> false);
            addModifiers(preventMovement);

            MonsterBehavior monster = creature.getOrNull(MonsterBehavior.class);
            if (monster != null) {
                Modifier changeColor = monster.modelColor.addModifier(c -> c.lerp(new Vec4d(.1, .3, 1, 1), .9));
                addModifiers(changeColor);
            }
        }

        @Override
        public void onUpdate(double dt) {
            creature.velocity.velocity = creature.velocity.velocity.mul(Math.pow(.1, dt)).setZ(creature.velocity.velocity.z);
        }
        
        @Override
        protected void onFinish() {
            
        }

        @Override
        protected StackMode stackMode() {
            return MAX_DURATION;
        }
    }
}
