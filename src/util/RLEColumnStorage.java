package util;

import java.util.TreeMap;

public class RLEColumnStorage<T> {

    public final int size;
    public final TreeMap<Integer, T>[][] columns;
    private boolean recomputeMinMax = true;
    private int minZ, maxZ;

    public RLEColumnStorage(int size) {
        this.size = size;
        columns = new TreeMap[size][size];
        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
                columns[x][y] = new TreeMap();
            }
        }
    }

    private T columnValueAt(TreeMap<Integer, T> c, int z) {
        if (c.isEmpty() || c.lastKey() < z) {
            return null;
        } else {
            return c.ceilingEntry(z).getValue();
        }
    }

    public T get(int x, int y, int z) {
        return columnValueAt(columns[x][y], z);
    }

    public int maxZ() {
        recomputeMinMax();
        return maxZ;
    }

    public int minZ() {
        recomputeMinMax();
        return minZ;
    }

    public boolean rangeEquals(int x, int y, int zMin, int zMax, T color) {
        return columns[x][y].subMap(zMin, zMax).isEmpty() && get(x, y, zMax) == color;
    }

    private void recomputeMinMax() {
        if (recomputeMinMax) {
            minZ = Integer.MAX_VALUE;
            maxZ = Integer.MIN_VALUE;
            for (int x = 0; x < size; x++) {
                for (int y = 0; y < size; y++) {
                    if (!columns[x][y].isEmpty()) {
                        minZ = Math.min(minZ, columns[x][y].firstKey());
                        maxZ = Math.max(maxZ, columns[x][y].lastKey());
                    }
                }
            }
            recomputeMinMax = false;
        }
    }

    public void set(int x, int y, int z, T color) {
        if (get(x, y, z) != color) {
            T prevLowerColor = get(x, y, z - 1);
            if (get(x, y, z + 1) == color) {
                columns[x][y].remove(z);
            } else {
                columns[x][y].put(z, color);
            }
            if (prevLowerColor != color) {
                columns[x][y].put(z - 1, prevLowerColor);
            } else {
                columns[x][y].remove(z - 1);
            }
            recomputeMinMax = true;
        }
    }

    public void setRange(int x, int y, int zMin, int zMax, T color) {
        T prevLowerColor = get(x, y, zMin - 1);
        columns[x][y].subMap(zMin, true, zMax, true).clear();
        if (get(x, y, zMax) != color) {
            columns[x][y].put(zMax, color);
        }
        if (prevLowerColor != color) {
            columns[x][y].put(zMin - 1, prevLowerColor);
        }
        recomputeMinMax = true;
    }

    public void setRangeInfinite(int x, int y, int zMax, T color) {
        columns[x][y].headMap(zMax, true).clear();
        if (get(x, y, zMax) != color) {
            columns[x][y].put(zMax, color);
        }
        recomputeMinMax = true;
    }
}
