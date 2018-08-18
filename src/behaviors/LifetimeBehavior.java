package behaviors;

import engine.Behavior;

public class LifetimeBehavior extends Behavior {

    public double lifetime = 0;

    @Override
    public void update(double dt) {
        lifetime -= dt;
        if (lifetime < 0) {
            getRoot().destroy();
        }
    }
}
