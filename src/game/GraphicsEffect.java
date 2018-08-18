package game;

import behaviors.LifetimeBehavior;
import engine.Behavior;
import java.util.function.Consumer;

public class GraphicsEffect extends Behavior {

    public final LifetimeBehavior lifetime = require(LifetimeBehavior.class);

    public Consumer<Double> onRender;

    public static void createGraphicsEffect(double lifetime, Runnable onRender) {
        createGraphicsEffect(lifetime, t -> onRender.run());
    }

    public static void createGraphicsEffect(double lifetime, Consumer<Double> onRender) {
        GraphicsEffect ge = new GraphicsEffect();
        ge.lifetime.lifetime = lifetime;
        ge.onRender = lt -> onRender.accept(lifetime - lt);
        ge.create();
    }

    @Override
    public void render() {
        onRender.accept(lifetime.lifetime);
    }
}
