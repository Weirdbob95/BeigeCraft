package game.combat;

import definitions.BlockType;
import game.ParticleBurst;
import game.abilities.Ability;
import game.abilities.WeaponChargeAbility;
import game.abilities.WeaponSwingAbility;
import game.creatures.CreatureBehavior;
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
            ParticleBurst pb = new ParticleBurst();
            pb.position.position = pos;
            pb.acceleration.acceleration = new Vec3d(0, 0, -16);
            pb.lifetime.lifetime = .2;
            pb.maxLifetime = .2;
            pb.color = new Vec4d(.1, .4, .1, 1);
            pb.spawn(10, 5, 10);
            pb.create();
        }
        for (CreatureBehavior c : new LinkedList<>(CreatureBehavior.ALL)) {
            if (c != attacker) {
                if (c.physics.containsPoint(pos)) {
                    if (!targetsHit.contains(c)) {
                        targetsHit.add(c);
                        if (wantToParryThis.contains(c)) {
                            haveParriedThis.add(c);
                            ParticleBurst pb = new ParticleBurst();
                            pb.position.position = pos;
                            pb.acceleration.acceleration = new Vec3d(0, 0, -16);
                            pb.lifetime.lifetime = .15;
                            pb.maxLifetime = .15;
                            pb.color = new Vec4d(.7, .7, .7, 1);
                            pb.spawn(10, 5, 10);
                            pb.create();
                        } else {
                            c.damage(damage, knockback);
                            ParticleBurst pb = new ParticleBurst();
                            pb.position.position = pos;
                            pb.acceleration.acceleration = new Vec3d(0, 0, -16);
                            pb.lifetime.lifetime = .15;
                            pb.maxLifetime = .15;
                            pb.color = new Vec4d(.8, .1, .1, 1);
                            pb.spawn(10, 5, 10);
                            pb.create();
                        }
                    }
                }
            }
        }
    }
}
