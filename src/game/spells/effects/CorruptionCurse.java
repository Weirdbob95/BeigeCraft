/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.spells.effects;

import definitions.BlockType;
import game.spells.SpellInfo;
import game.spells.SpellPart;
import game.spells.TypeDefinitions;
import static game.spells.TypeDefinitions.SpellElement.CORRUPTION;

/**
 *
 * @author nikolas
 */
public class CorruptionCurse extends SpellPart.SpellEffect {
    @Override
    public void cast(SpellInfo info) {
        if (info.target.targetsCreature()) {
            //TODO add debuffs
            info.target.creature.damage(3 * info.powerMultiplier, info.direction);
        }
        if (info.target.targetsTerrain()) {
            //TODO
            BlockType block = info.world.getBlock(info.target.terrain);
            //info.target.terrain adds corruption
        }
        if (info.target.targetsItem()) {
            //TODO
            //info.target.item adds corruption
        }
    }

    @Override
    public TypeDefinitions.SpellElement element() {
        return CORRUPTION;
    }
}
