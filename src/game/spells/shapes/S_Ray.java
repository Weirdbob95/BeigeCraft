package game.spells.shapes;

import engine.Behavior;
import static game.GraphicsEffect.createGraphicsEffect;
import game.spells.SpellInfo;
import graphics.Graphics;
import java.util.LinkedList;
import util.math.Vec3d;

/**
 * The S_Ray class represents a Ray initial shape, as described in the
 * spellcrafting design document.
 *
 * @author rsoiffer
 */
public class S_Ray extends SpellShapeMissile {

    @Override
    public void cast(SpellInfo info) {
        spawnMissiles(info, S_RayBehavior.class, 200);
    }

    /**
     * The S_RayBehavior class represents the physical incarnation of a spell
     * ray in the game world.
     */
    public static class S_RayBehavior extends Behavior {

        private final static double PERSIST_TIME = .3;

        public final MissileBehavior missile = require(MissileBehavior.class);

        public LinkedList<Vec3d> pastPositions = new LinkedList();
        public LinkedList<Double> pastTimes = new LinkedList();
        public double currentTime;

        @Override
        public void createInner() {
            missile.lifetime.lifetime = 1;
            pastPositions.add(missile.position.position);
            pastTimes.add(0.);
        }

        @Override
        public void destroyInner() {
            createGraphicsEffect(PERSIST_TIME, t -> {
                if (currentTime + t - pastTimes.peek() > PERSIST_TIME) {
                    pastPositions.poll();
                    pastTimes.poll();
                }
                for (int i = 0; i < pastPositions.size() - 1; i++) {
                    Graphics.drawLine(pastPositions.get(i), pastPositions.get(i + 1), missile.info.color());
                }
            });
        }

        @Override
        public void render() {
            for (int i = 0; i < pastPositions.size() - 1; i++) {
                Graphics.drawLine(pastPositions.get(i), pastPositions.get(i + 1), missile.info.color());
            }
        }

        @Override
        public void update(double dt) {
            currentTime += dt;
            pastPositions.add(missile.position.position);
            pastTimes.add(currentTime);
            if (currentTime - pastTimes.peek() > PERSIST_TIME) {
                pastPositions.poll();
                pastTimes.poll();
            }
        }
    }
}
