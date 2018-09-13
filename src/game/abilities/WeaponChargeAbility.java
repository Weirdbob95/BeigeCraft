package game.abilities;

import game.HeldItemController;

public class WeaponChargeAbility extends Ability {

    public HeldItemController heldItemController;

    @Override
    public Ability attemptTransitionTo(Ability nextAbility) {
        if (nextAbility == DO_NOTHING) {
            return new WeaponSwingAbility();
        } else {
            return super.attemptTransitionTo(nextAbility);
        }
    }

    @Override
    public void onStartUse() {
        heldItemController = abilityController.get(HeldItemController.class);
        heldItemController.reorientSpeed = .1;
    }

    @Override
    public void onEndUse() {
        heldItemController.reorientSpeed = .5;
    }
}
