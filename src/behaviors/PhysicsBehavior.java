package behaviors;

import engine.Behavior;
import static util.MathUtils.ceil;
import static util.MathUtils.floor;
import static util.MathUtils.mod;
import util.vectors.Vec3d;
import world.ConstructedChunk;
import world.World;
import static world.World.CHUNK_SIZE;

public class PhysicsBehavior extends Behavior {

    private static final int DETAIL = 5;

    public final PositionBehavior position = require(PositionBehavior.class);
    public final PreviousPositionBehavior prevPos = require(PreviousPositionBehavior.class);
    public final VelocityBehavior velocity = require(VelocityBehavior.class);

    public Vec3d hitboxSize = new Vec3d(0, 0, 0);
    public boolean onGround;
    public World world;

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
        onGround = false;
        Vec3d del = position.position.sub(prevPos.prevPos);
        if (wouldCollideAt(position.position)) {
            if (!wouldCollideAt(prevPos.prevPos)) {
                position.position = prevPos.prevPos;
                if (moveToWall(new Vec3d(0, 0, del.z))) {
                    velocity.velocity = velocity.velocity.setZ(0);
                    onGround = true;
                }

                // Step up walls
                double z = position.position.z;
                if (onGround) {
                    moveToWall(new Vec3d(0, 0, 1));
                }

                if (moveToWall(new Vec3d(del.x, 0, 0))) {
                    velocity.velocity = velocity.velocity.setX(0);
                }
                if (moveToWall(new Vec3d(0, del.y, 0))) {
                    velocity.velocity = velocity.velocity.setY(0);
                }

                if (position.position.z != z) {
                    moveToWall(new Vec3d(0, 0, z - position.position.z));
                }
            } else {
                velocity.velocity = new Vec3d(0, 0, 0);
            }
        }
    }

    @Override
    public double updateLayer() {
        return 5;
    }

    public boolean wouldCollideAt(Vec3d pos) {
        for (int x = floor(pos.x - hitboxSize.x); x < pos.x + hitboxSize.x; x++) {
            for (int y = floor(pos.y - hitboxSize.y); y < pos.y + hitboxSize.y; y++) {
                ConstructedChunk cc = world.constructedChunks.get(world.getChunkPos(new Vec3d(x, y, 0)));
                if (!cc.blockStorage.rangeEquals(mod(x, CHUNK_SIZE), mod(y, CHUNK_SIZE), floor(pos.z - hitboxSize.z), ceil(pos.z + hitboxSize.z), null)) {
                    return true;
                }
            }
        }
        return false;
    }
}
