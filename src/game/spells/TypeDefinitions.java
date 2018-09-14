package game.spells;

import game.spells.SpellPart.SpellEffect;
import game.spells.SpellPart.SpellShapeInitial;

/**
 * The TypeDefinitions class provides a container for a number of useful
 * functions and enums related to spells.
 *
 * The TypeDefinitions class acts only as an organizational tool for other code.
 * It should never be instantiated.
 *
 * @author rsoiffer
 */
public abstract class TypeDefinitions {

    /**
     * The SpellCastingType enum represents the way a spell is cast.
     */
    public static enum SpellCastingType {

        /**
         * An instant spell activates as soon as the player presses the key.
         */
        INSTANT,
        /**
         * A charged spell starts charging while the player holds the key,
         * becoming more powerful over time. The spell is cast when the player
         * releases the key.
         */
        CHARGED,
        /**
         * A channeled spell is cast continuously while the player holds the
         * key.
         */
        CHANNELED
    }

    /**
     * The SpellElement enum represents the element associated with a
     * SpellEffect.
     */
    public static enum SpellElement {

        FIRE,
        ICE,
        WIND,
        STONE,
        LIGHTNING,
        LIFE,
        CORRUPTION,
        SPACE,
        LIGHT,
        FORCE
    }

    /**
     * Constructs a spell with the given initial shape and the given parts.
     *
     * This method is primarily intended as a helper method to make it easier to
     * test out particular spell constructions.
     *
     * @param shapeInitial The spell's initial shape
     * @param parts All of the parts in the spell
     * @return The spell's initial shape
     */
    public static SpellShapeInitial constructSpell(SpellShapeInitial shapeInitial, SpellPart... parts) {
        if (parts.length == 0 || !(parts[parts.length - 1] instanceof SpellEffect)) {
            throw new RuntimeException("The spell must end with a spell effect");
        }
        for (SpellPart part : parts) {
            if (part instanceof SpellShapeInitial) {
                throw new RuntimeException("A spell cannot have an initial shape as an intermediate effect");
            }
        }
        shapeInitial.onHit = parts[0];
        for (int i = 0; i < parts.length - 1; i++) {
            parts[i].onHit = parts[i + 1];
        }
        return shapeInitial;
    }
}
