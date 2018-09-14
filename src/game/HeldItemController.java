package game;

import behaviors.PositionBehavior;
import behaviors.PreviousPositionBehavior;
import engine.Behavior;
import static game.GraphicsEffect.createGraphicsEffect;
import game.creatures.CreatureBehavior;
import game.creatures.EyeBehavior;
import util.math.MathUtils;
import util.math.SplineAnimation;
import util.math.Vec3d;
import util.math.Vec4d;

public class HeldItemController extends Behavior {

    public final PositionBehavior position = require(PositionBehavior.class);
    public final PreviousPositionBehavior prevPos = require(PreviousPositionBehavior.class);
    public final CreatureBehavior creature = require(CreatureBehavior.class);
    public final EyeBehavior eye = require(EyeBehavior.class);

    public Vec3d heldItemPos = new Vec3d(0, 0, 0);
    public Vec3d heldItemVel = new Vec3d(0, 0, 0);
    public Vec3d realHeldItemVel = new Vec3d(0, 0, 0);

    public Weapon heldItemType = Weapon.SWORD;
    public boolean makeTrail;
    public double reorientSpeed = .5;
    public Vec4d color = new Vec4d(1, 1, 1, 1);

    private SplineAnimation currentAnim;
    private double animTime;

    public void clearAnim() {
        currentAnim = null;
    }

    public SplineAnimation newAnim() {
        currentAnim = new SplineAnimation();
        currentAnim.addKeyframe(0, heldItemPos, heldItemVel);
        animTime = 0;
        return currentAnim;
    }

    @Override
    public void render() {
        double direction1 = MathUtils.direction1(heldItemPos.add(eye.facing.cross(new Vec3d(0, 0, 1)).mul(-.5)));
        double direction2 = Math.PI / 2 + MathUtils.direction2(heldItemPos.add(new Vec3d(0, 0, 1)));
        heldItemType.model.render(eye.eyePos.get().add(heldItemPos), direction1, direction2, 1 / 16., heldItemType.modelTip, color);
    }

    @Override
    public void update(double dt) {
        Vec3d prevSwordPos = heldItemPos;

        if (currentAnim == null) {
            heldItemPos = heldItemPos.add(position.position.sub(prevPos.prevPos).mul(-1 / Math.max(heldItemType.slashiness, 1)));
            //heldItemPos = heldItemPos.add(position.position.sub(prevPos.prevPos).mul(-1));

            SplineAnimation anim = new SplineAnimation();
            anim.addKeyframe(0, heldItemPos, heldItemVel);
            anim.addKeyframe(1, heldItemPos.normalize().lerp(eye.facing, reorientSpeed).mul(heldItemType.ext1), new Vec3d(0, 0, 0));

            double t = 1 - Math.exp(-dt * 20 / heldItemType.slashiness);
            heldItemPos = anim.getPosition(t);
            heldItemVel = anim.getVelocity(t);
        } else {
            animTime += dt;
            heldItemPos = currentAnim.getPosition(animTime);
            heldItemVel = currentAnim.getVelocity(animTime);
        }

        if (makeTrail) {
            Vec3d currentPos = eye.eyePos.get().add(heldItemPos);
            double direction1 = MathUtils.direction1(heldItemPos.add(eye.facing.cross(new Vec3d(0, 0, 1)).mul(-.5)));
            double direction2 = Math.PI / 2 + MathUtils.direction2(heldItemPos.add(new Vec3d(0, 0, 1)));
            double duration = .1;
            createGraphicsEffect(duration, t -> {
                heldItemType.model.render(currentPos, direction1, direction2, 1 / 16., heldItemType.modelTip, new Vec4d(2, 2, 2, .05 * .2 / heldItemType.slashDuration * (1 - t / duration)));
            });
        }
        realHeldItemVel = heldItemPos.sub(prevSwordPos).div(dt);
    }

    @Override
    public double updateLayer() {
        return 6;
    }
}
