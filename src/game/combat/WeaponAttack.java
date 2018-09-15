package game.combat;

import definitions.BlockType;
import static game.GraphicsEffect.createGraphicsEffect;
import game.abilities.Ability;
import game.abilities.WeaponChargeAbility;
import game.abilities.WeaponSwingAbility;
import game.creatures.CreatureBehavior;
import graphics.Model;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import util.math.Vec3d;
import util.math.Vec4d;
import world.World;

public class WeaponAttack {

    public CreatureBehavior attacker = null;
    public boolean canHitAttacker = false;
    public boolean isParryable = false;
    public double damage = 0;
    public Vec3d knockback = new Vec3d(0, 0, 0);
    public Set<BlockType> blocksToBreak = new HashSet();

    public Set<CreatureBehavior> targetsHit = new HashSet();
    public Set<CreatureBehavior> wantToParryThis = new HashSet();
    public Set<CreatureBehavior> haveParriedThis = new HashSet();
    public boolean hasFinished;

    public static WeaponAttack getFromAbility(Ability a) {
        if (a instanceof WeaponChargeAbility) {
            return ((WeaponChargeAbility) a).weaponAttack;
        }
        if (a instanceof WeaponSwingAbility) {
            return ((WeaponSwingAbility) a).weaponAttack;
        }
        return null;
    }

    public void hitAtPos(Vec3d pos) {
        World world = attacker.physics.world;
        if (blocksToBreak.contains(world.getBlock(pos))) {
            world.setBlock(pos, null);
        }
        for (CreatureBehavior c : new LinkedList<>(CreatureBehavior.ALL)) {
            if (c != attacker) {
                if (c.physics.containsPoint(pos)) {
                    if (!targetsHit.contains(c)) {
                        targetsHit.add(c);
                        if (wantToParryThis.contains(c)) {
                            haveParriedThis.add(c);
                        } else {
                            c.damage(damage, knockback);
                            createGraphicsEffect(.2, t -> {
                                Model m = Model.load("fireball.vox");
                                m.render(pos, 0, 0, 1 / 16., m.size().div(2), new Vec4d(1, 1, 1, 1 - 5 * t));
                            });
                        }
                    }
                }
            }
        }
    }
}
