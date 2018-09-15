package game.abilities;

import engine.Behavior;

public class Ability {

    public static final Ability DO_NOTHING = new Ability(null);

    public final Behavior user;

    public Ability(Behavior user) {
        this.user = user;
    }

    public Ability attemptTransitionTo(Ability nextAbility) {
        return nextAbility == null ? this : nextAbility;
    }

    public void onStartUse() {
    }

    public void onContinuousUse(double dt) {
    }

    public void onEndUse() {
    }

    public static abstract class ChanneledAbility extends Ability {

        private double chargeTime;

        public ChanneledAbility(Behavior user) {
            super(user);
        }

        @Override
        public void onStartUse() {
            chargeTime = 0;
        }

        @Override
        public void onContinuousUse(double dt) {
            chargeTime += dt;
            while (chargeTime > 1 / tickRate()) {
                chargeTime -= 1 / tickRate();
                use();
            }
        }

        public abstract double tickRate();

        public abstract void use();
    }

    public static abstract class ChargedAbility extends Ability {

        public double charge;

        public ChargedAbility(Behavior user) {
            super(user);
        }

        public abstract boolean autoUseOnMaxCharge();

        @Override
        public Ability attemptTransitionTo(Ability nextAbility) {
            if (nextAbility == null) {
                return autoUseOnMaxCharge() && charge >= maxCharge() ? nextAbility : this;
            } else {
                return charge >= minCharge() ? nextAbility : this;
            }
        }

        public abstract double maxCharge();

        public abstract double minCharge();

        @Override
        public void onStartUse() {
            charge = 0;
        }

        @Override
        public void onContinuousUse(double dt) {
            charge = Math.min(charge + dt, maxCharge());
        }

        @Override
        public void onEndUse() {
            use();
        }

        public abstract void use();
    }

    public static abstract class ContinuousAbility extends Ability {

        public ContinuousAbility(Behavior user) {
            super(user);
        }

        @Override
        public void onContinuousUse(double dt) {
            use(dt);
        }

        public abstract void use(double dt);
    }

    public static abstract class InstantAbility extends Ability {

        public InstantAbility(Behavior user) {
            super(user);
        }

        @Override
        public Ability attemptTransitionTo(Ability nextAbility) {
            return nextAbility;
        }

        @Override
        public void onStartUse() {
            use();
        }

        public abstract void use();
    }

    public static abstract class TimedAbility extends Ability {

        public double timer;

        public TimedAbility(Behavior user) {
            super(user);
        }

        @Override
        public Ability attemptTransitionTo(Ability nextAbility) {
            return (nextAbility != null || timer <= 0) ? nextAbility : this;
        }

        public abstract double duration();

        @Override
        public void onStartUse() {
            timer = duration();
        }

        @Override
        public void onContinuousUse(double dt) {
            timer -= dt;
        }
    }

    public static class Wait extends ContinuousAbility {

        private double timer;

        public Wait(Behavior user, double timer) {
            super(user);
            this.timer = timer;
        }

        @Override
        public Ability attemptTransitionTo(Ability nextAbility) {
            return timer > 0 ? this : nextAbility;
        }

        @Override
        public void use(double dt) {
            timer -= dt;
        }
    }
}
