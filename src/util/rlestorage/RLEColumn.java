package util.rlestorage;

import java.util.AbstractMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import static util.math.MathUtils.mod;

public class RLEColumn<T> implements Iterable<Entry<Integer, T>> {

    public final int x, y;

    /*
    Each element in data is a compressed representation of a paired position
    and block. The position occupies the lower 16 bits of the int, and the
    block ID occupies the upper 16 bits of the int.
     */
    private int[] data = {};
    private final IntConverter<T> ic;

    public RLEColumn(int x, int y, IntConverter<T> ic) {
        this.x = x;
        this.y = y;
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

    T get(int pos) {
        int i = findIndexAbove(pos);
        if (i == data.length) {
            return null;
        } else {
            return blockType(data[i]);
        }
    }

    public boolean isEmpty() {
        return data.length == 0;
    }

    @Override
    public Iterator<Entry<Integer, T>> iterator() {
        return new Iterator() {
            int pos = 0;

            @Override
            public boolean hasNext() {
                return pos < data.length;
            }

            @Override
            public Object next() {
                int i = data[pos];
                pos++;
                return new AbstractMap.SimpleImmutableEntry(position(i), blockType(i));
            }

        };
    }

    private int makeData(int pos, T t) {
        return mod(pos, 1 << 16) + (ic.toInt(t) << 16);
    }

    int maxPos() {
        return position(data[data.length - 1]);
    }

    int minPos() {
        return position(data[0]);
    }

    private int position(int d) {
        return (short) (d & 0xFFFF);
    }

    boolean rangeEquals(int posMin, int posMax, T t) {
        int i1 = findIndexAbove(posMin);
        int i2 = findIndexAbove(posMax);
        return (i1 == i2 && ((t == null && i2 == data.length) || blockType(data[i2]) == t));
    }

    void set(int pos, T t) {
        TreeMap<Integer, T> dataTree = dataTree();
        dataTree.put(pos - 1, get(pos - 1));
        dataTree.put(pos, t);
        setDataTree(dataTree);
    }

    void setRange(int posMin, int posMax, T t) {
        TreeMap<Integer, T> dataTree = dataTree();
        dataTree.put(posMin - 1, get(posMin - 1));
        dataTree.subMap(posMin, posMax).clear();
        dataTree.put(posMax, t);
        setDataTree(dataTree);
    }

    void setRangeInfinite(int posMax, T t) {
        TreeMap<Integer, T> dataTree = dataTree();
        dataTree.headMap(posMax).clear();
        dataTree.put(posMax, t);
        setDataTree(dataTree);
    }

    private void setDataTree(TreeMap<Integer, T> dataTree) {
        Iterator<Map.Entry<Integer, T>> iterator = dataTree.descendingMap().entrySet().iterator();
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
            Map.Entry<Integer, T> e = iterator.next();
            data[i] = makeData(e.getKey(), e.getValue());
            i++;
        }
    }
}
