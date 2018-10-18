/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.spells.effects;

import game.combat.statuses.Healing;
import game.spells.SpellInfo;
import game.spells.SpellPart;
import game.spells.TypeDefinitions;
import static game.spells.TypeDefinitions.SpellElement.LIFE;

/**
 *
 * @author nikolas
 */
public class LifeGrow extends SpellPart.SpellEffect {
    
    private static final double TIME = 10.0;
    
    @Override
    public void cast(SpellInfo info) {
        if (info.target.targetsCreature()) {
            new Healing(info.target.creature, TIME, info.powerMultiplier).start();
            hit(info);
        }
        if (info.target.targetsTerrain()) {
            //TODO
            //info.target.terrain spawn vegetation
            hit(info);
        }
        if (info.target.targetsItem()) {
            //TODO
            //info.target.item spawn vegetation
            hit(info);
        }
    }

    @Override
    public TypeDefinitions.SpellElement element() {
        return LIFE;
    }
    
}
