package behaviors;

import engine.Behavior;
import java.util.LinkedList;
import java.util.Queue;
import java.util.function.Consumer;
import opengl.Window;

public abstract class MiscBehaviors {

    public static Behavior onRender(double layer, Runnable toRun) {
        return new Behavior() {
            @Override
            public void render() {
                toRun.run();
            }

            @Override
            public double renderLayer() {
                return layer;
            }
        }.create();
    }

    public static Behavior onUpdate(double layer, Consumer<Double> toRun) {
        return new Behavior() {
            @Override
            public void update(double dt) {
                toRun.accept(dt);
            }

            @Override
            public double updateLayer() {
                return layer;
            }
        }.create();
    }

    public static class FPSBehavior extends Behavior {

        private final Queue<Double> dtList = new LinkedList();
        public double fps;
        private double timeElapsed;

        @Override
        public void update(double dt) {
            dtList.add(dt);
            if (dtList.size() > 100) {
                dtList.remove();
            }
            fps = dtList.size() / dtList.stream().mapToDouble(x -> x).sum();
            timeElapsed += dt;
            if (timeElapsed > .25) {
                timeElapsed -= .25;
                Window.window.setTitle("FPS: " + Math.round(fps));
            }
        }
    }
}
