package game.items;

import game.Player;
import game.spells.SpellInfo;
import game.spells.SpellInfo.SpellTarget;
import static game.spells.TypeDefinitions.SpellEffectType.*;
import static game.spells.TypeDefinitions.SpellElement.*;
import game.spells.TypeDefinitions.SpellShapeInitial;
import game.spells.effects.FireIgnite;
import game.spells.shapes.S_Burst;
import game.spells.shapes.S_Projectile;
import game.spells.shapes.SpellShapeMissile;
import opengl.Camera;
import util.MathUtils;
import util.vectors.Vec2d;

public class WandItem extends Item {

    @Override
    public String description() {
        return "This item can cast powerful magical spells.";
    }

    @Override
    public int maxStackSize() {
        return 1;
    }

    @Override
    public String name() {
        return "Wand";
    }

    @Override
    public void renderGUI(Vec2d pos) {
        renderSprite("item_wand.png", pos);
    }
       public void useItemPress(Player player, boolean isMainHand) {
            SpellShapeMissile missile = new S_Projectile();
            missile.isMultishot = true;
            SpellShapeInitial shape = missile.onHit(new S_Burst().onHit(new FireIgnite()));
            SpellInfo info = new SpellInfo(new SpellTarget(Camera.camera3d.position.add(MathUtils.randomInSphere().mul(.1))), Camera.camera3d.facing(), 1, FIRE, IGNITE, player.physics.world);
            shape.cast(info, Camera.camera3d.facing());
        }
    }

