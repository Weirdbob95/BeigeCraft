package world.biomes;

public enum Biome {

    PLAINS(1),
    FOREST(1.5),
    DESERT(.5);

    public final double elevation;

    private Biome(double elevation) {
        this.elevation = elevation;
    }
}
