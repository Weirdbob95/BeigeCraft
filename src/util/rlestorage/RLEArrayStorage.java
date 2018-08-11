package util.rlestorage;

import java.util.Arrays;
import java.util.stream.Stream;

public class RLEArrayStorage<T> extends RLEStorage<T> {

    private final int size;
    private final RLEColumn<T>[][] columns;

    public RLEArrayStorage(int size, IntConverter<T> ic) {
        this.size = size;
        columns = new RLEColumn[size][size];
        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
                columns[x][y] = new RLEColumn(x, y, ic);
            }
        }
    }

    @Override
    protected Stream<RLEColumn<T>> allColumns() {
        return Arrays.stream(columns).flatMap(Arrays::stream);
    }

    @Override
    protected RLEColumn<T> columnAt(int x, int y) {
        if (x < 0 || x >= size || y < 0 || y >= size) {
            return null;
        }
        return columns[x][y];
    }
}
