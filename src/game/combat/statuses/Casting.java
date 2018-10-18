/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.combat.statuses;

import game.combat.Status;
import static game.combat.Status.StackMode.MAX_DURATION;
import game.creatures.CreatureBehavior;
import game.spells.SpellInfo;
import game.spells.SpellPart;

/** Casts a spell after the time is finished
 *
 * @author nikolas
 */
public class Casting extends Status {
    
    private SpellPart spell;
    
    private SpellInfo info;
    
    public Casting(CreatureBehavior creature, double maxTimer, SpellPart spell, SpellInfo info) {
        super(creature, maxTimer);
        this.spell = spell;
        this.info = info;
    }
    
    public SpellPart getCastingSpell() {
        return spell;
    }
    
    public SpellInfo getCastingInfo() {
        return info;
    }
    
    public void interrupt() {
        spell = null;
        info = null;
    }

    @Override
    protected void onStart() {
    }

    @Override
    protected void onUpdate(double dt) {
    }
    
    @Override
    protected void onFinish() {
        if (spell != null) {
            spell.cast(info);
        }
    }
    
    @Override
    public String toString() {
        return "Casting";
    }

    @Override
    protected Status.StackMode stackMode() {
        return MAX_DURATION;
    }
    
}
