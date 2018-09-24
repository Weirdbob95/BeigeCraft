package world.regions.provinces;

import java.util.ArrayList;
import java.util.List;
import static util.math.MathUtils.poissonSample;
import static util.math.MathUtils.round;
import world.World;
import world.regions.RegionPos;
import world.regions.chunks.StructuredChunk;
import world.structures.House;
import world.structures.Structure;
import world.structures.StructurePlan;

public class StructuredProvince extends AbstractProvince {

    public List<StructurePlan> structurePlans = new ArrayList();

    public StructuredProvince(World world, RegionPos pos) {
        super(world, pos);
    }

    @Override
    protected void generate() {
        int numVillages = poissonSample(random, size() * size() / 1e6);
        for (int i = 0; i < numVillages; i++) {
            int villageX = random.nextInt(size());
            int villageY = random.nextInt(size());
            List<HousePlot> houses = new ArrayList();
            int numHouses = poissonSample(random, 50);
            for (int j = 0; j < numHouses; j++) {
                placeHouse(villageX, villageY, houses);
            }
            structurePlans.addAll(houses);
        }
    }

    public void placeHouse(int villageX, int villageY, List<HousePlot> houses) {
        int width = 15 + random.nextInt(10);
        int height = 15 + random.nextInt(10);
        double distance = 0;
        while (true) {
            double theta = random.nextDouble() * 360;
            int x = villageX + round(distance * Math.cos(theta) - width / 2.);
            int y = villageY + round(distance * Math.sin(theta) - width / 2.);
            HousePlot h = new HousePlot(this, x, y, width, height);
            boolean canPlace = true;
            for (HousePlot h2 : houses) {
                if (h.intersects(h2)) {
                    canPlace = false;
                    break;
                }
            }
            if (canPlace) {
                houses.add(h);
                break;
            }
            distance += random.nextDouble() * 150;
        }
    }

    public static class HousePlot extends StructurePlan {

        public final int w, h;

        public HousePlot(StructuredProvince sp, int x, int y, int w, int h) {
            super(sp, x, y);
            this.w = w;
            this.h = h;
        }

        @Override
        public Structure construct(StructuredChunk sc, int x, int y) {
            return new House(sc, x, y, sc.world.heightmappedChunks.get(sc.pos).elevationAt(x, y) + 1, w - 2, h - 2);
        }

        public boolean intersects(HousePlot other) {
            double wDist = w / 2. + other.w / 2.;
            double hDist = h / 2. + other.h / 2.;
            return !(x + wDist < other.x || y + hDist < other.y || other.x + wDist < x || other.y + hDist < y);
        }
    }
}
