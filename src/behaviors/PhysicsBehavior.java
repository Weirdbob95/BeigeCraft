package behaviors;

import engine.Behavior;
import static util.MathUtils.ceil;
import static util.MathUtils.floor;
import static util.MathUtils.mod;
import util.vectors.Vec3d;
import world.World;
import static world.World.CHUNK_SIZE;
import world.chunks.ConstructedChunk;

public class PhysicsBehavior extends Behavior {

    private static final int DETAIL = 10;

    public final PositionBehavior position = require(PositionBehavior.class);
    public final PreviousPositionBehavior prevPos = require(PreviousPositionBehavior.class);
    public final VelocityBehavior velocity = require(VelocityBehavior.class);

    public Vec3d hitboxSize1 = new Vec3d(0, 0, 0);
    public Vec3d hitboxSize2 = new Vec3d(0, 0, 0);
    public boolean onGround;
    public boolean hitWall;
    public double stepUp = 1.05;
    public World world;

    public Vec3d hitboxSize1Crouch = new Vec3d(0, 0, 0);
    public Vec3d hitboxSize2Crouch = new Vec3d(0, 0, 0);
    public boolean canCrouch;
    public boolean crouch;
    public boolean shouldCrouch;

    public boolean containsPoint(Vec3d v) {
        return v.x >= position.position.x - hitboxSize1.x
                && v.y >= position.position.y - hitboxSize1.y
                && v.z >= position.position.z - hitboxSize1.z
                && v.x <= position.position.x + hitboxSize2.x
                && v.y <= position.position.y + hitboxSize2.y
                && v.z <= position.position.z + hitboxSize2.z;
    }

    private boolean moveToWall(Vec3d del) {
        if (!wouldCollideAt(position.position.add(del))) {
            position.position = position.position.add(del);
            return false;
        }
        double best = 0;
        double check = .5;
        double step = .25;
        for (int i = 0; i < DETAIL; i++) {
            if (wouldCollideAt(del.mul(check).add(position.position))) {
                check -= step;
            } else {
                best = check;
                check += step;
            }
            step /= 2;
        }
        position.position = position.position.add(del.mul(best));
        return true;
    }

    private double potentialMoveDist(Vec3d del) {
        Vec3d oldPos = position.position;
        moveToWall(new Vec3d(del.x, 0, 0));
        moveToWall(new Vec3d(0, del.y, 0));
        moveToWall(new Vec3d(0, 0, del.z));
        double dist = position.position.sub(oldPos).length();
        position.position = oldPos;
        return dist;
    }

    @Override
    public void update(double dt) {
        // Useful vars
        boolean wasOnGround = onGround;
        Vec3d del = position.position.sub(prevPos.prevPos);

        // Reset all vars
        onGround = false;
        hitWall = false;
        crouch = canCrouch;

        // Check collision
        if (wouldCollideAt(position.position)) {
            if (wouldCollideAt(prevPos.prevPos)) {
                // Give up
                velocity.velocity = new Vec3d(0, 0, 0);
            } else {
                position.position = prevPos.prevPos;

                // Move in Z dir
                if (moveToWall(new Vec3d(0, 0, del.z))) {
                    velocity.velocity = velocity.velocity.setZ(0);
                    if (del.z < 0) {
                        onGround = true;
                    }
                }
                // Try step up
                boolean steppingUp = false;
                if (wasOnGround || onGround) {
                    double moveDist1 = potentialMoveDist(del.setZ(0));
                    position.position = position.position.add(new Vec3d(0, 0, stepUp));
                    double moveDist2 = potentialMoveDist(del.setZ(0));
                    if (moveDist1 >= moveDist2) {
                        position.position = position.position.sub(new Vec3d(0, 0, stepUp));
                    } else {
                        steppingUp = true;
                    }
                }
                if (moveToWall(new Vec3d(del.x, 0, 0))) {
                    velocity.velocity = velocity.velocity.setX(0);
                    hitWall = true;
                }
                if (moveToWall(new Vec3d(0, del.y, 0))) {
                    velocity.velocity = velocity.velocity.setY(0);
                    hitWall = true;
                }
                if (steppingUp) {
                    moveToWall(new Vec3d(0, 0, -stepUp));
                }
            }
        }

        // Try to stand up
        if (canCrouch && !shouldCrouch) {
            crouch = false;
            if (wouldCollideAt(position.position)) {
                crouch = true;
            }
        }

        // Set onGround
        if (!onGround && wouldCollideAt(position.position.add(new Vec3d(0, 0, -.01)))) {
            onGround = true;
        }
    }

    @Override
    public double updateLayer() {
        return 5;
    }

    public boolean wouldCollideAt(Vec3d pos) {
        Vec3d hitboxSize1 = crouch ? hitboxSize1Crouch : this.hitboxSize1;
        Vec3d hitboxSize2 = crouch ? hitboxSize2Crouch : this.hitboxSize2;
        for (int x = floor(pos.x - hitboxSize1.x); x < pos.x + hitboxSize2.x; x++) {
            for (int y = floor(pos.y - hitboxSize1.y); y < pos.y + hitboxSize2.y; y++) {
                ConstructedChunk cc = world.constructedChunks.get(world.getChunkPos(new Vec3d(x, y, 0)));
                if (!cc.blockStorage.rangeEquals(mod(x, CHUNK_SIZE), mod(y, CHUNK_SIZE), floor(pos.z - hitboxSize1.z), ceil(pos.z + hitboxSize2.z) - 1, null)) {
                    return true;
                }
            }
        }
        return false;
    }
}
