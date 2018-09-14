package game.combat;

import behaviors.CollisionBehavior;
import behaviors.ModelBehavior;
import behaviors.PositionBehavior;
import engine.Behavior;
import game.creatures.CreatureBehavior;
import graphics.Model;
import util.math.Vec3d;

public class Strike extends Behavior {

    public final PositionBehavior position = require(PositionBehavior.class);
    public final CollisionBehavior collision = require(CollisionBehavior.class);
    public final ModelBehavior model = require(ModelBehavior.class);
    //public final PhysicsBehavior physics= require(PhysicsBehavior.class);
    public double power = 1;
    public int lifetime = 40;//in frames

    @Override
    public void createInner() {
        model.model = Model.load("slash.vox");
    }

    public Vec3d facing() {
        double x = Math.cos(model.rotation);
        double y = Math.sin(model.rotation);
        int z = 0;
        return new Vec3d(x, y, z);
    }

    @Override
    public void update(double dt) {
        CreatureBehavior other;
        if ((other = collision.test()) != null) {
            other.damage(power, facing());
            getRoot().destroy();
        }
        lifetime--;
        if (lifetime <= 0) {
            getRoot().destroy();
        }
    }
}
