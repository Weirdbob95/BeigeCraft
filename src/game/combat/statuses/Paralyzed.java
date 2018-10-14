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
public class Paralyzed extends Status {
    
    public Paralyzed(CreatureBehavior creature, double maxTimer) {
        super(creature, maxTimer);
    }

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
    protected Status.StackMode stackMode() {
        return MAX_DURATION;
    }
    
}
