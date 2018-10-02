package world.regions.chunks;

import static definitions.Loader.getTerrainObject;
import definitions.TerrainObjectType;
import java.util.ArrayList;
import java.util.List;
import static util.math.MathUtils.mod;
import static util.math.MathUtils.poissonSample;
import util.math.Vec2d;
import util.math.Vec3d;
import world.World;
import static world.World.CHUNK_SIZE;
import world.biomes.Biome;
import world.regions.RegionPos;
import world.regions.provinces.StructuredProvince;
import world.structures.Structure;
import world.structures.Structure.Cactus;
import world.structures.Structure.SingleTerrainObject;
import world.structures.StructurePlan;
import world.structures.Tree;

public class StructuredChunk extends AbstractChunk {

    public final List<Structure> structures = new ArrayList();

    public StructuredChunk(World world, RegionPos pos) {
        super(world, pos);
    }

    @Override
    protected void generate() {
        HeightmappedChunk hc = world.heightmappedChunks.get(pos);
        PoissonDiskChunk pdc = world.getRegionMap(PoissonDiskChunk.class).get(pos);
        for (Vec2d v : pdc.getPoints(10)) {
            int x = (int) v.x, y = (int) v.y;
            if (hc.biomemap[x][y].plurality().treeDensity > 0) {
                structures.add(new Tree(this, x, y, hc.elevationAt(x, y) + 1,
                        (6 + random.nextInt(6) + random.nextInt(6)) * hc.biomemap[x][y].averageTreeHeight()));
            } else if (hc.biomemap[x][y].plurality() == Biome.COLD_DESERT) {
                structures.add(new Cactus(this, x, y, hc.elevationAt(x, y) + 1, 2 + random.nextInt(6)));
            }
        }
//        int numToGenerate = poissonSample(random, CHUNK_SIZE * CHUNK_SIZE / 10.);
//        for (int i = 0; i < numToGenerate; i++) {
//            int x = random.nextInt(CHUNK_SIZE);
//            int y = random.nextInt(CHUNK_SIZE);
//            if (hc.biomemap[x][y].plurality().treeDensity > 0 && random.nextDouble() < hc.biomemap[x][y].averageTreeDensity() * .05) {
//                structures.add(new Tree(this, x, y, hc.elevationAt(x, y) + 1,
//                        (6 + random.nextInt(6) + random.nextInt(6)) * hc.biomemap[x][y].averageTreeHeight()));
//            } else if (hc.biomemap[x][y].plurality() == Biome.COLD_DESERT && random.nextDouble() < hc.biomemap[x][y].get(Biome.COLD_DESERT) * .01) {
//                structures.add(new Cactus(this, x, y, hc.elevationAt(x, y) + 1, 2 + random.nextInt(6)));
//            }
//        }
        generateFlora(getTerrainObject("fern"), .25);
        generateFlora(getTerrainObject("flower1"), .25);
        generateFlora(getTerrainObject("extraPlant1"), .1);
        generateFlora(getTerrainObject("extraPlant2"), .1);
        generateFlora(getTerrainObject("extraPlant3"), .1);
        generateFlora(getTerrainObject("extraPlant4"), .1);

        for (RegionPos rp : world.getRegionMap(StructuredProvince.class).get(worldPos()).pos.nearby(1)) {
            for (StructurePlan sp : world.getRegionMap(StructuredProvince.class).get(rp).structurePlans) {
                Vec3d pos = sp.worldPos().sub(worldPos());
                if (pos.x >= 0 && pos.y >= 0 && pos.x < CHUNK_SIZE && pos.y < CHUNK_SIZE) {
                    int x = mod((int) pos.x, CHUNK_SIZE);
                    int y = mod((int) pos.y, CHUNK_SIZE);
                    structures.add(sp.construct(this, x, y));
                }
            }
        }
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
}
