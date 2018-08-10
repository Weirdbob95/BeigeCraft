package world.chunks;

import static util.MathUtils.floor;
import world.ChunkPos;
import world.World;
import static world.World.CHUNK_SIZE;
import world.biomes.BiomeData;

public class HeightmappedChunk extends AbstractChunk {

    public final BiomeData[][] biomemap = new BiomeData[CHUNK_SIZE][CHUNK_SIZE];
    public final int[][] heightmap = new int[CHUNK_SIZE][CHUNK_SIZE];

    public HeightmappedChunk(World world, ChunkPos pos) {
        super(world, pos);
    }

    @Override
    protected void generate() {
        for (int x = 0; x < CHUNK_SIZE; x++) {
            for (int y = 0; y < CHUNK_SIZE; y++) {
                biomemap[x][y] = BiomeData.generate(world, worldPos(x, y, 0));
                heightmap[x][y] = floor(heightAt(x + pos.x * CHUNK_SIZE, y + pos.y * CHUNK_SIZE) * biomemap[x][y].averageElevation());
            }
        }
    }

    private double heightAt(int x, int y) {
        return (100 * world.noise("heightmappedchunk1").noise2d(x, y, .003)
                + 20 * world.noise("heightmappedchunk2").noise2d(x, y, .015)
                + 4 * world.noise("heightmappedchunk3").noise2d(x, y, .075)
                - 40) * world.noise("heightmappedchunk4").noise2d(x, y, .001) * 2;
    }
}
