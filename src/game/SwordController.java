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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
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

    public static Model model;
    public static Vec3d modelTip;
    public static double ext1, ext2;
    public static double slashDuration;
    public static double weight;
    public static double slashiness;

    static {
        // Sword
        model = Model.load("sword.vox");
        modelTip = new Vec3d(16, 16, 32);
        ext1 = 2.5;
        ext2 = 5;
        slashDuration = .2;
        weight = .5;
        slashiness = 1;
        // Dagger
//        model = Model.load("dagger.vox");
//        modelTip = new Vec3d(4, 4, 16);
//        ext1 = 1.5;
//        ext2 = 3;
//        slashDuration = .15;
//        weight = .25;
//        slashiness = 1;
        // Fist
//        model = Model.load("fist.vox");
//        modelTip = new Vec3d(-8, 2, 4);
//        ext1 = 1;
//        ext2 = 2.5;
//        slashDuration = .15;
//        weight = 2;
//        slashiness = .2;
        // Hammer
//        model = Model.load("hammer.vox");
//        modelTip = new Vec3d(8, 8, 32);
//        ext1 = 1.5;
//        ext2 = 3;
//        slashDuration = .8;
//        weight = 2.5;
//        slashiness = 5;
        // Spear
//        model = Model.load("spear.vox");
//        modelTip = new Vec3d(4, 4, 64);
//        ext1 = 4.5;
//        ext2 = 8;
//        slashDuration = .2;
//        weight = 1;
//        slashiness = .1;
    }

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
        model.render(getRealSwordPos(), direction1, direction2, 1 / 16., modelTip, new Vec4d(1, 1, 1, 1));
    }

    @Override
    public void update(double dt) {
        slashTimer -= dt;
        Quaternion facing = Quaternion.fromEulerAngles(Camera.camera3d.horAngle, Camera.camera3d.vertAngle, 0);
        Vec3d realSwordPos = getRealSwordPos();

        if (Input.mouseJustReleased(0)) {
            if (slashTimer < -.05) {
                slashTimer = slashDuration;
                double angleBetween = Math.acos(facing.applyToForwards().dot(swordPos.applyToForwards()));
                swordVel = facing.applyToForwards().cross(swordPos.applyToForwards()).normalize().mul(angleBetween * 2 / slashDuration);
                Vec3d swordVel2 = facing.applyToForwards().cross(swordPos.applyToForwards()).normalize().mul(-angleBetween);
                slashGoalVel = swordVel.mul(-5);
//                swordVel = facing.div(swordPos).toAngleAxis().mul(-2 / slashDuration);
//                slashGoalVel = facing.div(swordPos).toAngleAxis().mul(10 / slashDuration);
                hit.clear();

                SplineAnimation sa = new SplineAnimation();
                sa.addKeyframe(0, swordPos.applyToForwards().mul(swordExtension), swordVel);
                sa.addKeyframe(slashDuration * .5, swordPos.applyToForwards().mul(ext2), slashGoalVel.cross(swordPos.applyToForwards()).mul(2));
                sa.addKeyframe(slashDuration * .75, facing.applyToForwards().mul(ext2), slashGoalVel.cross(facing.applyToForwards()).mul(2));
                Vec3d endPos = Quaternion.fromAngleAxis(swordVel2).mul(facing).applyToForwards();
                sa.addKeyframe(slashDuration, endPos.mul(ext2), slashGoalVel.cross(endPos).mul(2));
                sa.addKeyframe(slashDuration * 1.5, swordPos.applyToForwards().mul(swordExtension), new Vec3d(0, 0, 0));
                for (double time = 0; time < slashDuration; time += slashDuration / 100) {
                    System.out.println(time + " " + sa.getPosition(time));
                    Vec3d currentPos = Camera3d.camera3d.position.add(sa.getPosition(time));
                    double direction1 = MathUtils.direction1(sa.getPosition(time));
                    double direction2 = Math.PI / 2 + MathUtils.direction2(sa.getPosition(time));
                    double duration = 5.1;
                    createGraphicsEffect(duration, t -> {
                        model.render(currentPos, direction1, direction2, 1 / 16., modelTip, new Vec4d(2, 2, 2, .5 * (1 - t / duration)));
                    });
                }
            }
        }

        if (slashTimer <= 0) {
            Vec3d posChange = new Vec3d(1, 0, 0).add(swordPos.inverse().applyTo(position.position.sub(prevPos.prevPos).mul(-.25)));
            swordPos = swordPos.mul(Quaternion.fromEulerAngles(MathUtils.direction1(posChange), MathUtils.direction2(posChange), 0));
            if (Input.mouseDown(0)) {
                moveToGoal(facing, .5, dt / slashiness);
            } else {
                moveToGoal(facing, .05, dt / slashiness);
            }
            swordExtension = Math.pow(1e-8, dt * .2 / slashDuration) * swordExtension + (1 - Math.pow(1e-8, dt * .2 / slashDuration)) * ext1;
        } else {
            //swordPos = swordPos.mul(Quaternion.fromAngleAxis(swordVel.mul(dt)));
            swordPos = Quaternion.fromAngleAxis(swordVel.mul(dt)).mul(swordPos);
            swordVel = swordVel.lerp(slashGoalVel, 1 - Math.pow(.01, .2 * dt / slashDuration));
            swordExtension = Math.pow(1e-8, dt * .2 / slashDuration) * swordExtension + (1 - Math.pow(1e-8, dt * .2 / slashDuration)) * ext2;
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
                                c.damage(2, realSwordVel.mul(.02 * weight));
                                createGraphicsEffect(.2, t -> {
                                    Model m = Model.load("fireball.vox");
                                    m.render(pos, 0, 0, 1 / 16., m.size().div(2), new Vec4d(1, 1, 1, 1 - 5 * t));
                                });
                            }
                        }
                    }
                }
            }

