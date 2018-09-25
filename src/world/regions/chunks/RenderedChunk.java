package world.regions.chunks;

import game.Settings;
import graphics.ChunkRenderer;
import opengl.Camera;
import world.TerrainObjectInstance;
import world.World;
import static world.World.CHUNK_SIZE;
import world.regions.RegionPos;

public class RenderedChunk extends AbstractChunk {

    private ChunkRenderer chunkRenderer;
    public boolean shouldRegenerate;
    public ConstructedChunk cc;

    public RenderedChunk(World world, RegionPos pos) {
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
        if (chunkRenderer == null) {
            chunkRenderer = new ChunkRenderer(world, pos);
        } else {
            chunkRenderer.generate();
        }
        cc = world.constructedChunks.get(pos);
    }

    private boolean intersectsFrustum() {
        return Camera.camera3d.getViewFrustum().testAab(CHUNK_SIZE * pos.x, CHUNK_SIZE * pos.y, (float) chunkRenderer.min().z,
                CHUNK_SIZE * (pos.x + 1), CHUNK_SIZE * (pos.y + 1), (float) chunkRenderer.max().z);
    }

    public void render() {
        if (!intersectsFrustum()) {
            return;
        }
        if (shouldRegenerate) {
            shouldRegenerate = false;
            generateOuter();
        }
        chunkRenderer.render(worldPos(), null);
        if (Camera.camera3d.position.sub(center()).setZ(0).length() < 200) {
            for (TerrainObjectInstance toi : cc.terrainObjects) {
                toi.render(worldPos());
            }
        }
    }

    @Override
    public int unloadDist() {
        return Settings.RENDER_DISTANCE;
    }
}
