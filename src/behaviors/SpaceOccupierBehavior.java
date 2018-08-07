package behaviors;

import engine.Behavior;
import static engine.Behavior.track;
import java.util.Collection;
import util.vectors.Vec3d;

public class SpaceOccupierBehavior extends Behavior {

    private static final Collection<SpaceOccupierBehavior> ALL_SPACE_OCCUPIERS = track(SpaceOccupierBehavior.class);

    public final PositionBehavior position = require(PositionBehavior.class);

    public double radius = .5;
    public double lightness = 10;

    @Override
    public void update(double dt) {
        for (SpaceOccupierBehavior other : ALL_SPACE_OCCUPIERS) {
            if (other != this) {
                Vec3d delta = other.position.position.sub(position.position);
                double distance = delta.length();
                if (distance < radius + other.radius) {
                    delta.normalize();
                    position.position = position.position.sub(delta.mul((radius + other.radius - distance) * lightness * dt));
                    other.position.position = other.position.position.add(delta.mul((radius + other.radius - distance) * other.lightness * dt));
                }
            }
        }
    }
}
