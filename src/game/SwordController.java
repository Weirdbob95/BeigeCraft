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
import util.Quaternion;
import util.vectors.Vec3d;
import util.vectors.Vec4d;

public class SwordController extends Behavior {

    public final PositionBehavior position = require(PositionBehavior.class);
    public final PreviousPositionBehavior prevPos = require(PreviousPositionBehavior.class);
    //public final VelocityBehavior velocity = require(VelocityBehavior.class);
    public final CreatureBehavior creature = require(CreatureBehavior.class);

    public Quaternion swordPos = Quaternion.IDENTITY;
    public Vec3d swordVel = new Vec3d(0, 0, 0);
    public double swordExtension = 2;
    public double slashTimer;
    public Vec3d slashGoalVel = new Vec3d(0, 0, 0);
    public Vec3d realSwordVel = new Vec3d(0, 0, 0);

    public Set<CreatureBehavior> hit = new HashSet();

    public Model model = Model.load("sword.vox");

    private Vec3d getRealSwordPos() {
        return Camera3d.camera3d.position.add(swordPos.applyToForwards().mul(swordExtension));
    }

    private void moveToGoal(Quaternion goal, double secs, double dt) {
        if (dt > secs) {
            dt = secs;
        }
        swordPos = swordPos.lerp(goal, dt / secs);
    }

    @Override
    public void render() {
        double direction1 = swordPos.getYaw();
        double direction2 = Math.PI / 2 + MathUtils.direction2(getRealSwordPos().sub(Camera.camera3d.position.sub(new Vec3d(0, 0, 1))));
        model.render(getRealSwordPos(), direction1, direction2, 1 / 16., new Vec3d(16, 16, 32), new Vec4d(1, 1, 1, 1));
    }

    @Override
    public void update(double dt) {
        slashTimer -= dt;
        Quaternion facing = Quaternion.fromEulerAngles(Camera.camera3d.horAngle, Camera.camera3d.vertAngle, 0);
        Vec3d realSwordPos = getRealSwordPos();
        Vec3d posChange = new Vec3d(1, 0, 0).add(swordPos.inverse().applyTo(position.position.sub(prevPos.prevPos).mul(-.25)));
        swordPos = swordPos.mul(Quaternion.fromEulerAngles(MathUtils.direction1(posChange), MathUtils.direction2(posChange), 0));

        if (Input.mouseJustReleased(0)) {
            if (slashTimer < -.05) {
                slashTimer = .2;
                swordVel = facing.div(swordPos).toAngleAxis().mul(-10);
                slashGoalVel = facing.div(swordPos).toAngleAxis().mul(50);
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
            swordPos = swordPos.mul(Quaternion.fromAngleAxis(swordVel.mul(dt)));
            swordVel = swordVel.lerp(slashGoalVel, 1 - Math.pow(.01, dt));
            swordExtension = Math.pow(1e-8, dt) * swordExtension + (1 - Math.pow(1e-8, dt)) * 5;
            for (double i = 0; i < 1; i += .1) {
                Vec3d pos = getRealSwordPos().lerp(position.position, i);
                if (get(PhysicsBehavior.class).world.getBlock(pos) == BlockType.getBlock("leaves")) {
                    get(PhysicsBehavior.class).world.setBlock(pos, null);
                }
                for (CreatureBehavior c : new LinkedList<>(CreatureBehavior.ALL)) {
                    if (c != creature) {
                        if (c.physics.containsPoint(pos)) {
                            if (!hit.contains(c)) {
                                hit.add(c);
                                c.damage(2, realSwordVel.mul(.02));
                                createGraphicsEffect(.2, t -> {
                                    Model m = Model.load("fireball.vox");
                                    m.render(pos, 0, 0, 1 / 16., m.size().div(2), new Vec4d(1, 1, 1, 1 - 5 * t));
                                });
                            }
                        }
                    }
                }
            }

            Vec3d currentPos = getRealSwordPos();
            double direction1 = swordPos.getYaw();
            double direction2 = Math.PI / 2 + MathUtils.direction2(getRealSwordPos().sub(Camera.camera3d.position.sub(new Vec3d(0, 0, 1))));
            double duration = .1;
            createGraphicsEffect(duration, t -> {
                model.render(currentPos, direction1, direction2, 1 / 16., new Vec3d(16, 16, 32), new Vec4d(2, 2, 2, .05 * (1 - t / duration)));
            });
        }
        realSwordVel = getRealSwordPos().sub(realSwordPos).div(dt);
    }

    @Override
    public double updateLayer() {
        return 6;
    }
}
