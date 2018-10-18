/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.spells.effects;

import definitions.BlockType;
import game.combat.statuses.SpeedChanged;
import game.spells.SpellInfo;
import game.spells.SpellPart.SpellEffect;
import game.spells.TypeDefinitions;
import static game.spells.TypeDefinitions.SpellElement.ICE;

/**
 *
 * @author nikolas
 */
public class IceChill extends SpellEffect {
    
    private static final double TIME = 10.0;

    @Override
    public void cast(SpellInfo info) {
        if (info.target.targetsCreature()) {
            info.target.creature.damage(5 * info.powerMultiplier, info.direction);
            new SpeedChanged(info.target.creature, TIME, 0.5).start();
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
        return ICE;
    }
}

