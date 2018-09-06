package game.inventory;

import game.Player;
import game.spells.SpellInfo;
import game.spells.SpellInfo.SpellTarget;
import game.spells.SpellPart.SpellShapeInitial;
import static game.spells.TypeDefinitions.constructSpell;
import game.spells.effects.*;
import game.spells.shapes.S_Burst;
import game.spells.shapes.S_Projectile;
import game.spells.shapes.SpellShapeMissile;
import opengl.Camera;

public class WandItem extends UsableItem {

    @Override
    public void useItemPress(Player player, boolean isMainHand) {
        SpellShapeMissile missile = new S_Projectile();
        missile.isMultishot = true;
        SpellShapeInitial shape = constructSpell(missile, new S_Burst(), new WindGust());
        SpellInfo info = new SpellInfo(new SpellTarget(player.creature), Camera.camera3d.facing(), 1, player.physics.world);
        shape.cast(info);
    }
}
