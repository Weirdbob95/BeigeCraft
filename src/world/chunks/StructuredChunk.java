package world.chunks;

import static definitions.Loader.getTerrainObject;
import definitions.TerrainObjectType;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import world.ChunkPos;
import world.World;
import static world.World.CHUNK_SIZE;
import world.biomes.Biome;
import world.structures.Structure;
import world.structures.Structure.Cactus;
import world.structures.Structure.SingleTerrainObject;
import world.structures.Tree;

public class StructuredChunk extends AbstractChunk {

    public final List<Structure> structures = new ArrayList();

    public StructuredChunk(World world, ChunkPos pos) {
        super(world, pos);
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
        generateFlora(getTerrainObject("fern"), .25);
        generateFlora(getTerrainObject("flower1"), .25);
        generateFlora(getTerrainObject("extraPlant1"), .1);
        generateFlora(getTerrainObject("extraPlant2"), .1);
        generateFlora(getTerrainObject("extraPlant3"), .1);
        generateFlora(getTerrainObject("extraPlant4"), .1);
    }

    private void generateFlora(TerrainObjectType tot, double density) {
        HeightmappedChunk hc = world.heightmappedChunks.get(pos);
        int numToGenerate = poissonSample(random, CHUNK_SIZE * CHUNK_SIZE / 10. * density);
        for (int i = 0; i < numToGenerate; i++) {
            int x = random.nextInt(CHUNK_SIZE);
            int y = random.nextInt(CHUNK_SIZE);
            if (hc.biomemap[x][y].plurality().treeDensity > 0 && random.nextDouble() < hc.biomemap[x][y].averageTreeDensity()) {
                int x2 = random.nextInt((int) tot.getSize().x);
                int y2 = random.nextInt((int) tot.getSize().y);
                structures.add(new SingleTerrainObject(this, x - x2, y - y2, hc.elevationAt(x, y) + 1, tot));
            }
        }
    }

    private static int poissonSample(Random random, double expected) {
        double t = 0;
        int k = 0;
        while (true) {
            t += -Math.log(random.nextDouble()) / expected;
            if (t > 1) {
                return k;
            }
            k++;
        }
    }
}
