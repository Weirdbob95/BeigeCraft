package world.chunks;

import graphics.ChunkRenderer;
import opengl.Camera;
import util.vectors.Vec3d;
import world.ChunkPos;
import world.World;
import static world.World.CHUNK_SIZE;

public class RenderedChunk extends AbstractChunk {

    private ChunkRenderer chunkRenderer;

    public RenderedChunk(World world, ChunkPos pos) {
        super(world, pos);
    }

    @Override
    public void cleanup() {
        chunkRenderer.cleanup();
    }

    @Override
    protected void generate() {
        chunkRenderer = new ChunkRenderer(world, pos);
    }

    private boolean intersectsFrustum() {
        return Camera.camera.getViewFrustum().testAab(CHUNK_SIZE * pos.x, CHUNK_SIZE * pos.y, world.constructedChunks.get(pos).blockStorage.minZ(),
                CHUNK_SIZE * (pos.x + 1), CHUNK_SIZE * (pos.y + 1), world.constructedChunks.get(pos).blockStorage.maxZ() + 1);
    }

    public void render() {
        if (!intersectsFrustum()) {
            return;
        }
        Vec3d worldPos = new Vec3d(CHUNK_SIZE * pos.x, CHUNK_SIZE * pos.y, 0);
        chunkRenderer.render(worldPos, 0, 1, new Vec3d(0, 0, 0));
//        Vec3d min = worldPos.add(new Vec3d(0, 0, world.constructedChunks.get(pos).blockStorage.minZ()));
//        Vec3d max = worldPos.add(new Vec3d(CHUNK_SIZE, CHUNK_SIZE, world.constructedChunks.get(pos).blockStorage.maxZ()));
//        TERRAIN_SHADER.setUniform("modelViewMatrix", Camera.camera.getWorldMatrix(worldPos));
//        for (Vec3d dir : DIRS) {
//            if (Camera.camera.position.sub(min).dot(dir) > 0 || Camera.camera.position.sub(max).dot(dir) > 0) {
//                using(Arrays.asList(vaoMap.get(dir)), () -> {
//                    glDrawElements(GL_TRIANGLES, 6 * numQuadsMap.get(dir), GL_UNSIGNED_INT, 0);
//                });
//            }
//        }
    }
}
