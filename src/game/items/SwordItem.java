package game.items;

import game.Player;
import game.creatures.Creature;
import java.util.ArrayList;
import opengl.Camera;
import util.vectors.Vec2d;
import util.vectors.Vec3d;

public class SwordItem extends Item {

    @Override
    public String description() {
        return "This melee weapon is common and easy to use.";
    }

    @Override
    public int maxStackSize() {
        return 1;
    }

    @Override
    public String name() {
        return "Sword";
    }

    @Override
    public void renderGUI(Vec2d pos) {
        renderSprite("item_sword.png", pos);
    }
    
    @Override
       public void useItemPress(Player player, boolean isMainHand) {
            new ArrayList<>(Creature.ALL).forEach(c -> {
                Vec3d delta = c.position.position.sub(player.position.position);
                if (delta.length() < 5 && delta.normalize().dot(Camera.camera3d.facing()) > .8) {
                    c.damage(5, Camera.camera3d.facing());
                }
            });
       }
}

        
