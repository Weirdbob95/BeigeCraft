package world.chunks;

import java.util.ArrayList;
import java.util.List;
import util.math.Vec3d;
import world.ChunkPos;
import world.TerrainObjectInstance;
import world.World;
import static world.World.CHUNK_SIZE;
import world.biomes.Biome;
import world.structures.Structure;
import world.structures.Structure.Cactus;
import world.structures.Structure.Flower;
import world.structures.Tree;

public class StructuredChunk extends AbstractChunk {

    private final List<Structure> structures = new ArrayList();

    public StructuredChunk(World world, ChunkPos pos) {
        super(world, pos);
    }

    public void constructIn(ConstructedChunk cc) {
        Vec3d translation = new Vec3d(-(cc.pos.x - pos.x) * CHUNK_SIZE, -(cc.pos.y - pos.y) * CHUNK_SIZE, 0);
        for (Structure s : structures) {
            s.blocks.copyTo(cc.blockStorage, s.x + (int) translation.x, s.y + (int) translation.y, s.z);
            for (TerrainObjectInstance toi : s.terrainObjects) {
                for (Vec3d v : toi.getOccupancy()) {
                    v = v.add(translation);
                    if (v.x >= 0 && v.x < CHUNK_SIZE && v.y >= 0 && v.y < CHUNK_SIZE) {
                        cc.terrainObjectOccupancyMap.put(v, toi);
                    }
                }
                if (toi.chunkPos.equals(cc.pos)) {
                    cc.terrainObjects.add(toi);
                }
            }
        }
    }

    @Override
    protected void generate() {
        HeightmappedChunk hc = world.heightmappedChunks.get(pos);
        for (int i = 0; i < CHUNK_SIZE * CHUNK_SIZE / 10.; i++) {
            int x = random.nextInt(CHUNK_SIZE);
            int y = random.nextInt(CHUNK_SIZE);
            if (hc.biomemap[x][y].plurality().treeDensity > 0 && random.nextDouble() < hc.biomemap[x][y].averageTreeDensity() * .1) {
                structures.add(new Tree(this, x, y, hc.elevationAt(x, y) + 1,
                        (2 + random.nextInt(8) + random.nextInt(8)) * hc.biomemap[x][y].averageTreeHeight()));
            } else if (hc.biomemap[x][y].plurality() == Biome.COLD_DESERT && random.nextDouble() < hc.biomemap[x][y].get(Biome.COLD_DESERT) * .01) {
                structures.add(new Cactus(this, x, y, hc.elevationAt(x, y) + 1, 2 + random.nextInt(6)));
            }
        }
        for (int i = 0; i < CHUNK_SIZE * CHUNK_SIZE / 10.; i++) {
            int x = random.nextInt(CHUNK_SIZE);
            int y = random.nextInt(CHUNK_SIZE);
            if (hc.biomemap[x][y].plurality().treeDensity > 0 && random.nextDouble() < hc.biomemap[x][y].averageTreeDensity() * .1) {
                structures.add(new Flower(this, x, y, hc.elevationAt(x, y) + 1));
            }
        }
    }
}
