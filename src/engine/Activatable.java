package engine;

import java.util.List;

public interface Activatable {

    public void activate();

    public void deactivate();

    public static void using(Runnable r, Activatable... as) {
        for (Activatable a : as) {
            a.activate();
        }
        r.run();
        for (Activatable a : as) {
            a.deactivate();
        }
    }

    public static void using(List<Activatable> as, Runnable r) {
        for (Activatable a : as) {
            a.activate();
        }
        r.run();
        for (Activatable a : as) {
            a.deactivate();
        }
    }
}
