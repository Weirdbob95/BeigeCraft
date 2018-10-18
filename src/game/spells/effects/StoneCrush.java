/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.spells.effects;

import definitions.BlockType;
import game.spells.SpellInfo;
import game.spells.SpellPart.SpellEffect;
import game.spells.TypeDefinitions;
import static game.spells.TypeDefinitions.SpellElement.STONE;

/**
 *
 * @author nikolas
 */
public class StoneCrush extends SpellEffect {

    @Override
    public void cast(SpellInfo info) {
        if (info.target.targetsCreature()) {
            //TODO
            
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
        return STONE;
    }
}

