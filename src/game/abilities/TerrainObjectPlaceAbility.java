package game.abilities;

import definitions.TerrainObjectType;
import engine.Behavior;
import game.Player;
import game.abilities.Ability.InstantAbility;
import game.items.ItemSlot;
import util.math.Vec3d;
import world.Raycast.RaycastHit;

public class TerrainObjectPlaceAbility extends InstantAbility {

    public final Player player = user.get(Player.class);

    public final TerrainObjectType terrainObjectType;

    public TerrainObjectPlaceAbility(Behavior user, TerrainObjectType terrainObjectType) {
        super(user);
        this.terrainObjectType = terrainObjectType;
    }

    @Override
    public void use() {
        RaycastHit block = player.firstSolid();
        if (block != null) {
            Vec3d objCenter = block.hitPos.sub(block.hitDir.setLength(Math.abs(block.hitDir.normalize().dot(terrainObjectType.getSize())) / 2));
            if (player.physics.world.addTerrainObject(objCenter.sub(terrainObjectType.getSize().div(2)).add(.5), terrainObjectType)) {
                //(isMainHand ? ItemSlot.MAIN_HAND : ItemSlot.OFF_HAND).removeItem();
                ItemSlot.MAIN_HAND.removeItem();
            }
        }
    }
}
