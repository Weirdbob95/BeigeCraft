package game.spells.shapes;

import behaviors.ModelBehavior;
import engine.Behavior;
import game.spells.SpellInfo;
import graphics.Model;

/**
 * The S_Projectile class represents a Projectile initial shape, as described in
 * the spellcrafting design document.
 *
 * @author rsoiffer
 */
public class S_Projectile extends SpellShapeMissile {

    @Override
    public void cast(SpellInfo info) {
        spawnMissiles(info, S_ProjectileBehavior.class, 30);
    }

    /**
     * The S_ProjectileBehavior class represents the physical incarnation of a
     * spell projectile in the game world.
     */
    public static class S_ProjectileBehavior extends Behavior {

        public final MissileBehavior missile = require(MissileBehavior.class);
        public final ModelBehavior model = require(ModelBehavior.class);

        @Override
        public void createInner() {
            missile.lifetime.lifetime = 2;
            model.model = Model.load("fireball.vox");
        }
    }
}
