package game.creatures;

import engine.Behavior;
import game.abilities.AbilityController;
import game.abilities.Stun;
import game.archetypes.KnightFastAttack;
import game.archetypes.KnightPrepareAttack;
import game.archetypes.KnightSlowAttack;
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

    @Override
    public void createInner() {
        monster.model.model = Model.load("skeleton_armless.vox");
        monster.jumpChance = 0;
        monster.setHitboxFromModel();
        heldItemController.eye.setEyeHeight(1.4);
    }

    @Override
    public void render() {
        if (abilityController.currentAbility() instanceof KnightPrepareAttack) {
            Sprite.load("item_sword.png").drawBillboard(monster.position.position.add(new Vec3d(0, 0, 2.5)), new Vec2d(1, 1), new Vec4d(.8, .8, .2, 1));
        }
        if (abilityController.currentAbility() instanceof KnightFastAttack
                || abilityController.currentAbility() instanceof KnightSlowAttack) {
            Sprite.load("item_sword.png").drawBillboard(monster.position.position.add(new Vec3d(0, 0, 2.5)), new Vec2d(1, 1), new Vec4d(.9, .4, .2, 1));
        }
        if (abilityController.currentAbility() instanceof Stun) {
            Sprite.load("swirl.png").drawBillboard(monster.position.position.add(new Vec3d(0, 0, 2.5)), new Vec2d(1, 1), new Vec4d(.8, .8, .2, 1));
        }
    }

    @Override
    public void update(double dt) {
        if (monster.goal != null) {
            heldItemController.eye.lookAt(monster.goal);
            if (Math.random() < 4 * dt) {
                if (abilityController.currentAbility() instanceof KnightPrepareAttack) {
                    abilityController.finishAbility();
                } else {
                    abilityController.tryAbility(new KnightPrepareAttack(this));
                }
            }
        }
    }
}
