package world.chunks;

import util.RLEColumnStorage;
import world.BlockType;
import static world.BlockType.*;
import world.ChunkPos;
import world.World;
import static world.World.CHUNK_SIZE;

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
                switch (hc.biomemap[x][y].plurality()) {
                    case FOREST:
                    case PLAINS:
                    case JUNGLE:
                        blockStorage.setRangeInfinite(x, y, elevation, GRASS);
                        blockStorage.setRangeInfinite(x, y, elevation - 1, DIRT);
                        break;
                    case TAIGA:
                    case TUNDRA:
                        blockStorage.setRangeInfinite(x, y, elevation, TUNDRA_GRASS);
                        blockStorage.setRangeInfinite(x, y, elevation - 1, DIRT);
                        break;
                    case SNOW:
                        blockStorage.setRangeInfinite(x, y, elevation, SNOWY_GRASS);
                        blockStorage.setRangeInfinite(x, y, elevation - 1, DIRT);
                        break;
                    case DESERT:
                        blockStorage.setRangeInfinite(x, y, elevation, SAND);
                        break;
                    case ROCK:
                        blockStorage.setRangeInfinite(x, y, elevation, STONE);
                        break;
                }
                blockStorage.setRangeInfinite(x, y, elevation - 3, STONE);

                // CAVES
                double density = 1;
                for (int z = -100; z <= elevation; z++) {
                    if ((Math.abs(world.noise.multi(x + pos.x * CHUNK_SIZE, y + pos.y * CHUNK_SIZE, z * 1.5, 4, .005 * density) - .5))
                            + (Math.abs(world.noise.multi(x + pos.x * CHUNK_SIZE, y + pos.y * CHUNK_SIZE, z * 1.5 + 1000, 4, .005 * density) - .5)) < .04 * density * (1 - 15 / (elevation - z + 20.))) {
                        blockStorage.set(x, y, z, null);
                    }
                }
            }
        }
        for (ChunkPos cp : world.getChunksNearby(pos)) {
            StructuredChunk sc = world.structuredChunks.get(cp);
            sc.constructIn(this);
        }
        world.heightmappedChunks.remove(pos);
    }
}
