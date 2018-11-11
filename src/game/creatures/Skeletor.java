package game.creatures;

import engine.Behavior;
import game.Knight.FastAttack;
import static game.abilities.Ability.DO_NOTHING;
import game.abilities.AbilityController;
import game.abilities.Stun;
import game.combat.WeaponAttack;
import game.items.HeldItemController;
import graphics.Model;
import graphics.Sprite;
import java.util.Collection;
import util.math.Vec2d;
import util.math.Vec3d;
import util.math.Vec4d;

public class Skeletor extends Behavior {

    public static final Collection<Skeletor> ALL = track(Skeletor.class);

    public final MonsterBehavior monster = require(MonsterBehavior.class);
    public final HeldItemController heldItemController = require(HeldItemController.class);
    public final AbilityController abilityController = require(AbilityController.class);

    public boolean timerOn;
    public double attackTimer;

    @Override
    public void createInner() {
        monster.model.model = Model.load("skeleton_armless.vox");
        monster.jumpChance = 0;
        monster.setHitboxFromModel();
        heldItemController.eye.setEyeHeight(1.4);
    }

    @Override
    public void render() {
        WeaponAttack wa = WeaponAttack.getFromAbility(abilityController.currentAbility);
        if (wa != null && wa.isParryable) {
//            Vec4d color = !wa.wantToParryThis.isEmpty() ? new Vec4d(.5, 1, .5, 1) : new Vec4d(1, .5 + clamp(attackTimer * .5 / .4, 0, .5), .5, 1);
//            Sprite.load("item_sword.png").drawBillboard(monster.position.position.add(new Vec3d(0, 0, 2.5)), new Vec2d(1, 1), color);
        }
        if (abilityController.currentAbility instanceof Stun) {
            Sprite.load("swirl.png").drawBillboard(monster.position.position.add(new Vec3d(0, 0, 2.5)), new Vec2d(1, 1), new Vec4d(.8, .8, .2, 1));
        }
    }

    @Override
    public void update(double dt) {
        attackTimer -= dt;
        if (monster.goal != null) {
            heldItemController.eye.lookAt(monster.goal);
            if (!timerOn) {
                if (Math.random() < 2 * dt) {
                    abilityController.attemptAbility(new FastAttack(this));
                    timerOn = true;
                    attackTimer = .7;
                }
//                if (Math.random() < 4 * dt) {
//                    if (abilityController.currentAbility == DO_NOTHING) {
//                        abilityController.attemptAbility(new WeaponChargeAbility(this));
//                    } else if (abilityController.currentAbility instanceof WeaponChargeAbility) {
//                        timerOn = true;
//                        attackTimer = .4;
//                        WeaponAttack.getFromAbility(abilityController.currentAbility).isParryable = true;
//                    }
//                }
            }
            if (timerOn && attackTimer <= 0) {
                timerOn = false;
                abilityController.attemptAbility(DO_NOTHING);
            }
        }
    }
}
