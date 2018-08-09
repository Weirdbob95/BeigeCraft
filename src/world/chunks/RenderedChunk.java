package world.chunks;

import graphics.ChunkRenderer;
import opengl.Camera;
import util.vectors.Vec3d;
import world.ChunkPos;
import world.World;
import static world.World.CHUNK_SIZE;

public class RenderedChunk extends AbstractChunk {

    private ChunkRenderer chunkRenderer;
    public boolean shouldRegenerate;

    public RenderedChunk(World world, ChunkPos pos) {
        super(world, pos);
    }

    @Override
    public void cleanup() {
        if (chunkRenderer != null) {
            chunkRenderer.cleanup();
        }
    }

    @Override
    protected void generate() {
        chunkRenderer = new ChunkRenderer(world, pos);
    }

    private boolean intersectsFrustum() {
        return Camera.camera3d.getViewFrustum().testAab(CHUNK_SIZE * pos.x, CHUNK_SIZE * pos.y, world.constructedChunks.get(pos).blockStorage.minZ(),
                CHUNK_SIZE * (pos.x + 1), CHUNK_SIZE * (pos.y + 1), world.constructedChunks.get(pos).blockStorage.maxZ() + 1);
    }

    public void render() {
        if (!intersectsFrustum()) {
            return;
        }
        if (shouldRegenerate) {
            shouldRegenerate = false;
            generateOuter();
        }
        Vec3d worldPos = new Vec3d(CHUNK_SIZE * pos.x, CHUNK_SIZE * pos.y, 0);
        chunkRenderer.render(worldPos, 0, 1, new Vec3d(0, 0, 0));
    }
}
