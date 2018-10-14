/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.combat.statuses;

import game.combat.Status;
import static game.combat.Status.StackMode.MAX_DURATION;
import game.creatures.CreatureBehavior;

/** Damages over time
 *
 * @author nikolas
 */
public class Hurting extends Status {
    
    private double power;
    
    public Hurting(CreatureBehavior creature, double maxTimer, double power) {
        super(creature, maxTimer);
        this.power = power;
    }

    @Override
    protected void onStart() {
    }

    @Override
    protected void onUpdate(double dt) {
        if (creature.currentHealth.get() < creature.maxHealth.get()) {
            creature.currentHealth.setBaseValue(Math.max(0,
                    creature.currentHealth.getBaseValue() - 3 * power));
        }
    }
    
    @Override
    protected void onFinish() {
        
    }
    
    @Override
    public String toString() {
        return "Hurting";
    }

    @Override
    protected StackMode stackMode() {
        return MAX_DURATION;
    }
    
}
