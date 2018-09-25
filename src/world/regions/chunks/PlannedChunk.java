package world.regions.chunks;

import world.World;
import static world.World.CHUNK_SIZE;
import world.biomes.BiomeData;
import world.regions.RegionPos;

public class PlannedChunk extends AbstractChunk {

    public BiomeData bd;

    public PlannedChunk(World world, RegionPos pos) {
        super(world, pos);
    }

    @Override
    protected void generate() {
        bd = BiomeData.generate(world, (pos.x + .5) * CHUNK_SIZE, (pos.y + .5) * CHUNK_SIZE);
    }

    @Override
    public int unloadDist() {
        return 500;
    }
}
