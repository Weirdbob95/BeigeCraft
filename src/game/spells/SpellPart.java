package game.spells;

/**
 * The SpellPart class represents any part of a spell.
 *
 * The purpose of the SpellPart class is to give spells a uniform way to cast
 * themselves and to store and activate the next part of the spell.
 */
public abstract class SpellPart {

    /**
     * The next effect of the spell, or null if there is no next effect
     */
    public SpellPart onHit;

    /**
     * Activates this effect at the given SpellInfo.
     *
     * @param info The SpellInfo that stores the spell's information
     */
    public abstract void cast(SpellInfo info);

    /**
     * Activates the next effect of the spell at the given SpellInfo.
     *
     * @param info The SpellInfo at which to activate the next effect
     */
    public void hit(SpellInfo info) {
        if (onHit != null) {
            onHit.cast(info);
        }
    }

    /**
     * The SpellEffect class represents a spell effect, a way that it actually
     * affects creatures or terrain.
     */
    public static abstract class SpellEffect extends SpellPart {

        /**
         * @return The element associated with the effect
         */
        public abstract TypeDefinitions.SpellElement element();
    }

    /**
     * The SpellShapeInitial class represents the initial shape of a spell.
     *
     * The SpellShapeInitial class is empty because it doesn't need to contain
     * any code. Instead, it is useful for type checking.
     */
    public abstract static class SpellShapeInitial extends SpellPart {
    }

    /**
     * The SpellShapeModifier class represents an intermediate shape of a spell.
     *
     * The SpellShapeModifier class is empty because it doesn't need to contain
     * any code. Instead, it is useful for type checking.
     */
    public abstract static class SpellShapeModifier extends SpellPart {
    }
}
