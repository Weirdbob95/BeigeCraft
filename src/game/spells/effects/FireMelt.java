/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.spells.effects;

import definitions.BlockType;
import game.combat.Status;
import static game.combat.Status.StackMode.MAX_DURATION;
import game.creatures.CreatureBehavior;
import game.spells.SpellInfo;
import game.spells.SpellPart.SpellEffect;
import game.spells.TypeDefinitions;
import static game.spells.TypeDefinitions.SpellElement.FIRE;

/**
 *
 * @author nikolas
 */
public class FireMelt extends SpellEffect {
    
    private static final double TIME = 10.0;

    @Override
    public void cast(SpellInfo info) {
        if (info.target.targetsCreature()) {
            new Melting(info.target.creature, TIME, 4 * info.powerMultiplier).start();
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
        return FIRE;
    }
    
    public class Melting extends Status {
        
        private double power;
    
        public Melting(CreatureBehavior creature, double maxTimer, double heat) {
            super(creature, maxTimer);
            this.power = heat;
        }

        @Override
        protected void onStart() {
        }

        @Override
        protected void onUpdate(double dt) {
            creature.damage(power);
            //creature.defense -= power; //TODO
        }
        
        @Override
        protected void onFinish() {
            
        }

        @Override
        protected Status.StackMode stackMode() {
            return MAX_DURATION;
        }
    
    }
}
