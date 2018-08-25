package world.water;

import engine.Behavior;
import graphics.Graphics;
import static graphics.VoxelRenderer.DIRS;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import static util.MathUtils.clamp;
import util.vectors.Vec2d;
import util.vectors.Vec3d;
import util.vectors.Vec4d;
import world.World;

public class WaterManager extends Behavior {

    private static final double TICK_RATE = 100;

    public Vec3d spawnWater = null;
    public HashMap<Vec3d, WaterData> waterBlocks = new HashMap();
    public HashMap<Vec3d, WaterData> changes = new HashMap();
    public HashSet<Vec3d> toUpdate = new HashSet();
    public World world;

    private double elapsedTime;

    private static void addToMap(Vec3d pos, WaterData w, Map<Vec3d, WaterData> map) {
        map.compute(pos, (k, v) -> {
            WaterData e = (v == null ? new WaterData() : v);
            e.add(w);
            return e.isZero() ? null : e;
        });
    }

    private static WaterData get(Vec3d pos, Map<Vec3d, WaterData> map) {
        return map.getOrDefault(pos, new WaterData());
    }

    private static double mix(double x, double y, double a) {
        return x * (1 - a) + y * a;
    }

    @Override
    public void render() {
        double sum = 0;
        for (Entry<Vec3d, WaterData> e : waterBlocks.entrySet()) {
            WaterData w = e.getValue();
            if (w.amount > 0) {
//                Graphics.drawRectangle3d(e.getKey().add(new Vec3d(0, 0, e.getValue().amount)), new Vec3d(0, 0, 1), 0, new Vec2d(1, 1),
//                        new Vec4d(w.flowX * 10 + .5, w.flowY * 10 + .5, w.flowZ * 10 + .5, 1));
                double f = e.getValue().flowSpeed();
                if (toUpdate.contains(e.getKey())) {
                    Graphics.drawRectangle3d(e.getKey().add(new Vec3d(0, 0, e.getValue().amount)), new Vec3d(0, 0, 1), 0, new Vec2d(1, 1), new Vec4d(.2 + 10 * f, .2 + 10 * f, 1, 1));
                } else {
                    Graphics.drawRectangle3d(e.getKey().add(new Vec3d(0, 0, e.getValue().amount)), new Vec3d(0, 0, 1), 0, new Vec2d(1, 1), new Vec4d(1, .2 + 10 * f, 1, 1));
                }
                sum += w.amount;
            }
        }
        System.out.println(sum);
    }

