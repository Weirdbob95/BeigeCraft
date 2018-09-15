package game.abilities;

import static definitions.Loader.getBlock;
import engine.Behavior;
import game.combat.WeaponAttack;
import game.items.HeldItemController;

public class WeaponChargeAbility extends Ability {

    public HeldItemController heldItemController = user.get(HeldItemController.class);

    public WeaponAttack weaponAttack;

    public WeaponChargeAbility(Behavior user) {
        super(user);
    }

    @Override
    public Ability attemptTransitionTo(Ability nextAbility) {
        if (nextAbility == DO_NOTHING) {
            return new WeaponSwingAbility(user, weaponAttack);
        } else {
            return super.attemptTransitionTo(nextAbility);
        }
    }

    @Override
    public void onStartUse() {
        heldItemController.reorientSpeed = .1;

        weaponAttack = new WeaponAttack();
        weaponAttack.attacker = heldItemController.creature;
        weaponAttack.damage = 2;
        weaponAttack.blocksToBreak.add(getBlock("leaves"));
    }

    @Override
    public void onEndUse() {
        heldItemController.reorientSpeed = .5;
    }
}
