package world.regions;

import java.util.Random;
import util.math.Vec3d;
import util.noise.Noise;
import world.World;

public abstract class AbstractRegion {

    public final World world;
    public final RegionPos pos;
    public final Random random;
    public final Noise noise;
    private boolean isGenerated;

    public AbstractRegion(World world, RegionPos pos) {
        this.world = world;
        this.pos = pos;
        if (world != null && pos != null) {
            this.random = new Random(pos.hashCode() + getClass().hashCode() + Double.hashCode(world.seed));
            this.noise = new Noise(random);
        } else {
            this.random = null;
            this.noise = null;
        }
    }

    public Vec3d center() {
        return new Vec3d(pos.x + .5, pos.y + .5, 0).mul(size());
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

    public abstract int size();

    public Vec3d worldPos() {
        return new Vec3d(pos.x, pos.y, 0).mul(size());
    }

    public Vec3d worldPos(int x, int y, int z) {
        return worldPos().add(new Vec3d(x, y, z));
    }
}
