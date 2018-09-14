package behaviors;

import engine.Behavior;
import static engine.Behavior.track;
import java.util.Collection;
import util.math.Vec3d;

public class SpaceOccupierBehavior extends Behavior {

    private static final Collection<SpaceOccupierBehavior> ALL_SPACE_OCCUPIERS = track(SpaceOccupierBehavior.class);

    public final PositionBehavior position = require(PositionBehavior.class);

    public double radius = 1;
    public double lightness = 2;

    @Override
    public void update(double dt) {
        for (SpaceOccupierBehavior other : ALL_SPACE_OCCUPIERS) {
            if (other != this) {
                Vec3d delta = other.position.position.sub(position.position);
                double distance = delta.length();
                if (distance < radius + other.radius) {
                    if (delta.setZ(0).length() == 0) {
                        delta = new Vec3d(Math.random() - .5, Math.random() - .5, 0).normalize();
                    } else {
                        delta = delta.setZ(0).normalize();
                    }
                    position.position = position.position.sub(delta.mul((radius + other.radius - distance) * lightness * dt));
                    other.position.position = other.position.position.add(delta.mul((radius + other.radius - distance) * other.lightness * dt));
                }
            }
        }
    }
}
