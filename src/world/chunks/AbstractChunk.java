package world.chunks;

import java.util.Random;
import util.noise.Noise;
import util.vectors.Vec3d;
import world.ChunkPos;
import world.World;
import static world.World.CHUNK_SIZE;

public abstract class AbstractChunk {

    protected final World world;
    protected final ChunkPos pos;
    protected final Random random;
    protected final Noise noise;
    private boolean isGenerated;

    public AbstractChunk(World world, ChunkPos pos) {
        this.world = world;
        this.pos = pos;
        this.random = new Random(pos.hashCode() + getClass().hashCode() + Double.hashCode(world.seed));
        this.noise = new Noise(random);
    }

    protected Vec3d center() {
        return new Vec3d(pos.x + .5, pos.y + .5, 0).mul(CHUNK_SIZE);
    }

    public void cleanup() {
    }

    protected abstract void generate();

    public void generateOuter() {
        //isGenerated = false;
        generate();
        isGenerated = true;
    }

    public boolean isGenerated() {
        return isGenerated;
    }

    protected Vec3d worldPos() {
        return new Vec3d(pos.x, pos.y, 0).mul(CHUNK_SIZE);
    }

    protected Vec3d worldPos(int x, int y, int z) {
        return worldPos().add(new Vec3d(x, y, z));
    }
}
