/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.combat.statuses;

import engine.Property.Modifier;
import game.combat.Status;
import static game.combat.Status.StackMode.MAX_DURATION;
import game.creatures.CreatureBehavior;

/** changes creature speed for the given time.
 *
 * @author nikolas
 */
public class SpeedChanged extends Status {
    
    private Modifier speedModifier;
    
    private double modifier;
    
    public SpeedChanged(CreatureBehavior creature, double maxTimer, double change) {
        super(creature, maxTimer);
        modifier = change;
    }

    @Override
    protected void onStart() {
        speedModifier = creature.speed.addModifier(x -> x * modifier);
    }

    @Override
    protected void onUpdate(double dt) {
        
    }
    
    @Override
    protected void onFinish() {
        speedModifier.remove();
    }
    
    @Override
    public String toString() {
        return "Speed Changed";
    }

    @Override
    protected Status.StackMode stackMode() {
        return MAX_DURATION;
    }
    
}