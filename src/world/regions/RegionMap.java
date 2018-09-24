package world.regions;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import util.Multithreader;
import util.math.Vec3d;
import world.World;

public class RegionMap<T extends AbstractRegion> {

    private final Map<RegionPos, T> chunks = new ConcurrentHashMap();
    private final Map<RegionPos, Object> locks = new ConcurrentHashMap();
    private final Set<RegionPos> border = Collections.newSetFromMap(new ConcurrentHashMap());
    private final World world;
    private final int size;
    private final BiFunction<World, RegionPos, T> constructor;

    public RegionMap(World world, int size, BiFunction<World, RegionPos, T> constructor) {
        this.world = world;
        this.size = size;
        this.constructor = constructor;
    }

    public List<RegionPos> allGenerated() {
        return chunks.entrySet().stream().filter(e -> e.getValue().isGenerated())
                .map(Map.Entry::getKey).collect(Collectors.toList());
    }

    public Set<RegionPos> border() {
        return border;
    }

    public T get(Vec3d pos) {
        return get(world.getChunkPos(pos));
    }

    public T get(RegionPos pos) {
        locks.putIfAbsent(pos, new Object());
        synchronized (locks.get(pos)) {
            if (!chunks.containsKey(pos)) {
                chunks.put(pos, constructor.apply(world, pos));
                updateBorder(pos);
                chunks.get(pos).generateOuter();
            }
            return chunks.get(pos);
        }
    }

    public boolean has(RegionPos pos) {
        return chunks.containsKey(pos);
    }

    public void lazyGenerate(RegionPos pos) {
        if (has(pos)) {
            return;
        }
        locks.putIfAbsent(pos, new Object());
        synchronized (locks.get(pos)) {
            chunks.put(pos, constructor.apply(world, pos));
            updateBorder(pos);
            Multithreader.run(() -> chunks.get(pos).generateOuter());
        }
    }

    public void remove(RegionPos pos) {
        locks.putIfAbsent(pos, new Object());
        synchronized (locks.get(pos)) {
            if (chunks.containsKey(pos)) {
                T t = chunks.remove(pos);
                updateBorder(pos);
                t.cleanup();
            }
        }
    }

    public void removeDistant(RegionPos camera, int maxDist) {
        for (RegionPos pos : chunks.keySet()) {
            if (camera.distance(pos) > maxDist) {
                remove(pos);
            }
        }
    }

    private boolean shouldBeBorder(RegionPos pos) {
        if (has(pos)) {
            return false;
        }
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                if (has(new RegionPos(pos.x + i, pos.y + j))) {
                    return true;
                }
            }
        }
        return false;
    }

    private void updateBorder(RegionPos pos) {
        synchronized (border) {
            for (int i = -1; i <= 1; i++) {
                for (int j = -1; j <= 1; j++) {
                    if (shouldBeBorder(new RegionPos(pos.x + i, pos.y + j))) {
                        border.add(new RegionPos(pos.x + i, pos.y + j));
                    } else {
                        border.remove(new RegionPos(pos.x + i, pos.y + j));
                    }
                }
            }
        }
    }
}
