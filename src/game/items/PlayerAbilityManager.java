package game.items;

import definitions.WeaponType;
import game.Player;
import game.abilities.Ability;
import game.abilities.BlockBreakAbility;
import game.abilities.BlockPlaceAbility;
import game.abilities.LiquidPlaceAbility;
import game.abilities.SpellcastAbility;
import game.abilities.TerrainObjectPlaceAbility;
import game.archetypes.KnightBlock;
import game.archetypes.KnightFastAttack;

public class PlayerAbilityManager {

    public static Player player;

    public static Ability primary;
    public static Ability secondary;

    private static Ability itemSlotToAbility(ItemSlot is) {
        if (is == null || is.item() == null) {
            return new KnightFastAttack(player);
        }
        if (is.item().weapon != null) {
            return new KnightFastAttack(player);
        }
        if (is.item().tool != null) {
            return new BlockBreakAbility(player);
        }
        if (is.item().blockType != null) {
            return new BlockPlaceAbility(player, is.item().blockType);
        }
        if (is.item().terrainObjectType != null) {
            return new TerrainObjectPlaceAbility(player, is.item().terrainObjectType);
        }
        if (is.item().gameName.equals("wand")) {
            return new SpellcastAbility(player);
        }
        if (is.item().gameName.equals("waterBucket")) {
            return new LiquidPlaceAbility(player);
        }
        throw new RuntimeException("Unknown ability for item: " + is.item().gameName);
    }

    public static void updateAbilities() {
        primary = itemSlotToAbility(ItemSlot.MAIN_HAND);
        secondary = itemSlotToAbility(ItemSlot.OFF_HAND);
        if (primary.getClass().equals(secondary.getClass())) {
            secondary = new KnightBlock(player);
        }
        if (primary instanceof KnightFastAttack) {
            if (ItemSlot.MAIN_HAND != null && ItemSlot.MAIN_HAND.item() != null) {
                player.heldItemController.heldItemType = ItemSlot.MAIN_HAND.item().weapon;
            } else {
                player.heldItemController.heldItemType = WeaponType.FIST;
            }
        } else if (secondary instanceof KnightFastAttack) {
            if (ItemSlot.OFF_HAND != null && ItemSlot.OFF_HAND.item() != null) {
                player.heldItemController.heldItemType = ItemSlot.OFF_HAND.item().weapon;
            } else {
                player.heldItemController.heldItemType = WeaponType.FIST;
            }
        } else {
            player.heldItemController.heldItemType = WeaponType.FIST;
        }
    }
}