//            Vec3d currentPos = getRealSwordPos();
//            double direction1 = swordPos.getYaw();
//            double direction2 = Math.PI / 2 + MathUtils.direction2(getRealSwordPos().sub(Camera.camera3d.position.sub(new Vec3d(0, 0, 1))));
//            double duration = 5.1;
//            createGraphicsEffect(duration, t -> {
//                model.render(currentPos, direction1, direction2, 1 / 16., modelTip, new Vec4d(2, 2, 2, .5 * (1 - t / duration)));
//            });
        }
        realSwordVel = getRealSwordPos().sub(realSwordPos).div(dt);
    }

    @Override
    public double updateLayer() {
        return 6;
    }

    public static class SplineAnimation {

        private final List<Double> keyframeTimes = new ArrayList();
        private final List<Vec3d> keyframePositions = new ArrayList();
        private final List<Vec3d> keyframeVelocities = new ArrayList();

        public void addKeyframe(double time, Vec3d position, Vec3d velocity) {
            keyframeTimes.add(time);
            keyframePositions.add(position);
            keyframeVelocities.add(velocity);
        }

        private static Vec3d cubicInterp(double t, Vec3d p1, Vec3d v1, Vec3d p2, Vec3d v2) {
            Vec3d a = v1.sub(p2.sub(p1));
            Vec3d b = v2.mul(-1).add(p2.sub(p1));
            return p1.lerp(p2, t).add(a.lerp(b, t).mul(t * (1 - t)));
        }

        private static Vec3d cubicInterpDerivative(double t, Vec3d p1, Vec3d v1, Vec3d p2, Vec3d v2) {
            Vec3d a = v1.sub(p2.sub(p1));
            Vec3d b = v2.mul(-1).add(p2.sub(p1));
            return p2.sub(p1).add(a.lerp(b, t).mul(1 - 2 * t)).add(b.sub(a).mul(t * (1 - t)));
//            return p1.lerp(p2, t).add(a.lerp(b, t).mul(t * (1 - t)));
        }

        public Vec3d getPosition(double time) {
            int i = 0;
            while (i < keyframeTimes.size() - 1 && keyframeTimes.get(i + 1) < time) {
                i++;
            }
            if (i == keyframeTimes.size() - 1) {
                return keyframePositions.get(i);
            }
            double dt = keyframeTimes.get(i + 1) - keyframeTimes.get(i);
            double t = (time - keyframeTimes.get(i)) / dt;
            return cubicInterp(t, keyframePositions.get(i), keyframeVelocities.get(i).mul(dt),
                    keyframePositions.get(i + 1), keyframeVelocities.get(i + 1).mul(dt));
        }

        public Vec3d getVelocity(double time) {
            int i = 0;
            while (i < keyframeTimes.size() - 1 && keyframeTimes.get(i + 1) < time) {
                i++;
            }
            if (i == keyframeTimes.size() - 1) {
                return keyframeVelocities.get(i);
            }
            double dt = keyframeTimes.get(i + 1) - keyframeTimes.get(i);
            double t = (time - keyframeTimes.get(i)) / dt;
            return cubicInterpDerivative(t, keyframePositions.get(i), keyframeVelocities.get(i).mul(dt),
                    keyframePositions.get(i + 1), keyframeVelocities.get(i + 1).mul(dt)).div(dt);
        }
    }
}
