package game.abilities;

import definitions.BlockType;
import game.HeldItemController;
import game.combat.WeaponAttack;

public class WeaponChargeAbility extends Ability {

    public HeldItemController heldItemController;

    public WeaponAttack weaponAttack;

    @Override
    public Ability attemptTransitionTo(Ability nextAbility) {
        if (nextAbility == DO_NOTHING) {
            return new WeaponSwingAbility(weaponAttack);
        } else {
            return super.attemptTransitionTo(nextAbility);
        }
    }

    @Override
    public void onStartUse() {
        heldItemController = abilityController.get(HeldItemController.class);
        heldItemController.reorientSpeed = .1;

        weaponAttack = new WeaponAttack();
        weaponAttack.attacker = heldItemController.creature;
        weaponAttack.damage = 2;
        weaponAttack.blocksToBreak.add(BlockType.getBlock("leaves"));
    }

    @Override
    public void onEndUse() {
        heldItemController.reorientSpeed = .5;
    }
}
