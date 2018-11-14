package world;

import engine.Behavior;
import graphics.Graphics;
import static graphics.VoxelRenderer.DIRS;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import util.Multithreader;
import static util.math.MathUtils.clamp;
import static util.math.MathUtils.round;
import static util.math.MathUtils.vecMap;
import util.math.Vec2d;
import util.math.Vec3d;
import util.math.Vec4d;


public class FluidManager extends Behavior {

    private static final double TICK_RATE = 10;

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

    public Vec3d spawnFluid = null;
    public HashMap<Vec3d, Fluid> fluidBlocks = new HashMap();
    public HashSet<Vec3d> toUpdate = new HashSet();
    public World world;

    private double elapsedTime;

    public void addWater(Vec3d pos, double amount) {
        addFluid(pos, new Water(), amount);
    }
    
    public void addFluid(Vec3d pos, Fluid fluid, double amount) {
        pos = pos.floor();
        synchronized (this) {
            fluidBlocks.putIfAbsent(pos, fluid);
            fluidBlocks.get(pos).amount = clamp(fluidBlocks.get(pos).amount + amount, 0, 1);
            toUpdate.add(pos);
            for (Vec3d dir : ALL_NEARBY) {
                toUpdate.add(pos.add(dir));
            }
        }
    }

    private void applyChanges(Map<Vec3d, Fluid> changes, Set<Vec3d> newToUpdate) {
        for (Map.Entry<Vec3d, Fluid> e : changes.entrySet()) {
            if  (fluidBlocks.get(e.getKey()) == null) {
                if (!e.getValue().equals(new Water())) {
                    fluidBlocks.put(e.getKey(), e.getValue());
                    newToUpdate.add(e.getKey());
                    for (Vec3d dir : ALL_NEARBY) {
                        newToUpdate.add(e.getKey().add(dir));
                    }
                }
            } else {
                if (e.getValue().equals(new Water())) {
                    fluidBlocks.remove(e.getKey());
                    newToUpdate.add(e.getKey());
                    for (Vec3d dir : ALL_NEARBY) {
                        newToUpdate.add(e.getKey().add(dir));
                    }
                } else if (!e.getValue().equals(fluidBlocks.get(e.getKey()))) {
                    fluidBlocks.put(e.getKey(), e.getValue());
                    newToUpdate.add(e.getKey());
                    for (Vec3d dir : ALL_NEARBY) {
                        newToUpdate.add(e.getKey().add(dir));
                    }
                }
            }
        }
        changes.clear();
    }

    private static Fluid get(Vec3d pos, Map<Vec3d, Fluid> map) {
        return map.getOrDefault(pos, new Water());
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
            for (Map.Entry<Vec3d, Fluid> e : fluidBlocks.entrySet()) {
                Fluid f = e.getValue();
                if (f.amount > 1e-4) {
                    f.render(this, e.getKey());
                }
            }
        }
    }

    private double renderHeight(Vec3d pos) {
        return get(pos.add(new Vec3d(0, 0, 1)), fluidBlocks).amount < 1e-4 ? get(pos, fluidBlocks).amount : 1;
    }

    @Override
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

                    HashMap<Vec3d, Fluid> changes = new HashMap();
                    HashSet newToUpdate = new HashSet();

