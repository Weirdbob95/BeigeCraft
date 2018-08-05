package world.biomes;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import util.vectors.Vec3d;
import world.World;

public class BiomeData {

    private final Map<Biome, Double> biomeStrengths = new HashMap();
    private Biome plurality;
    private double totalStrength;

    public static BiomeData generate(World world, Vec3d pos) {
        BiomeData bd = new BiomeData();
        double temp = world.noise.multi(pos.x, pos.y, 10000, 12, .001);
        double hum = world.noise.multi(pos.x, pos.y, 20000, 12, .001);

        double edge = .1;
        bd.set(Biome.FOREST, hum - .5 + edge);
        bd.set(Biome.PLAINS, Math.min(-hum + .5 + edge, -temp + .5 + edge));
        bd.set(Biome.DESERT, Math.min(-hum + .5 + edge, temp - .5 + edge));
//        bd.set(Biome.PLAINS, world.noise.multi(pos.x, pos.y, 10000, 2, .001) - .5);
//        bd.set(Biome.FOREST, world.noise.multi(pos.x, pos.y, 20000, 2, .001) - .5);
//        bd.set(Biome.DESERT, world.noise.multi(pos.x, pos.y, 30000, 2, .001) - .5);
        return bd;
    }

    public double averageElevation() {
        double r = 0;
        for (Entry<Biome, Double> e : biomeStrengths.entrySet()) {
            r += e.getKey().elevation * e.getValue();
        }
        return r / totalStrength;
    }

    public double get(Biome b) {
        return biomeStrengths.getOrDefault(b, 0.) / totalStrength;
    }

    public Biome plurality() {
        return plurality;
    }

    public void set(Biome b, double strength) {
        biomeStrengths.put(b, Math.max(strength, 0));
        plurality = biomeStrengths.entrySet().stream().max(Comparator.comparingDouble(Entry::getValue)).map(Entry::getKey).orElse(null);
        totalStrength = biomeStrengths.values().stream().mapToDouble(d -> d).sum();
    }
}
