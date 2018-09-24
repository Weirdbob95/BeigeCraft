package world.structures;

import util.math.Vec3d;
import world.regions.chunks.StructuredChunk;
import world.regions.provinces.StructuredProvince;

public abstract class StructurePlan {

    public final StructuredProvince sp;
    public final int x, y;

    public StructurePlan(StructuredProvince sp, int x, int y) {
        this.sp = sp;
        this.x = x;
        this.y = y;
    }

    public abstract Structure construct(StructuredChunk sc, int x, int y);

    public Vec3d worldPos() {
        return sp.worldPos(x, y, 0);
    }
}
