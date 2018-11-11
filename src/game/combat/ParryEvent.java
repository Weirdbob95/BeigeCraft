package game.combat;

public class ParryEvent {

    public WeaponAttack attack;
    public double damageMultiplier;
    public double knockbackMultiplier;
    public boolean isParried;

    public ParryEvent(WeaponAttack attack) {
        this.attack = attack;
        damageMultiplier = 1;
        knockbackMultiplier = 1;
    }
}
