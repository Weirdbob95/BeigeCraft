package world.biomes;

import java.util.EnumMap;
import java.util.Map;
import java.util.Map.Entry;
import static util.MathUtils.ceil;
import static util.MathUtils.clamp;
import static util.MathUtils.floor;
import static util.MathUtils.mod;
import static util.MathUtils.round;
import world.World;
import static world.biomes.Biome.*;

public class BiomeData {

    private static final Biome[][] BIOME_ARRAY = {
        {JUNGLE, FOREST, FOREST, TAIGA, SNOW},
        {JUNGLE, FOREST, FOREST, TAIGA, SNOW},
        {JUNGLE, FOREST, FOREST, TUNDRA, SNOW},
        {PLAINS, PLAINS, PLAINS, TUNDRA, SNOW},
        {DESERT, DESERT, COLD_DESERT, TUNDRA, ROCK}
    };

    private final Map<Biome, Double> biomeStrengths = new EnumMap(Biome.class);
    private Biome plurality;
    private double totalStrength;

    public static BiomeData generate(World world, double x, double y) {
        double freqMult = .5;
        double noisyness = .05;

        BiomeData bd = new BiomeData();
        double temp = world.noise("biomedata1").fbm2d(x, y, 4, .001 * freqMult) - .5;
        double hum = world.noise("biomedata2").fbm2d(x, y, 4, .001 * freqMult) - .5;
        temp /= 1 + 300 / Math.sqrt(x * x + y * y);
        hum /= 1 + 300 / Math.sqrt(x * x + y * y);
        double temp2 = temp + (world.noise("biomedata3").fbm2d(x, y, 3, .1 * freqMult) - .5) * noisyness;
        double hum2 = hum + (world.noise("biomedata4").fbm2d(x, y, 3, .1 * freqMult) - .5) * noisyness;

        double ext = 8;
        temp = clamp(2 + temp * ext, 0, 4);
        hum = clamp(2 + hum * ext, 0, 4);
        temp2 = clamp(2 + temp2 * ext, 0, 4);
        hum2 = clamp(2 + hum2 * ext, 0, 4);

        double edge = .4;
        bd.addStrength(BIOME_ARRAY[floor(temp)][floor(hum)], Math.min(1 - mod(temp, 1), 1 - mod(hum, 1)) - .5 + edge);
        bd.addStrength(BIOME_ARRAY[floor(temp)][ceil(hum)], Math.min(1 - mod(temp, 1), mod(hum, 1)) - .5 + edge);
        bd.addStrength(BIOME_ARRAY[ceil(temp)][floor(hum)], Math.min(mod(temp, 1), 1 - mod(hum, 1)) - .5 + edge);
        bd.addStrength(BIOME_ARRAY[ceil(temp)][ceil(hum)], Math.min(mod(temp, 1), mod(hum, 1)) - .5 + edge);
        bd.update();

        bd.plurality = BIOME_ARRAY[round(temp2)][round(hum2)];

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
        //plurality = biomeStrengths.entrySet().stream().max(Comparator.comparingDouble(Entry::getValue)).map(Entry::getKey).orElse(null);
        totalStrength = biomeStrengths.values().stream().mapToDouble(d -> d).sum();
    }
}
