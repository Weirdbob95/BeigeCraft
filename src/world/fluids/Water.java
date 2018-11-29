package world.fluids;

import graphics.Graphics;
import static graphics.VoxelRenderer.DIRS;
import static util.math.MathUtils.clamp;
import static util.math.MathUtils.vecMap;
import util.math.Vec2d;
import util.math.Vec3d;
import util.math.Vec4d;
import world.fluids.FluidManager.FluidData;

/**
 *
 * @author nikolas
 */
public class Water {
    
    public static void render(FluidData fluid, FluidManager manager, Vec3d pos) {
        
        double totalFluid = 0;
        for (int i = 0; i > -5; i--) {
            double fluidHere = manager.get(pos.add(new Vec3d(0, 0, i)), manager.fluidBlocks).amount;
            //TODO add check here for water vs other fluid
            if  (fluidHere > 0) {
                totalFluid += fluidHere;
            } else {
               break;
            }
        }
        
        double f = fluid.totalFlow();
        Vec4d color = new Vec4d(.2 + 5 * f, .2 + 5 * f, 1, clamp(totalFluid * 5, .2, .5));
        double height = manager.renderHeight(pos);
        for (Vec3d v : DIRS) {
            double otherHeight = manager.renderHeight(pos.add(v));
            if (otherHeight < (v.z == 0 ? height : 1e-3) - 1e-4 && manager.world.getBlock(pos.add(v)) == null) {
                if (v.z == 1) {
                   Graphics.drawRectangle3d(pos.add(new Vec3d(0, 0, height)), vecMap(v, Math::abs), 0, new Vec2d(1, 1), color);
                } else if (v.y != 0) {
                   Graphics.drawRectangle3d(pos.add(vecMap(v, x -> x > 0 ? 1. : 0)).add(new Vec3d(0, 0, otherHeight)),
                            vecMap(v, Math::abs), Math.PI, new Vec2d(1, height - otherHeight), color);
                } else {
                    Graphics.drawRectangle3d(pos.add(vecMap(v, x -> ((x > 0) ? 1. : 0))).add(new Vec3d(0, 0, otherHeight)),
                            vecMap(v, Math::abs), 0, new Vec2d(1, height - otherHeight), color);
                }
            }
        }
    }
}
