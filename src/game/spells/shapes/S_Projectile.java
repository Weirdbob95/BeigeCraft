package game.spells.shapes;

import behaviors.ModelBehavior;
import engine.Behavior;
import game.spells.SpellInfo;
import graphics.Model;
import util.vectors.Vec3d;

public class S_Projectile extends SpellShapeMissile {

    @Override
    public void cast(SpellInfo info, Vec3d goal) {
        spawnMissiles(info, goal, S_ProjectileBehavior.class, 30);
    }

    public static class S_ProjectileBehavior extends Behavior {

        public final MissileBehavior missile = require(MissileBehavior.class);
        public final ModelBehavior model = require(ModelBehavior.class);

        @Override
        public void createInner() {
            missile.lifetime.lifetime = 2;
            missile.homingRate = 5;
            model.model = Model.load("fireball.vox");
        }
    }
}
