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
import util.MathUtils;
import util.Quaternion;
import util.vectors.Vec3d;
import util.vectors.Vec4d;

public class SwordController extends Behavior {

    public final PositionBehavior position = require(PositionBehavior.class);
    public final PreviousPositionBehavior prevPos = require(PreviousPositionBehavior.class);
    public final CreatureBehavior creature = require(CreatureBehavior.class);

    public Vec3d swordPos = new Vec3d(0, 0, 0);
    public Vec3d swordVel = new Vec3d(0, 0, 0);
    public Vec3d realSwordVel = new Vec3d(0, 0, 0);
    public double slashTimer;
    public SplineAnimation currentAnim;

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

    @Override
    public void render() {
        double direction1 = MathUtils.direction1(swordPos);
        double direction2 = Math.PI / 2 + MathUtils.direction2(swordPos.add(new Vec3d(0, 0, 1)));
        model.render(Camera.camera3d.position.add(swordPos), direction1, direction2, 1 / 16., modelTip, new Vec4d(1, 1, 1, 1));
    }

    @Override
    public void update(double dt) {
        slashTimer -= dt;
        Vec3d facing = Camera.camera3d.facing();
        Vec3d prevSwordPos = swordPos;

        if (Input.mouseJustReleased(0)) {
            if (slashTimer < -.1) {
                Vec3d normSwordPos = swordPos.normalize();
                double slashAngle = Math.acos(normSwordPos.dot(facing));
                slashAngle = Math.pow(slashAngle / Math.PI, Math.pow(slashiness, -.5)) * 2.5;
                Vec3d slashRotation = normSwordPos.cross(facing).normalize().mul(slashAngle);
                Vec3d startPos = Quaternion.fromAngleAxis(slashRotation).inverse().applyTo(facing);
                Vec3d endPos = Quaternion.fromAngleAxis(slashRotation).applyTo(facing);
                Vec3d slashGoalVel = slashRotation.mul(2 * ext2 / (slashDuration * .5));

                currentAnim = new SplineAnimation();
                currentAnim.addKeyframe(0, swordPos, swordVel);
                currentAnim.addKeyframe(slashDuration * .5, startPos.mul(ext2), slashGoalVel.cross(startPos));
                currentAnim.addKeyframe(slashDuration * .75, facing.mul(ext2), slashGoalVel.cross(facing));
                currentAnim.addKeyframe(slashDuration, endPos.mul(ext2), slashGoalVel.cross(endPos));

                slashTimer = slashDuration;
                hit.clear();
            }
        }

        if (slashTimer <= 0) {
            swordPos = swordPos.add(position.position.sub(prevPos.prevPos).mul(-1 / Math.max(slashiness, 1)));
            currentAnim = new SplineAnimation();
            currentAnim.addKeyframe(0, swordPos, swordVel);
            currentAnim.addKeyframe(Input.mouseDown(0) ? (.5 * Math.min(slashiness, 1)) : (.05 * Math.max(slashiness, 1)), facing.mul(ext1), new Vec3d(0, 0, 0));
            swordPos = currentAnim.getPosition(dt);
            swordVel = currentAnim.getVelocity(dt);
        } else {
            swordPos = currentAnim.getPosition(slashDuration - slashTimer);
            swordVel = currentAnim.getVelocity(slashDuration - slashTimer);
            for (double i = 0; i < 1; i += .1) {
                Vec3d pos = Camera.camera3d.position.add(swordPos).lerp(position.position, i);
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

            Vec3d currentPos = Camera.camera3d.position.add(swordPos);
            double direction1 = MathUtils.direction1(swordPos);
            double direction2 = Math.PI / 2 + MathUtils.direction2(swordPos.add(new Vec3d(0, 0, 1)));
            double duration = .1;
            createGraphicsEffect(duration, t -> {
                model.render(currentPos, direction1, direction2, 1 / 16., modelTip, new Vec4d(2, 2, 2, .05 * .2 / slashDuration * (1 - t / duration)));
            });
        }
        realSwordVel = swordPos.sub(prevSwordPos).div(dt);
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
