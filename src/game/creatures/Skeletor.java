package game.creatures;

import engine.Behavior;
import game.HeldItemController;
import static game.abilities.Ability.DO_NOTHING;
import game.abilities.AbilityController;
import game.abilities.WeaponChargeAbility;
import graphics.Model;
import graphics.Sprite;
import util.vectors.Vec2d;
import util.vectors.Vec3d;
import util.vectors.Vec4d;

public class Skeletor extends Behavior {

    public final MonsterBehavior monster = require(MonsterBehavior.class);
    public final HeldItemController heldItemController = require(HeldItemController.class);
    public final AbilityController abilityController = require(AbilityController.class);

    public boolean timerOn;
    public double attackTimer;

    @Override
    public void createInner() {
        monster.model.model = Model.load("skelesmalllarge.vox");
        monster.jumpChance = 0;
        monster.setHitboxFromModel();
        heldItemController.eye.setEyeHeight(.9);
    }

    @Override
    public void render() {
        if (attackTimer > 0) {
            Sprite.load("item_sword.png").drawBillboard(monster.position.position.add(new Vec3d(0, 0, 2.5)), new Vec2d(1, 1), new Vec4d(1, .5 + attackTimer * .5 / .4, .5, 1));
        }
    }

    @Override
    public void update(double dt) {
        attackTimer -= dt;
        if (monster.goal != null) {
            heldItemController.eye.facing = monster.goal.sub(monster.position.position).normalize();
            if (!timerOn) {
                if (Math.random() < 4 * dt) {
                    if (abilityController.currentAbility == DO_NOTHING) {
                        abilityController.attemptAbility(new WeaponChargeAbility());
                    } else {
                        timerOn = true;
                        attackTimer = .4;
                    }
                }
            }
            if (timerOn && attackTimer <= 0) {
                timerOn = false;
                abilityController.attemptAbility(DO_NOTHING);
            }
        }
    }
}
