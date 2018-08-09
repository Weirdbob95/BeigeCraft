package util.noise;

import static util.MathUtils.ceil;
import static util.MathUtils.floor;

public class NoiseInterpolator {

    private final Noise noise;
    private final int xSize, ySize, zSize;
    private final double[][][] samples;
    private double xTransform, yTransform, zTransform, scaleFactor;

    public NoiseInterpolator(Noise noise, int xSize, int ySize, int zSize) {
        this.noise = noise;
        this.xSize = xSize;
        this.ySize = ySize;
        this.zSize = zSize;
        this.samples = new double[xSize + 1][ySize + 1][zSize + 1];
    }

    public void generate(int octaves, double frequency) {
        for (int x = 0; x <= xSize; x++) {
            for (int y = 0; y <= ySize; y++) {
                for (int z = 0; z <= zSize; z++) {
                    samples[x][y][z] = noise.fbm3d(x * scaleFactor + xTransform, y * scaleFactor + yTransform, z * scaleFactor + zTransform, octaves, frequency);
                }
            }
        }
    }

    public double get(double x, double y, double z) {
        x = (x - xTransform) / scaleFactor;
        y = (y - yTransform) / scaleFactor;
        z = (z - zTransform) / scaleFactor;

        int x0 = floor(x);
        int x1 = ceil(x);
        int y0 = floor(y);
        int y1 = ceil(y);
        int z0 = floor(z);
        int z1 = ceil(z);

        double xd = x - x0;
        double yd = y - y0;
        double zd = z - z0;

        double c00 = mix(samples[x0][y0][z0], samples[x1][y0][z0], xd);
        double c10 = mix(samples[x0][y1][z0], samples[x1][y1][z0], xd);
        double c01 = mix(samples[x0][y0][z1], samples[x1][y0][z1], xd);
        double c11 = mix(samples[x0][y1][z1], samples[x1][y1][z1], xd);

        double c0 = mix(c00, c10, yd);
        double c1 = mix(c01, c11, yd);

        return mix(c0, c1, zd);
    }

    private static double mix(double d0, double d1, double amt) {
        return d0 * (1 - amt) + d1 * amt;
    }

    public void setTransform(double xTransform, double yTransform, double zTransform, double scaleFactor) {
        this.xTransform = xTransform;
        this.yTransform = yTransform;
        this.zTransform = zTransform;
        this.scaleFactor = scaleFactor;
    }
}
