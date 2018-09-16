package game.abilities;

import definitions.TerrainObjectType;
import engine.Behavior;
import game.Player;
import game.abilities.Ability.InstantAbility;
import game.items.ItemSlot;
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
        RaycastHit block = player.lastEmpty();
        if (block != null) {
            //(isMainHand ? ItemSlot.MAIN_HAND : ItemSlot.OFF_HAND).removeItem();
            ItemSlot.MAIN_HAND.removeItem();
            player.physics.world.addTerrainObject(block.hitPos, terrainObjectType);
        }
    }
}
