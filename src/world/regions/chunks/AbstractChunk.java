package world.regions.chunks;

import world.World;
import static world.World.CHUNK_SIZE;
import world.regions.AbstractRegion;
import world.regions.RegionPos;

public abstract class AbstractChunk extends AbstractRegion {

    public AbstractChunk(World world, RegionPos pos) {
        super(world, pos);
    }

    @Override
    public int size() {
        return CHUNK_SIZE;
    }
}
