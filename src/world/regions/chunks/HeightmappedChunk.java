package world.regions.chunks;

import java.util.LinkedList;
import java.util.List;
import java.util.function.IntToDoubleFunction;
import static util.math.MathUtils.ceil;
import util.math.Vec3d;
import util.noise.NoiseInterpolator;
import util.noise.ZeroCrossing;
import static util.noise.ZeroCrossing.findZeroCrossings;
import world.regions.RegionPos;
import world.World;
import static world.World.CHUNK_SIZE;
import world.biomes.BiomeData;

public class HeightmappedChunk extends AbstractChunk {

    public BiomeData[][] biomemap = new BiomeData[CHUNK_SIZE][CHUNK_SIZE];
    public List<ZeroCrossing>[][] heightmap = new List[CHUNK_SIZE][CHUNK_SIZE];
    public List<ZeroCrossing>[][] cavemap = new List[CHUNK_SIZE][CHUNK_SIZE];

    public double xyLength = 300;
    public double craziness = 1;
    public int height = 300;
    public int zMin = -100, zMax = height + 100;
    public double caveDensity = 1;

    public HeightmappedChunk(World world, RegionPos pos) {
        super(world, pos);
    }

    public double altitudeAt(int x, int y) {
        return Math.pow(world.noise("heightmappedchunk1").noise2d(x, y, .001), 3) + .1;
    }

    public int elevationAt(int x, int y) {
        return heightmap[x][y].get(heightmap[x][y].size() - 2).end;
    }

    @Override
    protected void generate() {
        NoiseInterpolator terrain = new NoiseInterpolator(world.noise("heightmappedchunk0"), 8, 8, ceil((zMax - zMin) * craziness / 4));
        terrain.setTransform(worldPos(), new Vec3d(1, 1, 1).mul(CHUNK_SIZE / 8.));
        terrain.generate(12, 1 / xyLength);

//        NoiseInterpolator caves1 = new NoiseInterpolator(world.noise("constructedchunk1"), 8, 8, ceil((zMax - zMin) * craziness / 4));
//        NoiseInterpolator caves2 = new NoiseInterpolator(world.noise("constructedchunk2"), 8, 8, ceil((zMax - zMin) * craziness / 4));
//        caves1.setTransform(worldPos(), new Vec3d(1, 1, 2).mul(CHUNK_SIZE / 8.));
//        caves2.setTransform(worldPos(), new Vec3d(1, 1, 2).mul(CHUNK_SIZE / 8.));
//        caves1.generate(6, .003 * caveDensity);
//        caves2.generate(6, .003 * caveDensity);
        for (int x = 0; x < CHUNK_SIZE; x++) {
            for (int y = 0; y < CHUNK_SIZE; y++) {
                int wx = x + pos.x * CHUNK_SIZE;
                int wy = y + pos.y * CHUNK_SIZE;
                biomemap[x][y] = BiomeData.generate(world, wx, wy);
                double wHeight = altitudeAt(wx, wy) * height * biomemap[x][y].averageElevation();
                IntToDoubleFunction density = z -> terrain.get(wx, wy, (z - zMin) * craziness) - z / wHeight + .1;
                heightmap[x][y] = findZeroCrossings(density, 0, zMax, .02 + 1 / wHeight);

//                IntToDoubleFunction caves = z
//                        -> Math.abs(caves1.get(wx, wy, (z - zMin) * 2) - .5)
//                        + Math.abs(caves2.get(wx, wy, (z - zMin) * 2) - .5)
//                        - .04 * caveDensity * (1 - 17 / (Math.max(density.applyAsDouble(z), 0) * wHeight + 20.))
//                        - 5 * Math.min(density.applyAsDouble(z), 0);
//                cavemap[x][y] = findZeroCrossings(caves, zMin, zMax, .1);
                cavemap[x][y] = new LinkedList();
            }
        }
    }
}
