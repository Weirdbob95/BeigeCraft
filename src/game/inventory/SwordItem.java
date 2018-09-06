package game.inventory;

import game.Player;
import game.creatures.CreatureBehavior;
import java.util.ArrayList;
import opengl.Camera;
import util.vectors.Vec3d;

public class SwordItem extends UsableItem {

    @Override
    public void useItemPress(Player player, boolean isMainHand) {
        new ArrayList<>(CreatureBehavior.ALL).forEach(c -> {
            Vec3d delta = c.position.position.sub(player.position.position);
            if (delta.length() < 5 && delta.normalize().dot(Camera.camera3d.facing()) > .8) {
                c.damage(5, Camera.camera3d.facing());
            }
        });
    }
}
