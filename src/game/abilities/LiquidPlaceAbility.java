package game.abilities;

import engine.Behavior;
import game.Player;
import game.abilities.Ability.InstantAbility;
import world.Raycast;

public class LiquidPlaceAbility extends InstantAbility {

    public final Player player = user.get(Player.class);

    public LiquidPlaceAbility(Behavior user) {
        super(user);
    }

    @Override
    public void use() {
        Raycast.RaycastHit block = player.lastEmpty();
        if (block != null) {
            player.physics.world.waterManager.addWater(block.hitPos, 1);
        }
    }
}
