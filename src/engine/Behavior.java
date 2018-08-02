package engine;

import static engine.Core.MAIN_THREAD;
import static engine.Core.onMainThread;
import java.util.*;

public abstract class Behavior {

    private static final Map<Class<? extends Behavior>, Collection<Behavior>> TRACKED_BEHAVIORS = new HashMap();
    private static final SortedSet<Behavior> RENDER_ORDER = new TreeSet(Comparator.comparingDouble(Behavior::renderLayer).thenComparingInt(b -> b.id));
    private static final SortedSet<Behavior> UPDATE_ORDER = new TreeSet(Comparator.comparingDouble(Behavior::updateLayer).thenComparingInt(b -> b.id));

    private static int maxID;
    private int id = maxID++;

    private static Behavior currentRoot;
    private final Behavior root;
    private final Map<Class<? extends Behavior>, Behavior> subBehaviors;

    public Behavior() {
        if (currentRoot == null) {
            // This is a root behavior
            root = this;
            subBehaviors = new HashMap();
            subBehaviors.put(getClass(), this);
        } else {
            // This is not a root behavior
            root = currentRoot;
            subBehaviors = null;
            if (root.subBehaviors.put(getClass(), this) != null) {
                throw new RuntimeException("A behavior can only have one subbehavior of each type");
            }
        }
    }

    // Utility functions
    public final Behavior create() {
        if (Thread.currentThread() != MAIN_THREAD) {
            onMainThread(() -> create());
            return this;
        }
        if (!isRoot()) {
            throw new RuntimeException("Can only create root behaviors");
        }
        for (Behavior b : subBehaviors.values()) {
            b.createActual();
        }
        return this;
    }

    private void createActual() {
        if (TRACKED_BEHAVIORS.containsKey(getClass())) {
            TRACKED_BEHAVIORS.get(getClass()).add(this);
        }
        RENDER_ORDER.add(this);
        UPDATE_ORDER.add(this);
        createInner();
    }

    public final void destroy() {
        if (Thread.currentThread() != MAIN_THREAD) {
            onMainThread(() -> destroy());
            return;
        }
        if (!isRoot()) {
            throw new RuntimeException("Can only destroy root behaviors");
        }
        for (Behavior b : subBehaviors.values()) {
            b.destroyActual();
        }
    }

    private void destroyActual() {
        if (TRACKED_BEHAVIORS.containsKey(getClass())) {
            TRACKED_BEHAVIORS.get(getClass()).remove(this);
        }
        RENDER_ORDER.remove(this);
        UPDATE_ORDER.remove(this);
        destroyInner();
    }

    public final <T extends Behavior> T get(Class<T> c) {
        T t = getOrNull(c);
        if (t != null) {
            return t;
        } else {
            throw new RuntimeException("Behavior not found: " + c.getSimpleName());
        }
    }

    public static Collection<Behavior> getAllRenderOrder() {
        return new LinkedList<>(RENDER_ORDER);
    }

    public static Collection<Behavior> getAllUpdateOrder() {
        return new LinkedList<>(UPDATE_ORDER);
    }

    public final <T extends Behavior> T getOrNull(Class<T> c) {
        return (T) root.subBehaviors.get(c);
    }

    public final Behavior getRoot() {
        return root;
    }

    public final Set<Class<? extends Behavior>> getSubBehaviors() {
        if (!isRoot()) {
            throw new RuntimeException("Can only get subbehaviors of root behaviors");
        }
        return subBehaviors.keySet();
    }

    public final boolean isRoot() {
        return this == root;
    }

    public final <T extends Behavior> T require(Class<T> c) {
        if (!isRoot()) {
            return root.require(c);
        }
        // Check if the behavior already exists
        try {
            return get(c);
        } catch (RuntimeException e) {
        }
        // Instantiate a new behavior
        try {
            currentRoot = this;
            T r = c.newInstance();
            currentRoot = null;
            return r;
        } catch (InstantiationException | IllegalAccessException ex) {
            throw new RuntimeException("Behavior does not have an empty public constructor: " + c.getSimpleName());
        }
    }

    public static <T extends Behavior> Collection<T> track(Class<T> c) {
        if (!TRACKED_BEHAVIORS.containsKey(c)) {
            TRACKED_BEHAVIORS.put(c, new HashSet());
        }
        return (Collection) TRACKED_BEHAVIORS.get(c);
    }

    // Overridable functions
    public void createInner() {
    }

    public void destroyInner() {
    }

    public void render() {
    }

    public double renderLayer() {
        return 0;
    }

    public void update(double dt) {
    }

    public double updateLayer() {
        return 0;
    }
}
