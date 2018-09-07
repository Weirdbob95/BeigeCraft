package game;

import behaviors.PhysicsBehavior;
import behaviors.PositionBehavior;
import behaviors.PreviousPositionBehavior;
import definitions.BlockType;
import engine.Behavior;
import engine.Input;
import static game.GraphicsEffect.createGraphicsEffect;
import game.creatures.CreatureBehavior;
import graphics.Model;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import opengl.Camera;
import opengl.Camera.Camera3d;
import util.MathUtils;
import util.vectors.Vec3d;
import util.vectors.Vec4d;

public class SwordController extends Behavior {

    public final PositionBehavior position = require(PositionBehavior.class);
    public final PreviousPositionBehavior prevPos = require(PreviousPositionBehavior.class);
    //public final VelocityBehavior velocity = require(VelocityBehavior.class);
    public final CreatureBehavior creature = require(CreatureBehavior.class);

    public Vec3d swordPos = new Vec3d(1, 0, 0);
    public double swordExtension = 2;
    public double slashTimer;
    public Vec3d slashGoal;

    public Vec3d realSwordPos;
    public Vec3d prevRealSwordPos;
    public Set<CreatureBehavior> hit = new HashSet();

    public Model model = Model.load("sword.vox");

    private void moveToGoal(Vec3d goal, double secs, double dt) {
        if (dt > secs) {
            dt = secs;
        }
        swordPos = swordPos.add(goal.sub(swordPos).mul(dt / secs)).normalize();
    }

    @Override
    public void render() {
        double direction1 = MathUtils.direction1(swordPos) * 1;
        double direction2 = -Math.PI / 2 + MathUtils.direction2(realSwordPos.sub(Camera.camera3d.position.sub(new Vec3d(0, 0, 1))));
        model.render(realSwordPos, direction1, direction2, 1 / 16., new Vec3d(16, 16, 32), new Vec4d(1, 1, 1, 1));
    }

    @Override
    public void update(double dt) {
        slashTimer -= dt;
        Vec3d facing = Camera.camera3d.facing();
        prevRealSwordPos = realSwordPos;
        realSwordPos = Camera3d.camera3d.position.add(swordPos.mul(swordExtension));
        // swordPos = swordPos.add(velocity.velocity.mul(dt * -.5)).normalize();
        swordPos = swordPos.add(position.position.sub(prevPos.prevPos).mul(-.25)).normalize();

        if (Input.mouseJustReleased(0)) {
            if (slashTimer < -.05) {
                slashTimer = .2;
                slashGoal = facing.add(facing.sub(swordPos).mul(3)).normalize();
                // swordPos = swordPos.add(swordPos.sub(slashGoal)).normalize();
                hit.clear();
            }
        }

        if (slashTimer <= 0) {
            if (Input.mouseDown(0)) {
                moveToGoal(facing, .5, dt);
            } else {
                moveToGoal(facing, .05, dt);
            }
            swordExtension = Math.pow(1e-8, dt) * swordExtension + (1 - Math.pow(1e-8, dt)) * 2.5;
        } else {
            moveToGoal(slashGoal, slashTimer, dt);
            swordExtension = Math.pow(1e-8, dt) * swordExtension + (1 - Math.pow(1e-8, dt)) * 5;
            for (double i = 0; i < 1; i += .1) {
                Vec3d pos = realSwordPos.lerp(position.position, i);
                if (get(PhysicsBehavior.class).world.getBlock(pos) == BlockType.getBlock("leaves")) {
                    get(PhysicsBehavior.class).world.setBlock(pos, null);
                }
                for (CreatureBehavior c : new LinkedList<>(CreatureBehavior.ALL)) {
                    if (c != creature) {
                        if (c.physics.containsPoint(pos)) {
                            if (!hit.contains(c)) {
                                hit.add(c);
                                c.damage(2, realSwordPos.sub(prevRealSwordPos).mul(.02 / dt));
                                createGraphicsEffect(.2, t -> {
                                    Model m = Model.load("fireball.vox");
                                    m.render(pos, 0, 0, 1 / 16., m.size().div(2), new Vec4d(1, 1, 1, 1 - 5 * t));
                                });
                            }
                        }
                    }
                }
            }

            Vec3d currentPos = realSwordPos;
            double direction1 = Camera.camera3d.horAngle * 0 + MathUtils.direction1(swordPos) * 1;
            double direction2 = -Math.PI / 2 + MathUtils.direction2(realSwordPos.sub(Camera.camera3d.position.sub(new Vec3d(0, 0, 1))));
            double duration = .1;
            createGraphicsEffect(duration, t -> {
                model.render(currentPos, direction1, direction2, 1 / 16., new Vec3d(16, 16, 32), new Vec4d(2, 2, 2, .05 * (1 - t / duration)));
            });
        }
    }

    @Override
    public double updateLayer() {
        return 6;
    }
}
