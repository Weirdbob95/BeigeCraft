/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.spells.effects;

import definitions.BlockType;
import game.combat.Status;
import game.combat.statuses.Casting;
import game.spells.SpellInfo;
import game.spells.SpellPart.SpellEffect;
import game.spells.TypeDefinitions;
import static game.spells.TypeDefinitions.SpellElement.LIGHTNING;

/**
 *
 * @author nikolas
 */
public class LightningShock extends SpellEffect {
    
    private static final double DAMAGE_MODIFIER = 3.0;

    @Override
    public void cast(SpellInfo info) {
        if (info.target.targetsCreature()) {
            info.target.creature.damage(DAMAGE_MODIFIER * info.powerMultiplier, info.direction);
            Status status = info.target.creature.status("casting");
            Casting castStatus = (Casting) status;
            if (castStatus == null) {
                castStatus.interrupt();
            }
            hit(info);
            
        }
        if (info.target.targetsTerrain()) {
            //TODO
            BlockType block = info.world.getBlock(info.target.terrain);
            hit(info);
        }
        if (info.target.targetsItem()) {
            //TODO
            hit(info);
        }
    }
    
    @Override
    public TypeDefinitions.SpellElement element() {
        return LIGHTNING;
    }
}


