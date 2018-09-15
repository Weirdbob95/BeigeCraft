package util.rlestorage;

import definitions.BlockType;
import static definitions.Loader.getBlockByID;

public interface IntConverter<T> {

    public T fromInt(int i);

    public int toInt(T t);

    public static class BlockTypeConverter implements IntConverter<BlockType> {

        @Override
        public BlockType fromInt(int i) {
            return getBlockByID(i);
        }

        @Override
        public int toInt(BlockType t) {
            if (t == null) {
                return 0;
            }
            return t.id;
        }
    }

    public static class IntegerConverter implements IntConverter<Integer> {

        @Override
        public Integer fromInt(int i) {
            if (i == 0) {
                return null;
            }
            return i - 1;
        }

        @Override
        public int toInt(Integer t) {
            if (t == null) {
                return 0;
            }
            return t + 1;
        }
    }
}
