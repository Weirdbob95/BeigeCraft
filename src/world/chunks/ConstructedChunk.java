package world.chunks;

import util.RLEColumnStorage;
import util.noise.NoiseInterpolator;
import util.vectors.Vec3d;
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
        double caveDensity = 2;
        double ironDensity = 1;
        int minZ = -200;

        NoiseInterpolator caves1 = new NoiseInterpolator(world.noise("constructedchunk1"), 8, 8, 128);
        NoiseInterpolator caves2 = new NoiseInterpolator(world.noise("constructedchunk2"), 8, 8, 128);
        caves1.setTransform(worldPos(), new Vec3d(1, 1, 2).mul(CHUNK_SIZE / 8.));
        caves2.setTransform(worldPos(), new Vec3d(1, 1, 2).mul(CHUNK_SIZE / 8.));
        caves1.generate(6, .003 * caveDensity);
        caves2.generate(6, .003 * caveDensity);

        NoiseInterpolator iron = new NoiseInterpolator(world.noise("constructedchunk3"), 8, 8, 128);
        iron.setTransform(worldPos(), new Vec3d(1, 1, 2).mul(CHUNK_SIZE / 8.));
        iron.generate(1, .05 * ironDensity);

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
                for (int z = minZ; z <= elevation; z++) {
                    if (Math.abs(caves1.get(worldPos(x, y, (z - minZ) * 2)) - .5) + Math.abs(caves2.get(worldPos(x, y, (z - minZ) * 2)) - .5)
                            < .04 * caveDensity * (1 - 15 / (elevation - z + 20.))) {
                        blockStorage.set(x, y, z, null);
                    } else if (iron.get(worldPos(x, y, (z - minZ) * 2)) * (1 - 15 / (elevation - z + 20.))
                            > .5 + .25 / ironDensity) {
                        blockStorage.set(x, y, z, IRON_ORE);
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
