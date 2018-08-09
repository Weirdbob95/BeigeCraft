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
    public World world;
    public boolean stepUp;

    public boolean couldChangeHitboxSize(Vec3d newSize1, Vec3d newSize2) {
        Vec3d oldSize1 = hitboxSize1;
        Vec3d oldSize2 = hitboxSize2;
        hitboxSize1 = newSize1;
        hitboxSize2 = newSize2;
        boolean r = !wouldCollideAt(position.position);
        hitboxSize1 = oldSize1;
        hitboxSize2 = oldSize2;
        return r;
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

    @Override
    public void update(double dt) {
        boolean wasOnGround = onGround;
        onGround = false;
        hitWall = false;
        Vec3d del = position.position.sub(prevPos.prevPos);
        if (wouldCollideAt(position.position)) {
            if (stepUp && wasOnGround && !wouldCollideAt(position.position.add(new Vec3d(0, 0, 1.1)))) {
                position.position = position.position.add(new Vec3d(0, 0, 1.1));
                if (moveToWall(new Vec3d(0, 0, -1.1))) {
                    velocity.velocity = velocity.velocity.setZ(0);
                    onGround = true;
                }
            } else {
                if (!wouldCollideAt(prevPos.prevPos)) {
                    position.position = prevPos.prevPos;
                    if (moveToWall(new Vec3d(0, 0, del.z))) {
                        velocity.velocity = velocity.velocity.setZ(0);
                        if (del.z < 0) {
                            onGround = true;
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
                } else {
                    velocity.velocity = new Vec3d(0, 0, 0);
                }
            }
        }
        if (!onGround && wouldCollideAt(position.position.add(new Vec3d(0, 0, -.01)))) {
            onGround = true;
        }
    }

    @Override
    public double updateLayer() {
        return 5;
    }

    public boolean wouldCollideAt(Vec3d pos) {
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
