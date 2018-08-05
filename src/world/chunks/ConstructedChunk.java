package world.chunks;

import util.RLEColumnStorage;
import world.BlockType;
import world.ChunkPos;
import world.World;
import static world.World.CHUNK_SIZE;
import world.biomes.Biome;

public class ConstructedChunk extends AbstractChunk {

    public final RLEColumnStorage<BlockType> blockStorage = new RLEColumnStorage(CHUNK_SIZE);

    public ConstructedChunk(World world, ChunkPos pos) {
        super(world, pos);
    }

    @Override
    protected void generate() {
        HeightmappedChunk hc = world.heightmappedChunks.get(pos);
        for (int x = 0; x < CHUNK_SIZE; x++) {
            for (int y = 0; y < CHUNK_SIZE; y++) {
                int elevation = hc.heightmap[x][y];
                if (hc.biomemap[x][y].plurality() == Biome.DESERT) {
                    blockStorage.setRangeInfinite(x, y, elevation, BlockType.SAND);
                } else {
                    blockStorage.setRangeInfinite(x, y, elevation, BlockType.GRASS);
                    blockStorage.setRangeInfinite(x, y, elevation - 1, BlockType.DIRT);
                }
                blockStorage.setRangeInfinite(x, y, elevation - 3, BlockType.STONE);
            }
        }
        for (ChunkPos cp : world.getChunksNearby(pos)) {
            StructuredChunk sc = world.structuredChunks.get(cp);
            sc.constructIn(this);
        }
    }
}
