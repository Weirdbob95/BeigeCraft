package world.chunks;

import world.ChunkPos;
import world.World;
import world.biomes.BiomeData;

public class PlannedChunk extends AbstractChunk {

    public BiomeData bd;

    public PlannedChunk(World world, ChunkPos pos) {
        super(world, pos);
    }

    @Override
    protected void generate() {
        bd = BiomeData.generate(world, center());
        //isDesert = world.noise.perlin(pos.x * CHUNK_SIZE, pos.y * CHUNK_SIZE, .001) > .7;
    }
}
