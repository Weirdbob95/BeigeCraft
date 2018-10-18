/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.spells.effects;

import game.spells.SpellInfo;
import game.spells.SpellPart;
import game.spells.TypeDefinitions;
import static game.spells.TypeDefinitions.SpellElement.LIGHT;

/**
 *
 * @author nikolas
 */
public class LightBrighten extends SpellPart.SpellEffect {
    @Override
    public void cast(SpellInfo info) {
        if (info.target.targetsCreature()) {
            //TODO
            //info.target.creature
            hit(info);
        }
        if (info.target.targetsTerrain()) {
            //TODO
            //info.target.terrain
            hit(info);
        }
        if (info.target.targetsItem()) {
            //TODO
            //info.target.item
            hit(info);
        }
    }

    @Override
    public TypeDefinitions.SpellElement element() {
        return LIGHT;
    }
}
