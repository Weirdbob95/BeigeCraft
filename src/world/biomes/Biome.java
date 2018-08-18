package world.biomes;

public enum Biome {

    PLAINS(1, .05, .8),
    FOREST(1, 1, 1),
    DESERT(.5, 0, 0),
    COLD_DESERT(1, 0, 0),
    JUNGLE(1.5, 5, 1),
    TAIGA(1, 1, 1),
    SNOW(1.5, 0, 0),
    TUNDRA(1, .05, .5),
    ROCK(1, 0, 0);

    public final double elevation;
    public final double treeDensity;
    public final double treeHeight;

    private Biome(double elevation, double treeDensity, double treeHeight) {
        this.elevation = elevation;
        this.treeDensity = treeDensity / 4;
        this.treeHeight = treeHeight * 2;
    }
}
