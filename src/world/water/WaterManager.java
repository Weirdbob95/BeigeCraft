package world.water;

import engine.Behavior;
import graphics.Graphics;
import static graphics.VoxelRenderer.DIRS;
import java.util.*;
import java.util.Map.Entry;
import static util.MathUtils.clamp;
import static util.MathUtils.round;
import static util.MathUtils.vecMap;
import util.Multithreader;
import util.vectors.Vec2d;
import util.vectors.Vec3d;
import util.vectors.Vec4d;
import world.World;

public class WaterManager extends Behavior {

    private static final double TICK_RATE = 20;

    private static final Set<Vec3d> ALL_NEARBY = new HashSet();

    static {
        ALL_NEARBY.add(new Vec3d(0, 0, 0));
        ALL_NEARBY.addAll(DIRS);
//        for (Vec3d v : DIRS) {
//            for (Vec3d v2 : DIRS) {
//                ALL_NEARBY.add(v.add(v2));
//            }
//        }
    }

    public Vec3d spawnWater = null;
    public HashMap<Vec3d, WaterData> waterBlocks = new HashMap();
    public HashSet<Vec3d> toUpdate = new HashSet();
    public World world;

    private double elapsedTime;

    public void addWater(Vec3d pos, double amount) {
        pos = pos.floor();
        synchronized (this) {
            waterBlocks.putIfAbsent(pos, new WaterData());
            waterBlocks.get(pos).amount = clamp(waterBlocks.get(pos).amount + amount, 0, 1);
            toUpdate.add(pos);
            for (Vec3d dir : ALL_NEARBY) {
                toUpdate.add(pos.add(dir));
            }
        }
    }

