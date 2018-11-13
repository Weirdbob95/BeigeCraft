package game.items;

import behaviors.ModelBehavior;
import behaviors.PositionBehavior;
import behaviors.PreviousPositionBehavior;
import static definitions.Loader.getItem;
import definitions.WeaponType;
import engine.Behavior;
import engine.Queryable.Property;
import static game.GraphicsEffect.createGraphicsEffect;
import game.creatures.CreatureBehavior;
import game.creatures.EyeBehavior;
import graphics.Model;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import org.joml.Matrix4d;
import static util.math.MathUtils.clamp;
import static util.math.MathUtils.lerp;
import static util.math.MathUtils.randomInSphere;
import util.math.Quaternion;
import util.math.SplineAnimation;
import util.math.Vec3d;
import util.math.Vec4d;

public class HeldItemController extends Behavior {

    public final PositionBehavior position = require(PositionBehavior.class);
    public final PreviousPositionBehavior prevPos = require(PreviousPositionBehavior.class);
    public final CreatureBehavior creature = require(CreatureBehavior.class);
    public final EyeBehavior eye = require(EyeBehavior.class);

    public Vec3d heldItemPos = randomInSphere();
    public Vec3d heldItemVel = new Vec3d(0, 0, 0);
    public Vec3d realHeldItemVel = new Vec3d(0, 0, 0);

    public WeaponType heldItemType = getItem("greataxe").weapon;
    public boolean makeTrail;
    public double reorientSpeed = 1;

    public Property<Supplier<Double>> ext1 = new Property<>(() -> heldItemType.ext1);
    public Vec3d shoulderPos = new Vec3d(0, .5, 14. / 16 - 1.4);
    public double armWidth = 2. / 16;
    public boolean rotateShouldersWithFacing = false;

    public SplineAnimation currentAnim;
    public double animTime;

    public void clearAnim() {
        currentAnim = null;
    }

    public void drawArm(Vec3d shoulder, Vec3d hand, double segmentLength, Vec4d color) {
        Vec3d delta = hand.sub(shoulder);
        double totalDist = delta.length();
        double elbowLower = 1. / 16;
        if (totalDist < segmentLength * 2) {
            double realDist = Math.sqrt(segmentLength * segmentLength - totalDist * totalDist / 4);
            elbowLower = lerp(realDist, Math.max(realDist, elbowLower), totalDist / (2 * segmentLength));
        }
        Vec3d viewUp = new Vec3d(0, 0, -1).cross(eye.facing).cross(eye.facing);
        Vec3d elbow = shoulder.lerp(hand, .5).add(viewUp.cross(delta).cross(delta).setLength(elbowLower));
        double segmentArcLength = Math.sqrt(totalDist * totalDist / 4 + elbowLower * elbowLower);
        double offset = (1 - segmentLength / segmentArcLength) / 4;

        Model.load("singlevoxel.vox").render(new Matrix4d()
                .translate(shoulder.lerp(elbow, .5 + offset).toJOML())
                .rotate(Quaternion.fromXYAxes(elbow.sub(shoulder), new Vec3d(0, 0, 1)).toJOML())
                .scale(segmentLength, armWidth, armWidth),
                new Vec3d(.5, .5, .5), color);
        Model.load("singlevoxel.vox").render(new Matrix4d()
                .translate(elbow.lerp(hand, .5 - offset).toJOML())
                .rotate(Quaternion.fromXYAxes(hand.sub(elbow), new Vec3d(0, 0, 1)).toJOML())
                .scale(segmentLength, armWidth, armWidth),
                new Vec3d(.5, .5, .5), color);
    }

    public SplineAnimation newAnim() {
        currentAnim = new SplineAnimation();
        currentAnim.addKeyframe(0, heldItemPos, heldItemVel);
        animTime = 0;
        return currentAnim;
    }

