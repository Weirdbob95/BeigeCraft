package world.chunks;

import util.vectors.Vec3d;
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
                biomemap[x][y] = BiomeData.generate(world, new Vec3d(x + pos.x * CHUNK_SIZE, y + pos.y * CHUNK_SIZE, 0));
                heightmap[x][y] = (int) (heightAt(x + pos.x * CHUNK_SIZE, y + pos.y * CHUNK_SIZE) * biomemap[x][y].averageElevation());
            }
        }
    }

    private double heightAt(int x, int y) {
        return 100 * world.noise.perlin(x, y, .003)
                + 20 * world.noise.perlin(x, y, .015)
                + 4 * world.noise.perlin(x, y, .075);
    }
}
