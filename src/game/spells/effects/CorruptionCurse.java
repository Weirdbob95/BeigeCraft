/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.spells.effects;

import definitions.BlockType;
import game.combat.Status;
import static game.combat.Status.StackMode.MAX_DURATION;
import game.combat.statuses.SpeedChanged;
import game.creatures.CreatureBehavior;
import game.spells.SpellInfo;
import game.spells.SpellPart;
import game.spells.TypeDefinitions;
import static game.spells.TypeDefinitions.SpellElement.CORRUPTION;

/**
 *
 * @author nikolas
 */
public class CorruptionCurse extends SpellPart.SpellEffect {
    
    private static final double DAMAGE_MODIFIER = 3.0;
    
    @Override
    public void cast(SpellInfo info) {
        if (info.target.targetsCreature()) {
            info.target.creature.damage(DAMAGE_MODIFIER * info.powerMultiplier, info.direction);
            new Debuff(info.target.creature, info.powerMultiplier).start();
            new SpeedChanged(info.target.creature, 10.0, 1 / info.powerMultiplier).start();
            hit(info);
        }
        if (info.target.targetsTerrain()) {
            //TODO
            BlockType block = info.world.getBlock(info.target.terrain);
            //info.target.terrain adds corruption
            hit(info);
        }
        if (info.target.targetsItem()) {
            //TODO
            //info.target.item adds corruption
            hit(info);
        }
    }

    @Override
    public TypeDefinitions.SpellElement element() {
        return CORRUPTION;
    }
    
    class Debuff extends Status {
        private static final double TIME = 10.0;
        
        private double power;
        
        public Debuff(CreatureBehavior creature, double power) {
            super(creature, TIME);
            this.power = power;
        }
        
        //TODO more debuff
        @Override
        protected void onStart() {
            
        }

        @Override
        protected void onUpdate(double dt) {
            
        }
    
        @Override
        protected void onFinish() {
        
        }
    
        @Override
        public String toString() {
           return "Debuffed";
        }
        
        @Override
        protected Status.StackMode stackMode() {
            return MAX_DURATION;
        }
    }
}
