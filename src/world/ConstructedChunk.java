package world;

import util.RLEColumnStorage;
import static world.World.CHUNK_SIZE;

public class ConstructedChunk {

    public final RLEColumnStorage<BlockType> blockStorage = new RLEColumnStorage(CHUNK_SIZE);

    public void fillWith(BlockType bt, int zMax) {
        for (int x = 0; x < CHUNK_SIZE; x++) {
            for (int y = 0; y < CHUNK_SIZE; y++) {
                blockStorage.setRangeInfinite(x, y, zMax, bt);
            }
        }
    }
}
