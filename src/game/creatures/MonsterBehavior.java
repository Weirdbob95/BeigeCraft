package game.creatures;

import behaviors.ModelBehavior;
import behaviors.PhysicsBehavior;
import behaviors.PositionBehavior;
import engine.Behavior;
import engine.Queryable.Property;
import opengl.Camera;
import util.math.Vec3d;
import util.math.Vec4d;

public class MonsterBehavior extends Behavior {

    public final PositionBehavior position = require(PositionBehavior.class);
    public final CreatureBehavior creature = require(CreatureBehavior.class);
    public final PhysicsBehavior physics = require(PhysicsBehavior.class);
    public final ModelBehavior model = require(ModelBehavior.class);

    public Vec3d goal;
    public double minDist = 4;
    public double jumpChance = 1;
    public Property<Vec4d> modelColor = new Property(new Vec4d(1, 1, 1, 1));

    public double prevHealth;
    public double redness = 0;

    @Override
    public void createInner() {
        modelColor.addModifier(-1, c -> new Vec4d(1, creature.currentHealth.get() / creature.maxHealth.get(),
                creature.currentHealth.get() / creature.maxHealth.get(), 1).lerp(new Vec4d(1, 1 - redness, 1 - redness, 1), .75));
    }

    @Override
    public void render() {
        model.color = modelColor.get();
    }

    public void setHitboxFromModel() {
        double width = Math.max(model.model.size().x, model.model.size().y);
        physics.hitboxSize1 = physics.hitboxSize2 = new Vec3d(width, width, model.model.size().z).div(32);
    }

    @Override
    public void update(double dt) {
        if (creature.canMove.get()) {
            Vec3d idealVel = new Vec3d(0, 0, 0);
            goal = Camera.camera3d.position;
            if (goal != null) {
                Vec3d delta = goal.sub(position.position).setZ(0);
                if (delta.length() > minDist) {
                    idealVel = delta.setLength(creature.speed.get());
                    if (physics.onGround && (physics.hitWall || Math.random() < dt * jumpChance)) {
                        creature.velocity.velocity = creature.velocity.velocity.setZ(creature.jumpSpeed.get());
                    }
                    model.rotation = Math.atan2(idealVel.y, idealVel.x);
                }
            }
            creature.velocity.velocity = creature.velocity.velocity.lerp(idealVel.setZ(creature.velocity.velocity.z), 1 - Math.pow(.005, dt));
        }
        redness *= Math.pow(.01, dt);
        if (creature.currentHealth.get() < prevHealth) {
            redness += (prevHealth - creature.currentHealth.get()) * .5;
        }
        prevHealth = creature.currentHealth.get();
    }
}
