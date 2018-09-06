package game.spells.shapes;

import static engine.Behavior.track;
import game.creatures.CreatureBehavior;
import game.spells.SpellInfo;
import game.spells.SpellPart.SpellShapeModifier;
import java.util.Collection;
import java.util.LinkedList;

/**
 * The S_Burst class represents a Burst shape modifier, as described in the
 * spellcrafting design document.
 *
 * @author rsoiffer
 */
public class S_Burst extends SpellShapeModifier {

    private static final Collection<CreatureBehavior> ALL_CREATURES = track(CreatureBehavior.class);

    @Override
    public void cast(SpellInfo info) {
        if (info.target.targetsCreature) {
            for (CreatureBehavior c : new LinkedList<>(ALL_CREATURES)) {
                if (c.position.position.sub(info.position()).length() < 6) {
                    hit(info.setTarget(c).multiplyPower(.5));
                }
            }
        } else {
            throw new RuntimeException("Not implemented yet");
        }
    }
}
