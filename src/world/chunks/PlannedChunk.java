package world.chunks;

import world.ChunkPos;
import world.World;
import static world.World.CHUNK_SIZE;
import world.biomes.BiomeData;

public class PlannedChunk extends AbstractChunk {

    public BiomeData bd;

    public PlannedChunk(World world, ChunkPos pos) {
        super(world, pos);
    }

    @Override
    protected void generate() {
        bd = BiomeData.generate(world, (pos.x + .5) * CHUNK_SIZE, (pos.y + .5) * CHUNK_SIZE);
        //isDesert = world.noise.perlin(pos.x * CHUNK_SIZE, pos.y * CHUNK_SIZE, .001) > .7;
    }
}
