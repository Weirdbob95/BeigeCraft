/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.combat.statuses;

import game.combat.Status;
import static game.combat.Status.StackMode.MAX_DURATION;
import game.creatures.CreatureBehavior;

/** Heals over time
 *
 * @author nikolas
 */
public class Healing extends Status {
    
    private double power;
    
    public Healing(CreatureBehavior creature, double maxTimer, double power) {
        super(creature, maxTimer);
        this.power = power;
    }

    @Override
    protected void onStart() {
    }

    @Override
    protected void onUpdate(double dt) {
        creature.heal(power);
    }
    
    @Override
    protected void onFinish() {
        
    }

    @Override
    protected StackMode stackMode() {
        return MAX_DURATION;
    }
    
}
