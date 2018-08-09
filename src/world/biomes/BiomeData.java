package world.biomes;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import static util.MathUtils.ceil;
import static util.MathUtils.clamp;
import static util.MathUtils.floor;
import static util.MathUtils.mod;
import util.vectors.Vec3d;
import world.World;
import static world.biomes.Biome.*;

public class BiomeData {

    private static final Biome[][] BIOME_ARRAY = {
        {JUNGLE, FOREST, FOREST, TAIGA, SNOW},
        {JUNGLE, FOREST, FOREST, TAIGA, SNOW},
        {JUNGLE, FOREST, FOREST, TUNDRA, SNOW},
        {PLAINS, PLAINS, PLAINS, TUNDRA, SNOW},
        {DESERT, DESERT, DESERT, TUNDRA, ROCK}
    };

    private final Map<Biome, Double> biomeStrengths = new HashMap();
    private Biome plurality;
    private double totalStrength;

    public static BiomeData generate(World world, Vec3d pos) {
        double freqMult = 1;

        BiomeData bd = new BiomeData();
//        double temp = world.noise.perlin(pos.x, pos.y, 10000, .001 * freqMult) - .5;
//        double hum = world.noise.perlin(pos.x, pos.y, 20000, .001 * freqMult) - .5;
        double temp = world.noise("biomedata1").fbm3d(pos.x, pos.y, 10000, 4, .001 * freqMult)
                + world.noise("biomedata2").fbm3d(pos.x, pos.y, 20000, 3, .1 * freqMult) * .02 - .501;
        double hum = world.noise("biomedata3").fbm3d(pos.x, pos.y, 30000, 4, .001 * freqMult)
                + world.noise("biomedata4").fbm3d(pos.x, pos.y, 40000, 3, .1 * freqMult) * .02 - .501;

        double ext = 8;
        temp = clamp(2 + temp * ext, 0, 4);
        hum = clamp(2 + hum * ext, 0, 4);

        double edge = .2;
        bd.addStrength(BIOME_ARRAY[floor(temp)][floor(hum)], Math.min(1 - mod(temp, 1), 1 - mod(hum, 1)) - .5 + edge);
        bd.addStrength(BIOME_ARRAY[floor(temp)][ceil(hum)], Math.min(1 - mod(temp, 1), mod(hum, 1)) - .5 + edge);
        bd.addStrength(BIOME_ARRAY[ceil(temp)][floor(hum)], Math.min(mod(temp, 1), 1 - mod(hum, 1)) - .5 + edge);
        bd.addStrength(BIOME_ARRAY[ceil(temp)][ceil(hum)], Math.min(mod(temp, 1), mod(hum, 1)) - .5 + edge);
        bd.update();
        return bd;
    }

    private void addStrength(Biome b, double strength) {
        if (strength > 0) {
            biomeStrengths.put(b, strength + biomeStrengths.getOrDefault(b, 0.));
        }
    }

    public double averageElevation() {
        double r = 0;
        for (Entry<Biome, Double> e : biomeStrengths.entrySet()) {
            r += e.getKey().elevation * e.getValue();
        }
        return r / totalStrength;
    }

    public double averageTreeDensity() {
        double r = 0;
        for (Entry<Biome, Double> e : biomeStrengths.entrySet()) {
            r += e.getKey().treeDensity * e.getValue();
        }
        return r / totalStrength;
    }

    public double averageTreeHeight() {
        double r = 0;
        for (Entry<Biome, Double> e : biomeStrengths.entrySet()) {
            r += e.getKey().treeHeight * e.getValue();
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
        update();
    }

    private void update() {
        plurality = biomeStrengths.entrySet().stream().max(Comparator.comparingDouble(Entry::getValue)).map(Entry::getKey).orElse(null);
        totalStrength = biomeStrengths.values().stream().mapToDouble(d -> d).sum();
    }
}