//            Vec3d splash = Camera.camera3d.position.floor().add(new Vec3d(0, 0, -3));
//            if  fluidBlocks.containsKey(splash)) {
//                addToMap(splash, new FluidData fluidBlocks.get(splash).amount, Math.random() - .5, Math.random() - .5, Math.random() - .5), changes);
//            }
                    calculateFlowRate(changes);
                    
                    fixFlowMiscalculations(changes);
                    applyChanges(changes, newToUpdate);

                    updateFluidLevels(changes);
                    applyChanges(changes, newToUpdate);

                    spawnFluid(changes);
                    applyChanges(changes, newToUpdate);

                    toUpdate = newToUpdate;
                }
            });
        }
    }
    
    private void calculateFlowRate(HashMap<Vec3d, Fluid> changes) {
        for (Vec3d pos : toUpdate) {
            Fluid posW = get(pos, fluidBlocks);
            Fluid changeW = posW.copy();

            double flowFriction = .001;
            double averageContribution = .3;
            double changeSpeed = .3;

            for (int i = 0; i < 6; i++) {
                double actualFlow = 0;

                if (world.getBlock(pos) == null && world.getBlock(pos.add(DIRS.get(i))) == null) {
                    //TODO Defer interaction to the fluidData?
                    Fluid otherW = get(pos.add(DIRS.get(i)), fluidBlocks);
                    double smallerFlow = posW.flow[i] - clamp(posW.flow[i], -flowFriction, flowFriction);
                    double desiredFlow = mix(smallerFlow + (posW.amount - otherW.amount) * .5, DIRS.get(i).z * -10, DIRS.get(i).z == 0 ? 0 : 1);
                    double averageFlow = 0;
                    for (Vec3d v : DIRS) {
                        averageFlow += get(pos.add(v), fluidBlocks).flow[i] / 6;
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
    }
    
    private void fixFlowMiscalculations(HashMap<Vec3d, Fluid> changes) {
        for (Vec3d pos : toUpdate) {
            Fluid changeW = changes.get(pos);
            for (int i = 0; i < 6; i++) {
                Fluid otherW = changes.get(pos.add(DIRS.get(i)));
                if (otherW != null) {
                    double f = (changeW.flow[i] - otherW.flow[oppositeDir(i)]) / 2;
                    changeW.flow[i] = f;
                    otherW.flow[oppositeDir(i)] = -f;
                }
            }
        }
    }
    
    private void updateFluidLevels(HashMap<Vec3d, Fluid> changes) {
        for (Vec3d pos : toUpdate) {
           Fluid posW = get(pos, fluidBlocks);
           Fluid changeW = posW.copy();
           for (int i = 0; i < 6; i++) {
               changeW.amount -= posW.flow[i] * Math.min(posW.flowMultiplier(i), get(pos.add(DIRS.get(i)), fluidBlocks).flowMultiplier(oppositeDir(i)));
           }
           double roundSpeed = .001 / (1 + 1e3 * posW.totalFlow());
           changeW.amount += clamp((round(changeW.amount, 1 / 16.) - changeW.amount), -roundSpeed, roundSpeed);
           changes.put(pos, changeW);
       }
    }
    
    private void spawnFluid(HashMap<Vec3d, Fluid> changes) {
        if (spawnFluid != null) {
            Fluid posF = get(spawnFluid, fluidBlocks);
            Fluid changeF = posF.copy();
            changeF.amount = 1;
            changes.put(spawnFluid, changeF);
        }
    }
    
    
    
    public static abstract class Fluid {
        
        private static final double[] FLOW_BIAS = {1, 1, 1, 1, 100, 100};

        public double amount;
        public final double[] flow = new double[6];
        
        public Fluid() {}

        public Fluid(double amount) {
            this.amount = amount;
        }
        
        
        public abstract <F extends Fluid> F copy();

        @Override
        public boolean equals(Object other) {
            if (!(other instanceof Fluid)) {
                return false;
            }
            Fluid f = (Fluid) other;
            for (int i = 0; i < 6; i++) {
                if (Math.abs(flow[i] - f.flow[i]) > 1e-6) {
                    return false;
                }
            }
            return Math.abs(amount - f.amount) < 1e-6;
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
            return name() + "Data{" + "amount=" + amount + ", flow=" + Arrays.toString(flow) + '}';
        }
        
        public abstract String name();

        public double totalFlow() {
            double r = 0;
            for (int i = 0; i < 6; i++) {
                r += Math.abs(flow[i]);
            }
            return r;
        }
        
        public abstract void render(FluidManager manager, Vec3d pos);
    }
    
    
    
    public static class Water extends Fluid {
        public Water() {
            super();
        }
        
        public Water(double amount) {
            super(amount);
        }
        
        public String name() {
            return "Water";
        }
        
        public Water copy() {
            Water w = new Water(amount);
            for (int i = 0; i < 6; i++) {
                w.flow[i] = flow[i];
            }
            return w;
        }
        
        @Override
        public void render(FluidManager manager, Vec3d pos) {
            
            double totalFluid = 0;
            for (int i = 0; i > -5; i--) {
                double fluidHere = get(pos.add(new Vec3d(0, 0, i)), manager.fluidBlocks).amount;
                //TODO add check here for water vs other fluid
                if  (fluidHere > 0) {
                    totalFluid += fluidHere;
                } else {
                   break;
                }
            }
            
            double f = totalFlow();
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
}
