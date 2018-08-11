package util.rlestorage;

import world.BlockType;

public interface IntConverter<T> {

    public T fromInt(int i);

    public int toInt(T t);

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
}
