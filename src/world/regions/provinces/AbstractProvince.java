package world.regions.provinces;

import world.World;
import static world.World.CHUNK_SIZE;
import static world.World.PROVINCE_SIZE;
import world.regions.RegionPos;
import world.regions.chunks.AbstractChunk;

public abstract class AbstractProvince extends AbstractChunk {

    public AbstractProvince(World world, RegionPos pos) {
        super(world, pos);
    }

    @Override
    public int size() {
        return CHUNK_SIZE * PROVINCE_SIZE;
    }
}
