package game.abilities;

import engine.Behavior;

public class Ability {

    public final AbilityController user;
    public double timer;
    private boolean finished;

    public Ability(Behavior user) {
        this.user = user.get(AbilityController.class);
    }

    public void finish(boolean interrupted) {
        finished = true;
    }

    public double priority() {
        return 0;
    }

    public void start() {
        if (finished) {
            throw new RuntimeException("Cannot reuse instances of Ability");
        }
    }

    public void update(double dt) {
        timer += dt;
    }

    public static abstract class ChanneledAbility extends Ability {

        private double chargeTime;

        public ChanneledAbility(Behavior user) {
            super(user);
        }

        public abstract double tickRate();

        @Override
        public void update(double dt) {
            chargeTime += dt;
            while (chargeTime > 1 / tickRate()) {
                chargeTime -= 1 / tickRate();
                use();
            }
            super.update(dt);
        }

        public abstract void use();
    }

    public static abstract class ChargedAbility extends Ability {

        public ChargedAbility(Behavior user) {
            super(user);
        }

        public abstract boolean autoUseOnMaxCharge();

        public double currentCharge() {
            return Math.max(timer, maxCharge());
        }

        @Override
        public void finish(boolean interrupted) {
            if (!interrupted && timer >= minCharge()) {
                use();
            }
            super.finish(interrupted);
        }

        public abstract double maxCharge();

        public abstract double minCharge();

        @Override
        public void update(double dt) {
            if (autoUseOnMaxCharge() && timer >= maxCharge()) {
                user.finishAbility();
            }
            super.update(dt);
        }

        public abstract void use();
    }

    public static abstract class InstantAbility extends Ability {

        public InstantAbility(Behavior user) {
            super(user);
        }

        @Override
        public void start() {
            use();
            user.finishAbility();
            super.start();
        }

        public abstract void use();
    }

    public static abstract class TimedAbility extends Ability {

        public TimedAbility(Behavior user) {
            super(user);
        }

        public abstract double duration();

        @Override
        public void update(double dt) {
            if (timer > duration()) {
                user.finishAbility();
            }
            super.update(dt);
        }
    }
}
