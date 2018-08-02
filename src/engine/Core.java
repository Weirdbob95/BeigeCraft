package engine;

import java.io.File;
import java.util.Collection;
import java.util.LinkedList;
import opengl.Window;

public abstract class Core {

    public static Thread MAIN_THREAD;

    private static long prevTime;
    private static final Collection<Runnable> TO_RUN = new LinkedList();
    private static boolean shouldClose;

    private static Collection<Runnable> clearToRun() {
        synchronized (TO_RUN) {
            Collection<Runnable> r = new LinkedList<>(TO_RUN);
            TO_RUN.clear();
            return r;
        }
    }

    public static void init() {
        System.setProperty("org.lwjgl.librarypath", new File("native").getAbsolutePath());
        MAIN_THREAD = Thread.currentThread();
        Window.initGLFW();
        Input.init();
    }

    public static void onMainThread(Runnable toRun) {
        if (toRun == null) {
            throw new RuntimeException("toRun cannot be null");
        }
        synchronized (TO_RUN) {
            TO_RUN.add(toRun);
        }
    }

    public static void run() {
        while (!shouldClose && !Window.window.shouldClose()) {
            Input.nextFrame();
            Window.window.nextFrame();

            long time = System.nanoTime();
            double dt = Math.min((time - prevTime) / 1e9, .1);
            prevTime = time;

            clearToRun().forEach(r -> r.run());
            Behavior.getAllUpdateOrder().forEach(b -> b.update(dt));
            Behavior.getAllRenderOrder().forEach(b -> b.render());
        }
        //Behavior.getAll().stream().filter(b -> b.isRoot()).forEach(b -> b.destroy());
        Window.cleanupGLFW();
        System.exit(0);
    }

    public static void stopGame() {
        shouldClose = true;
    }
}
