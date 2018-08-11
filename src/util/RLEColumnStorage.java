package util;

import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.TreeMap;
import static util.MathUtils.mod;
import world.BlockType;

public class RLEColumnStorage<T> {

    private final int size;
    private final RLEColumn<T>[][] columns;
    private int maxZ, minZ;
    private boolean recomputeMinMax = true;

    public RLEColumnStorage(int size, IntConverter<T> ic) {
        this.size = size;
        columns = new RLEColumn[size][size];
        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
                columns[x][y] = new RLEColumn(ic);
            }
        }
    }

    public Iterator<Entry<Integer, T>> columnTree(int x, int y) {
        return columns[x][y].iterator();
    }

    public T get(int x, int y, int z) {
        return columns[x][y].get(z);
    }

    public int maxZ() {
        recomputeMinMax();
        return maxZ;
    }

    public int minZ() {
        recomputeMinMax();
        return minZ;
    }

    public boolean rangeEquals(int x, int y, int zMin, int zMax, T t) {
        return columns[x][y].rangeEquals(zMin, zMax, t);
    }

    private void recomputeMinMax() {
        if (recomputeMinMax) {
            minZ = Integer.MAX_VALUE;
            maxZ = Integer.MIN_VALUE;
            for (int x = 0; x < size; x++) {
                for (int y = 0; y < size; y++) {
                    if (!columns[x][y].isEmpty()) {
                        minZ = Math.min(minZ, columns[x][y].minPos());
                        maxZ = Math.max(maxZ, columns[x][y].maxPos());
                    }
                }
            }
            recomputeMinMax = false;
        }
    }

    public void set(int x, int y, int z, T t) {
        columns[x][y].set(z, t);
        recomputeMinMax = true;
    }

    public void setRange(int x, int y, int zMin, int zMax, T t) {
        columns[x][y].setRange(zMin, zMax, t);
        recomputeMinMax = true;
    }

    public void setRangeInfinite(int x, int y, int zMax, T t) {
        columns[x][y].setRangeInfinite(zMax, t);
        recomputeMinMax = true;
    }

    public static interface IntConverter<T> {

        public T fromInt(int i);

        public int toInt(T t);
    }

    public static class BlockTypeConverter implements IntConverter<BlockType> {

        @Override
        public BlockType fromInt(int i) {
            if (i == 0) {
                return null;
            }
            return BlockType.VALUES[i - 1];
        }

        @Override
        public int toInt(BlockType t) {
            if (t == null) {
                return 0;
            }
            return t.ordinal() + 1;
        }
    }

    public static class IntegerConverter implements IntConverter<Integer> {

        @Override
        public Integer fromInt(int i) {
            return i;
        }

        @Override
        public int toInt(Integer t) {
            return t;
        }
    }

    private static class RLEColumn<T> implements Iterable<Entry<Integer, T>> {

        /*
        Each element in data is a compressed representation of a paired position
        and block. The position occupies the lower 16 bits of the int, and the
        block ID occupies the upper 16 bits of the int.
         */
        private int[] data = {};
        private final IntConverter<T> ic;

        public RLEColumn(IntConverter<T> ic) {
            this.ic = ic;
        }

        private T blockType(int d) {
            return ic.fromInt(d >> 16);
        }

        private TreeMap<Integer, T> dataTree() {
            TreeMap<Integer, T> r = new TreeMap();
            for (int d : data) {
                r.put(position(d), blockType(d));
            }
            return r;
        }

        private int findIndexAbove(int pos) {
            int low = 0;
            int high = data.length;
            while (low < high) {
                int check = (low + high) / 2;
                int checkPos = position(data[check]);
                if (checkPos == pos) {
                    return check;
                } else if (checkPos > pos) {
                    high = check;
                } else {
                    low = check + 1;
                }
            }
            return low;
        }

        private T get(int pos) {
            int i = findIndexAbove(pos);
            if (i == data.length) {
                return null;
            } else {
                return blockType(data[i]);
            }
        }

        private boolean isEmpty() {
            return data.length == 0;
        }

        @Override
        public Iterator<Entry<Integer, T>> iterator() {
            return (Iterator) Arrays.stream(data).mapToObj(i -> new SimpleImmutableEntry(position(i), blockType(i))).iterator();
        }

        protected int makeData(int pos, T t) {
            return mod(pos, 1 << 16) + (ic.toInt(t) << 16);
        }

        private int maxPos() {
            return position(data[data.length - 1]);
        }

        private int minPos() {
            return position(data[0]);
        }

        private int position(int d) {
            return (short) (d & 0xFFFF);
        }

        private boolean rangeEquals(int posMin, int posMax, T t) {
            int i1 = findIndexAbove(posMin);
            int i2 = findIndexAbove(posMax);
            return (i1 == i2 && ((t == null && i2 == data.length) || blockType(data[i2]) == t));
        }

        private void set(int pos, T t) {
            TreeMap<Integer, T> dataTree = dataTree();
            dataTree.put(pos - 1, get(pos - 1));
            dataTree.put(pos, t);
            setDataTree(dataTree);
        }

        private void setRange(int posMin, int posMax, T t) {
            TreeMap<Integer, T> dataTree = dataTree();
            dataTree.put(posMin - 1, get(posMin - 1));
            dataTree.subMap(posMin, posMax).clear();
            dataTree.put(posMax, t);
            setDataTree(dataTree);
        }

        private void setRangeInfinite(int posMax, T t) {
            TreeMap<Integer, T> dataTree = dataTree();
            dataTree.headMap(posMax).clear();
            dataTree.put(posMax, t);
            setDataTree(dataTree);
        }

        private void setDataTree(TreeMap<Integer, T> dataTree) {
            Iterator<Entry<Integer, T>> iterator = dataTree.descendingMap().entrySet().iterator();
            T prev = null;
            while (iterator.hasNext()) {
                T t = iterator.next().getValue();
                if (t == prev) {
                    iterator.remove();
                }
                prev = t;
            }
            data = new int[dataTree.size()];
            iterator = dataTree.entrySet().iterator();
            int i = 0;
            while (iterator.hasNext()) {
                Entry<Integer, T> e = iterator.next();
                data[i] = makeData(e.getKey(), e.getValue());
                i++;
            }
        }
    }
}
