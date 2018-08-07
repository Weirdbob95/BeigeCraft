package world.biomes;

public enum Biome {

    PLAINS(1, .05),
    FOREST(1, 1),
    DESERT(.5, 0),
    JUNGLE(1.5, 10),
    TAIGA(1, 1),
    SNOW(1.5, 0),
    TUNDRA(1, .05),
    ARID(1, 0),
    ROCK(1, 0);

    public final double elevation;
    public final double vegetation;

    private Biome(double elevation, double vegetation) {
        this.elevation = elevation;
        this.vegetation = vegetation;
    }
}