    private void applyChanges(Map<Vec3d, WaterData> changes, Set<Vec3d> newToUpdate) {
        for (Entry<Vec3d, WaterData> e : changes.entrySet()) {
            if (waterBlocks.get(e.getKey()) == null) {
                if (!e.getValue().equals(new WaterData())) {
                    waterBlocks.put(e.getKey(), e.getValue());
                    newToUpdate.add(e.getKey());
                    for (Vec3d dir : ALL_NEARBY) {
                        newToUpdate.add(e.getKey().add(dir));
                    }
                }
            } else {
                if (e.getValue().equals(new WaterData())) {
                    waterBlocks.remove(e.getKey());
                    newToUpdate.add(e.getKey());
                    for (Vec3d dir : ALL_NEARBY) {
                        newToUpdate.add(e.getKey().add(dir));
                    }
                } else if (!e.getValue().equals(waterBlocks.get(e.getKey()))) {
                    waterBlocks.put(e.getKey(), e.getValue());
                    newToUpdate.add(e.getKey());
                    for (Vec3d dir : ALL_NEARBY) {
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

    private static int oppositeDir(int i) {
        return i + 1 - 2 * (i % 2);
    }

    @Override
    public void render() {
        synchronized (this) {
            for (Entry<Vec3d, WaterData> e : waterBlocks.entrySet()) {
                WaterData w = e.getValue();
                if (w.amount > 1e-4) {
                    double f = e.getValue().totalFlow();
                    double totalWater = 0;
                    for (int i = 0; i > -5; i--) {
                        double waterHere = get(e.getKey().add(new Vec3d(0, 0, i)), waterBlocks).amount;
                        if (waterHere > 0) {
                            totalWater += waterHere;
                        } else {
                            break;
                        }
                    }
                    Vec4d color = new Vec4d(.2 + 5 * f, .2 + 5 * f, 1, clamp(totalWater * 5, .2, .5));
                    double height = renderHeight(e.getKey());
                    for (Vec3d v : DIRS) {
                        double otherHeight = renderHeight(e.getKey().add(v));
                        if (otherHeight < (v.z == 0 ? height : 1e-3) - 1e-4 && world.getBlock(e.getKey().add(v)) == null) {
                            if (v.z == 1) {
                                Graphics.drawRectangle3d(e.getKey().add(new Vec3d(0, 0, height)), vecMap(v, Math::abs), 0, new Vec2d(1, 1), color);
                            } else if (v.y != 0) {
                                Graphics.drawRectangle3d(e.getKey().add(vecMap(v, x -> x > 0 ? 1. : 0)).add(new Vec3d(0, 0, otherHeight)),
                                        vecMap(v, Math::abs), Math.PI, new Vec2d(1, height - otherHeight), color);
                            } else {
                                Graphics.drawRectangle3d(e.getKey().add(vecMap(v, x -> ((x > 0) ? 1. : 0))).add(new Vec3d(0, 0, otherHeight)),
                                        vecMap(v, Math::abs), 0, new Vec2d(1, height - otherHeight), color);
                            }
                        }
                    }
                }
            }
        }
    }

    private double renderHeight(Vec3d pos) {
        return get(pos.add(new Vec3d(0, 0, 1)), waterBlocks).amount < 1e-4 ? get(pos, waterBlocks).amount : 1;
    }

    public double renderLayer() {
        return 1;
    }

    @Override
    public void update(double dt) {
        elapsedTime += dt;
        while (elapsedTime > 1 / TICK_RATE) {
            elapsedTime -= 1 / TICK_RATE;

            Multithreader.run(() -> {
                synchronized (this) {

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

                        double flowFriction = .001;
                        double averageContribution = .3;
                        double changeSpeed = .3;

                        for (int i = 0; i < 6; i++) {
                            double actualFlow = 0;

                            if (world.getBlock(pos) == null && world.getBlock(pos.add(DIRS.get(i))) == null) {
                                WaterData otherW = get(pos.add(DIRS.get(i)), waterBlocks);
                                double smallerFlow = posW.flow[i] - clamp(posW.flow[i], -flowFriction, flowFriction);
                                double desiredFlow = mix(smallerFlow + (posW.amount - otherW.amount) * .5, DIRS.get(i).z * -10, DIRS.get(i).z == 0 ? 0 : 1);
                                double averageFlow = 0;
                                for (Vec3d v : DIRS) {
                                    averageFlow += get(pos.add(v), waterBlocks).flow[i] / 6;
                                }
                                desiredFlow = mix(desiredFlow, averageFlow, averageContribution);
                                actualFlow = mix(posW.flow[i], desiredFlow, changeSpeed);
                                actualFlow = clamp(actualFlow, -otherW.amount, posW.amount);
                                actualFlow = clamp(actualFlow, posW.amount - 1, 1 - otherW.amount);
                            }

                            changeW.flow[i] = actualFlow;
                        }
                        changes.put(pos, changeW);
                    }
                    // Fix flow calculation mismatches between blocks
                    for (Vec3d pos : toUpdate) {
                        WaterData changeW = changes.get(pos);
                        for (int i = 0; i < 6; i++) {
                            WaterData otherW = changes.get(pos.add(DIRS.get(i)));
                            if (otherW != null) {
                                double f = (changeW.flow[i] - otherW.flow[oppositeDir(i)]) / 2;
                                changeW.flow[i] = f;
                                otherW.flow[oppositeDir(i)] = -f;
                            }
                        }
                    }
                    applyChanges(changes, newToUpdate);

                    // Apply changes to each block's water level
                    for (Vec3d pos : toUpdate) {
                        WaterData posW = get(pos, waterBlocks);
                        WaterData changeW = posW.copy();
                        for (int i = 0; i < 6; i++) {
                            changeW.amount -= posW.flow[i] * Math.min(posW.flowMultiplier(i), get(pos.add(DIRS.get(i)), waterBlocks).flowMultiplier(oppositeDir(i)));
                        }
                        double roundSpeed = .001 / (1 + 1e3 * posW.totalFlow());
                        changeW.amount += clamp((round(changeW.amount, 1 / 16.) - changeW.amount), -roundSpeed, roundSpeed);
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
            });
        }
    }

    public static class WaterData {

        private static final double[] FLOW_BIAS = {1, 1, 1, 1, 100, 100};

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

        public double flowMultiplier(int i) {
            if (flow[i] > 0) {
                return Math.min(1, amount / outflow()) * FLOW_BIAS[i];
            } else if (flow[i] < 0) {
                return Math.min(1, (1 - amount) / inflow()) * FLOW_BIAS[i];
            } else {
                return 0;
            }
        }

        public double inflow() {
            double r = 0;
            for (int i = 0; i < 6; i++) {
                r += Math.max(-flow[i], 0) * FLOW_BIAS[i];
            }
            return r;
        }

        public double outflow() {
            double r = 0;
            for (int i = 0; i < 6; i++) {
                r += Math.max(flow[i], 0) * FLOW_BIAS[i];
            }
            return r;
        }

        @Override
        public String toString() {
            return "WaterData{" + "amount=" + amount + ", flow=" + Arrays.toString(flow) + '}';
        }

        public double totalFlow() {
            double r = 0;
            for (int i = 0; i < 6; i++) {
                r += Math.abs(flow[i]);
            }
            return r;
        }
    }
}