    @Override
    public void update(double dt) {
        elapsedTime += dt;
        while (elapsedTime > 1 / TICK_RATE) {
            elapsedTime -= 1 / TICK_RATE;
            changes.clear();
            HashSet newToUpdate = new HashSet();

            if (spawnWater != null) {
                addToMap(spawnWater, new WaterData(1, 0, 0, 0), changes);
            }

            for (Vec3d pos : toUpdate) {
                WaterData posW = get(pos, waterBlocks);

                WaterData otherX = get(pos.add(new Vec3d(1, 0, 0)), waterBlocks);
                double desiredFlowX = posW.flowX + (posW.amount - otherX.amount) * .5;
                double averageFlowX = .25 * (get(pos.add(new Vec3d(0, -1, 0)), waterBlocks).flowX
                        + get(pos.add(new Vec3d(0, 1, 0)), waterBlocks).flowX
                        + get(pos.add(new Vec3d(0, 0, -1)), waterBlocks).flowX
                        + get(pos.add(new Vec3d(0, 0, 1)), waterBlocks).flowX);
                desiredFlowX = mix(desiredFlowX, averageFlowX, .2);
                double actualFlowX = mix(posW.flowX, desiredFlowX, .2);
                if (world.getBlock(pos) != null || world.getBlock(pos.add(new Vec3d(1, 0, 0))) != null) {
                    actualFlowX = 0;
                }
                actualFlowX = clamp(actualFlowX, -otherX.amount, posW.amount);
                actualFlowX = clamp(actualFlowX, posW.amount - 1, 1 - otherX.amount);

                WaterData otherY = get(pos.add(new Vec3d(0, 1, 0)), waterBlocks);
                double desiredFlowY = posW.flowY + (posW.amount - otherY.amount) * .5;
                double averageFlowY = .25 * (get(pos.add(new Vec3d(-1, 0, 0)), waterBlocks).flowY
                        + get(pos.add(new Vec3d(1, 0, 0)), waterBlocks).flowY
                        + get(pos.add(new Vec3d(0, 0, -1)), waterBlocks).flowY
                        + get(pos.add(new Vec3d(0, 0, 1)), waterBlocks).flowY);
                desiredFlowY = mix(desiredFlowY, averageFlowY, .2);
                double actualFlowY = mix(posW.flowY, desiredFlowY, .2);
                if (world.getBlock(pos) != null || world.getBlock(pos.add(new Vec3d(0, 1, 0))) != null) {
                    actualFlowY = 0;
                }
                actualFlowY = clamp(actualFlowY, -otherY.amount, posW.amount);
                actualFlowY = clamp(actualFlowY, posW.amount - 1, 1 - otherY.amount);

                WaterData otherZ = get(pos.add(new Vec3d(0, 0, 1)), waterBlocks);
                double desiredFlowZ = -1;
                double averageFlowZ = .25 * (get(pos.add(new Vec3d(-1, 0, 0)), waterBlocks).flowZ
                        + get(pos.add(new Vec3d(1, 0, 0)), waterBlocks).flowZ
                        + get(pos.add(new Vec3d(0, -1, 0)), waterBlocks).flowZ
                        + get(pos.add(new Vec3d(0, 1, 0)), waterBlocks).flowZ);
                desiredFlowZ = mix(desiredFlowZ, averageFlowZ, .2);
                double actualFlowZ = mix(posW.flowZ, desiredFlowZ, .2);
                if (world.getBlock(pos) != null || world.getBlock(pos.add(new Vec3d(0, 0, 1))) != null) {
                    actualFlowZ = 0;
                }
                actualFlowZ = clamp(actualFlowZ, -otherZ.amount, posW.amount);
                actualFlowZ = clamp(actualFlowZ, posW.amount - 1, 1 - otherZ.amount);

//                if (Math.abs(actualFlowX) > .001) {
//                    System.out.println(desiredFlowX + " " + actualFlowX);
//                }
                addToMap(pos, new WaterData(0, actualFlowX - posW.flowX, actualFlowY - posW.flowY, actualFlowZ - posW.flowZ), changes);
            }
            // Apply changes to the actual water flow rates
            for (Entry<Vec3d, WaterData> e : changes.entrySet()) {
                addToMap(e.getKey(), e.getValue(), waterBlocks);
                newToUpdate.add(e.getKey());
                for (Vec3d dir : DIRS) {
                    newToUpdate.add(e.getKey().add(dir));
                }
            }
            changes.clear();
            // Update water amounts based on flow
            for (Vec3d pos : toUpdate) {
                WaterData posW = get(pos, waterBlocks);
                if (posW.amount <= 0) {
                    continue;
                }
                HashMap<Vec3d, Double> outflow = new HashMap();
                if (posW.flowX > 0) {
                    outflow.put(pos.add(new Vec3d(1, 0, 0)), posW.flowX);
                }
                if (posW.flowY > 0) {
                    outflow.put(pos.add(new Vec3d(0, 1, 0)), posW.flowY);
                }
                if (posW.flowZ > 0) {
                    outflow.put(pos.add(new Vec3d(0, 0, 1)), posW.flowZ);
                }
                double otherFlowX = get(pos.add(new Vec3d(-1, 0, 0)), waterBlocks).flowX;
                if (otherFlowX < 0) {
                    outflow.put(pos.add(new Vec3d(-1, 0, 0)), -otherFlowX);
                }
                double otherFlowY = get(pos.add(new Vec3d(0, -1, 0)), waterBlocks).flowY;
                if (otherFlowY < 0) {
                    outflow.put(pos.add(new Vec3d(0, -1, 0)), -otherFlowY);
                }
                double otherFlowZ = get(pos.add(new Vec3d(0, 0, -1)), waterBlocks).flowZ;
                if (otherFlowZ < 0) {
                    outflow.put(pos.add(new Vec3d(0, 0, -1)), -otherFlowZ);
                }
                double outflowSum = 0;
                for (Double d : outflow.values()) {
                    outflowSum += d;
                }
                if (outflowSum > 0) {
                    double scaleFactor = Math.min(1, posW.amount / outflowSum);
                    for (Entry<Vec3d, Double> e : outflow.entrySet()) {
                        addToMap(e.getKey(), new WaterData(e.getValue() * scaleFactor, 0, 0, 0), changes);
                    }
                    addToMap(pos, new WaterData(-outflowSum * scaleFactor, 0, 0, 0), changes);
                }
                //System.out.println(outflowSum);
            }
            // Round water levels
            for (Vec3d pos : toUpdate) {
                double total = get(pos, waterBlocks).amount + get(pos, changes).amount;
                if (total < .001) {
                    addToMap(pos, new WaterData(-total, 0, 0, 0), changes);
                } else if (total > .995) {
                    addToMap(pos, new WaterData(1 - total, 0, 0, 0), changes);
                }
            }
            // Apply changes to the actual water amounts
            for (Entry<Vec3d, WaterData> e : changes.entrySet()) {
                addToMap(e.getKey(), e.getValue(), waterBlocks);
                newToUpdate.add(e.getKey());
                for (Vec3d dir : DIRS) {
                    newToUpdate.add(e.getKey().add(dir));
                }
            }
            toUpdate = newToUpdate;
        }
    }

    public static class WaterData {

        public double amount;
        public double flowX, flowY, flowZ;

        public WaterData() {
        }

        public WaterData(double amount, double flowX, double flowY, double flowZ) {
            this.amount = amount;
            this.flowX = flowX;
            this.flowY = flowY;
            this.flowZ = flowZ;
        }

        public void add(WaterData other) {
            amount += other.amount;
            flowX += other.flowX;
            flowY += other.flowY;
            flowZ += other.flowZ;
        }

        public double flowSpeed() {
            return new Vec3d(flowX, flowY, flowZ).length();
        }

        public boolean isZero() {
            return Math.abs(amount) < 1e-4 && flowSpeed() < 1e-4;
        }

        public double maxFlowTo(WaterData other) {
            return Math.min(amount, 1 - other.amount);
        }

        public double netFlow() {
            return flowX + flowY + flowZ;
        }

        @Override
        public String toString() {
            return "WaterData{" + "amount=" + amount + ", flowX=" + flowX + ", flowY=" + flowY + ", flowZ=" + flowZ + '}';
        }
    }
}
