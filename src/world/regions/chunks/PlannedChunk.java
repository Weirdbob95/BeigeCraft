package world.regions.chunks;

import world.regions.RegionPos;
import world.World;
import static world.World.CHUNK_SIZE;
import world.biomes.BiomeData;

public class PlannedChunk extends AbstractChunk {

    public BiomeData bd;

    public PlannedChunk(World world, RegionPos pos) {
        super(world, pos);
    }

    @Override
    protected void generate() {
        bd = BiomeData.generate(world, (pos.x + .5) * CHUNK_SIZE, (pos.y + .5) * CHUNK_SIZE);
    }
}
