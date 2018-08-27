package world.water;

import engine.Behavior;
import graphics.Graphics;
import static graphics.VoxelRenderer.DIRS;
import java.util.Map.Entry;
import java.util.*;
import static util.MathUtils.clamp;
import static util.MathUtils.round;
import util.vectors.Vec2d;
import util.vectors.Vec3d;
import util.vectors.Vec4d;
import world.World;

public class WaterManager extends Behavior {

    private static final double TICK_RATE = 20;

    public Vec3d spawnWater = null;
    public HashMap<Vec3d, WaterData> waterBlocks = new HashMap();
    public HashSet<Vec3d> toUpdate = new HashSet();
    public World world;

    private double elapsedTime;

    private void applyChanges(Map<Vec3d, WaterData> changes, Set<Vec3d> newToUpdate) {
        for (Entry<Vec3d, WaterData> e : changes.entrySet()) {
            if (waterBlocks.get(e.getKey()) == null) {
                if (!e.getValue().equals(new WaterData())) {
                    waterBlocks.put(e.getKey(), e.getValue());
                    newToUpdate.add(e.getKey());
                    for (Vec3d dir : DIRS) {
                        newToUpdate.add(e.getKey().add(dir));
                    }
                }
            } else {
                if (e.getValue().equals(new WaterData())) {
                    waterBlocks.remove(e.getKey());
                    newToUpdate.add(e.getKey());
                    for (Vec3d dir : DIRS) {
                        newToUpdate.add(e.getKey().add(dir));
                    }
                } else if (!e.getValue().equals(waterBlocks.get(e.getKey()))) {
                    waterBlocks.put(e.getKey(), e.getValue());
                    newToUpdate.add(e.getKey());
                    for (Vec3d dir : DIRS) {
                        newToUpdate.add(e.getKey().add(dir));
                    }
                }
            }
        }
        changes.clear();
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
                double f = e.getValue().totalFlow();
                if (toUpdate.contains(e.getKey())) {
                    Graphics.drawRectangle3d(e.getKey().add(new Vec3d(0, 0, e.getValue().amount)), new Vec3d(0, 0, 1), 0, new Vec2d(1, 1), new Vec4d(.2 + 10 * f, .2 + 10 * f, 1, 1));
                } else {
                    Graphics.drawRectangle3d(e.getKey().add(new Vec3d(0, 0, e.getValue().amount)), new Vec3d(0, 0, 1), 0, new Vec2d(1, 1), new Vec4d(1, w.totalFlow() * 1e5, 1, 1));
                }
                sum += w.amount;
            }
        }
        System.out.printf("%.8f\n", sum);
    }

    @Override
    public void update(double dt) {
        elapsedTime += dt;
        while (elapsedTime > 1 / TICK_RATE) {
            elapsedTime -= 1 / TICK_RATE;

            HashMap<Vec3d, WaterData> changes = new HashMap();
            HashSet newToUpdate = new HashSet();

//            Vec3d splash = Camera.camera3d.position.floor().add(new Vec3d(0, 0, -3));
//            if (waterBlocks.containsKey(splash)) {
//                addToMap(splash, new WaterData(waterBlocks.get(splash).amount, Math.random() - .5, Math.random() - .5, Math.random() - .5), changes);
//            }
            // Calculate the new flow rate
            for (Vec3d pos : toUpdate) {
                WaterData posW = get(pos, waterBlocks);
                WaterData changeW = posW.copy();

                double flowFriction = .01;
                double averageContribution = .3;
                double changeSpeed = .3;

                for (int i = 0; i < 6; i++) {
                    WaterData otherW = get(pos.add(DIRS.get(i)), waterBlocks);
                    double smallerFlow = posW.flow[i] - clamp(posW.flow[i], -flowFriction, flowFriction);
                    double desiredFlow = smallerFlow + (posW.amount - otherW.amount) * .5 + DIRS.get(i).dot(new Vec3d(0, 0, -10));
                    double averageFlow = 0;
                    for (int j = 0; j < 6; j++) {
                        averageFlow += get(pos.add(DIRS.get(j)), waterBlocks).flow[i] / 6;
                    }
                    desiredFlow = mix(desiredFlow, averageFlow, averageContribution);
                    double actualFlow = mix(posW.flow[i], desiredFlow, changeSpeed);
                    if (world.getBlock(pos) != null || world.getBlock(pos.add(DIRS.get(i))) != null) {
                        actualFlow = 0;
                    }
                    actualFlow = clamp(actualFlow, -otherW.amount, posW.amount);
                    actualFlow = clamp(actualFlow, posW.amount - 1, 1 - otherW.amount);
                    changeW.flow[i] = actualFlow;
                }

                changes.put(pos, changeW);
            }
            applyChanges(changes, newToUpdate);

            // Update water amounts based on flow
            for (Vec3d pos : toUpdate) {
                WaterData posW = get(pos, waterBlocks);
                WaterData changeW = posW.copy();
                for (int i = 4; i < 6; i++) {
                    changeW.amount -= posW.flow[i] / 2;
                }
                changes.put(pos, changeW);
            }
            applyChanges(changes, newToUpdate);
            // Update water amounts based on flow
            for (Vec3d pos : toUpdate) {
                WaterData posW = get(pos, waterBlocks);
                WaterData changeW = posW.copy();
                for (int i = 0; i < 4; i++) {
                    changeW.amount -= posW.flow[i] / 4;
                }
                changeW.amount += clamp((round(changeW.amount, 1 / 16.) - changeW.amount), -.0001, .0001);
                changes.put(pos, changeW);
            }
            applyChanges(changes, newToUpdate);

            // Spawn water
            if (spawnWater != null) {
                WaterData posW = get(spawnWater, waterBlocks);
                WaterData changeW = posW.copy();
                changeW.amount = 1;
                changes.put(spawnWater, changeW);
            }
            applyChanges(changes, newToUpdate);

            toUpdate = newToUpdate;
        }
    }

    public static class WaterData {

        public double amount;
        public final double[] flow = new double[6];

        public WaterData() {
        }

        public WaterData(double amount) {
            this.amount = amount;
        }

        public WaterData copy() {
            WaterData w = new WaterData(amount);
            for (int i = 0; i < 6; i++) {
                w.flow[i] = flow[i];
            }
            return w;
        }

        @Override
        public boolean equals(Object other) {
            if (!(other instanceof WaterData)) {
                return false;
            }
            WaterData w = (WaterData) other;
            for (int i = 0; i < 6; i++) {
                if (Math.abs(flow[i] - w.flow[i]) > 1e-6) {
                    return false;
                }
            }
            return Math.abs(amount - w.amount) < 1e-6;
        }

        public double inflow() {
            double r = 0;
            for (double f : flow) {
                r += Math.max(-f, 0);
            }
            return r;
        }

        public double outflow() {
            double r = 0;
            for (double f : flow) {
                r += Math.max(f, 0);
            }
            return r;
        }

        @Override
        public String toString() {
            return "WaterData{" + "amount=" + amount + ", flow=" + Arrays.toString(flow) + '}';
        }

        public double totalFlow() {
            return inflow() + outflow();
        }
    }
}