    @Override
    public void render() {
        renderTask().accept(new Vec4d(1, 1, 1, 1));

        ModelBehavior model = getOrNull(ModelBehavior.class);
        double modelRotation = eye.direction1();
        Vec4d modelColor = new Vec4d(.8, .6, .3, 1);
        if (model != null) {
            modelRotation = model.rotation;
            modelColor = model.color;
        }

        Vec3d currentPos = eye.eyePos.get().add(heldItemPos);
        Vec3d handleDir = heldItemPos.sub(eye.quat().applyTo(heldItemType.getHandlePos()));

        Quaternion shoulderQuat = rotateShouldersWithFacing ? eye.quatScaled(1, .75) : Quaternion.fromEulerAngles(modelRotation, 0, 0);
        Function<Vec3d, Vec3d> shoulderTransform = v -> shoulderQuat.applyTo(v).add(eye.eyePos.get());

        if (heldItemType.hand1 != Double.NaN) {
            Vec3d shoulder1 = shoulderTransform.apply(shoulderPos.mul(new Vec3d(1, -1, 1)));
            Vec3d hand1 = handleDir.setLength(-heldItemType.hand1).add(currentPos);
            drawArm(shoulder1, hand1, 12. / 16, new Vec4d(.9, .9, .9, 1).mul(modelColor));
        }

        if (heldItemType.hand2 != Double.NaN) {
            Vec3d shoulder2 = shoulderTransform.apply(shoulderPos);
            Vec3d hand2 = handleDir.setLength(-heldItemType.hand2).add(currentPos);
            drawArm(shoulder2, hand2, 12. / 16, new Vec4d(.9, .9, .9, 1).mul(modelColor));
        }
    }

    private Consumer<Vec4d> renderTask() {
        Vec3d currentPos = eye.eyePos.get().add(heldItemPos);
        Vec3d handleDir = heldItemPos.sub(eye.quat().applyTo(heldItemType.getHandlePos()));
        Quaternion quatVel = Quaternion.fromXYAxes(handleDir, heldItemVel);
        Quaternion quatUp = Quaternion.fromXYAxes(handleDir, new Vec3d(0, 0, -1));
        double lerpVal = clamp((heldItemVel.length() - .001) * .1, 0, 1);
        Quaternion quat = lerpVal == 0 ? quatUp : quatUp.lerp(quatVel, lerpVal);
        return c -> {
            heldItemType.getModel().render(currentPos, quat, 1 / 16., heldItemType.getModelTip(), c);
        };
    }

    @Override
    public void update(double dt) {
        Vec3d prevSwordPos = heldItemPos;

        if (currentAnim == null) {
            heldItemPos = heldItemPos.add(position.position.sub(prevPos.prevPos).mul(-1 / Math.max(heldItemType.slashiness, 1)));
            //heldItemPos = heldItemPos.add(position.position.sub(prevPos.prevPos).mul(-1));

            SplineAnimation anim = new SplineAnimation();
            anim.addKeyframe(0, heldItemPos, heldItemVel);
            Vec3d goal = heldItemPos.normalize().lerp(eye.quatScaled(1, .5).applyToForwards(), reorientSpeed).mul(ext1.get().get())
                    .add(eye.quat().applyTo(heldItemType.getRestingPos()));
//                    .add(rotate(restingPos, direction1(eye.facing)));
            anim.addKeyframe(1, goal, new Vec3d(0, 0, 0));

            double t = 1 - Math.exp(-dt * 20 / heldItemType.slashiness);
            heldItemPos = anim.getPosition(t);
            heldItemVel = anim.getVelocity(t);
        } else {
            animTime += dt;
            heldItemPos = currentAnim.getPosition(animTime);
            heldItemVel = currentAnim.getVelocity(animTime);
        }

        if (makeTrail) {
            Consumer<Vec4d> renderTask = renderTask();
            double duration = .1;
            createGraphicsEffect(duration, t -> {
                renderTask.accept(new Vec4d(2, 2, 2, .25 * (1 - t / duration)));
            });
        }
        realHeldItemVel = heldItemPos.sub(prevSwordPos).div(dt);
    }

    @Override
    public double updateLayer() {
        return 6;
    }
}
