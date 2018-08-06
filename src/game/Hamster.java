package game;

import behaviors.AccelerationBehavior;
import behaviors.PhysicsBehavior;
import engine.Behavior;
import util.vectors.Vec3d;

public class Hamster extends Behavior {

    public final ModelBehavior model = require(ModelBehavior.class);
    public final PhysicsBehavior physics = require(PhysicsBehavior.class);
    public final AccelerationBehavior acceleration = require(AccelerationBehavior.class);

    @Override
    public void createInner() {
        model.loadModel("hamster.vox");
        acceleration.acceleration = new Vec3d(0, 0, -10);
        physics.hitboxSize = new Vec3d(.3, .3, .3);
    }
}
