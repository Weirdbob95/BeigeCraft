package world.water;

import engine.Behavior;
import graphics.Graphics;
import graphics.Model;
import static graphics.VoxelRenderer.DIRS;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import util.vectors.Vec2d;
import util.vectors.Vec3d;
import util.vectors.Vec4d;
import world.World;

public class WaterManager extends Behavior {

    public Vec3d spawnWater = null;
    public HashMap<Vec3d, Double> waterBlocks = new HashMap();
    public HashMap<Vec3d, Double> changes = new HashMap();
    public HashMap<Vec3d, Double> flow = new HashMap();
    public World world;

    private static void addToMap(Vec3d pos, double amt, Map<Vec3d, Double> map) {
        map.compute(pos, (k, v) -> {
            double n = (v == null ? 0 : v) + amt;
            return Math.abs(n) < 1e-6 ? null : n;
        });
    }

    @Override
    public void render() {
        Model m = Model.load("fireball.vox");
        double sum = 0;
        for (Entry<Vec3d, Double> e : waterBlocks.entrySet()) {
            double f = flow.getOrDefault(e.getKey(), 0.);
            Graphics.drawRectangle3d(e.getKey().add(new Vec3d(0, 0, e.getValue())), new Vec3d(0, 0, 1), 0, new Vec2d(1, 1), new Vec4d(.2 + 10 * f, .2 + 10 * f, 1, 1));
//            if (changes.containsKey(e.getKey())) {
//                m.render(e.getKey().add(new Vec3d(.5, .5, .5)), 0, Math.pow(e.getValue(), .333) / 4, m.size().mul(.5), new Vec4d(1, 1, 1, 1));
//            } else {
//                m.render(e.getKey().add(new Vec3d(.5, .5, .5)), 0, Math.pow(e.getValue(), .333) / 4, m.size().mul(.5), new Vec4d(0, 1, 1, 1));
//            }
            sum += e.getValue();
        }
        System.out.println(sum);
    }

    @Override
    public void update(double dt) {
        if (spawnWater != null) {
            waterBlocks.put(spawnWater, 1.);
        }
        changes.clear();
        flow.clear();
        for (Entry<Vec3d, Double> e : waterBlocks.entrySet()) {
            double myVal = e.getValue();
            Vec3d down = e.getKey().add(new Vec3d(0, 0, -1));
            if (world.getBlock(down) == null) {
                double amt = Math.min(1 - waterBlocks.getOrDefault(down, 0.), e.getValue());
                myVal -= amt;
                if (amt > 0) {
                    addToMap(down, amt * .9, changes);
                    addToMap(e.getKey(), -amt * .9, changes);
                    addToMap(e.getKey(), amt * .9, flow);
                }
            }
            if (myVal > 0) {
                for (Vec3d dir : DIRS.subList(0, 4)) {
                    Vec3d side = e.getKey().add(dir);
                    if (world.getBlock(side) == null) {
                        double amt = myVal - waterBlocks.getOrDefault(side, 0.);
                        if (amt > 0) {
                            addToMap(side, amt / 8, changes);
                            addToMap(e.getKey(), -amt / 8, changes);
                            addToMap(e.getKey(), amt / 8, flow);
                        }
                    }
                }
            }
        }
        for (Entry<Vec3d, Double> e : changes.entrySet()) {
            addToMap(e.getKey(), e.getValue(), waterBlocks);
        }
        for (Entry<Vec3d, Double> e : new LinkedList<>(waterBlocks.entrySet())) {
            if (e.getValue() < .001) {
                waterBlocks.remove(e.getKey());
            }
            if (e.getValue() > .99) {
                waterBlocks.put(e.getKey(), 1.);
            }
        }
    }
}
