package game.creatures;

import behaviors.ModelBehavior;
import behaviors.PhysicsBehavior;
import behaviors.PositionBehavior;
import engine.Behavior;
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

    public double prevHealth;
    public double redness = 0;

    @Override
    public void render() {
        if (creature.frzStatusTimer <= 0) {
            model.color = new Vec4d(1, creature.currentHealth / creature.maxHealth, creature.currentHealth / creature.maxHealth, 1)
                    .lerp(new Vec4d(1, 1 - redness, 1 - redness, 1), .75);
        }
    }

    public void setHitboxFromModel() {
        double width = Math.max(model.model.size().x, model.model.size().y);
        physics.hitboxSize1 = physics.hitboxSize2 = new Vec3d(width, width, model.model.size().z).div(32);
    }

    @Override
    public void update(double dt) {
        if (creature.frzStatusTimer > 0) {
            model.color = new Vec4d(0, 0.5, 1, 1);
            creature.velocity.velocity = creature.velocity.velocity.mul(Math.pow(.5, dt));
            creature.frzStatusTimer -= dt;
        } else {
            Vec3d idealVel = new Vec3d(0, 0, 0);
            goal = Camera.camera3d.position;
            if (goal != null) {
                Vec3d delta = goal.sub(position.position).setZ(0);
                if (delta.length() > minDist) {
                    idealVel = delta.setLength(creature.getSpeed());
                    if (physics.onGround && (physics.hitWall || Math.random() < dt * jumpChance)) {
                        creature.velocity.velocity = creature.velocity.velocity.setZ(creature.jumpSpeed);
                    }
                    model.rotation = Math.atan2(idealVel.y, idealVel.x);
                }
            }
            creature.velocity.velocity = creature.velocity.velocity.lerp(idealVel.setZ(creature.velocity.velocity.z), 1 - Math.pow(.005, dt));
        }
        redness *= Math.pow(.01, dt);
        if (creature.currentHealth < prevHealth) {
            redness += (prevHealth - creature.currentHealth) * .5;
        }
        prevHealth = creature.currentHealth;
    }
}
