package game.items;

import game.Player;
import game.abilities.Ability;
import static game.abilities.Ability.DO_NOTHING;
import game.abilities.BlockBreakAbility;
import game.abilities.BlockPlaceAbility;
import game.abilities.LiquidPlaceAbility;
import game.abilities.ParryAbility;
import game.abilities.SpellcastAbility;
import game.abilities.WeaponChargeAbility;

public class PlayerAbilityManager {

    public static Player player;

    public static Ability primary;
    public static Ability secondary;

    private static Ability itemSlotToAbility(ItemSlot is) {
        if (is == null || is.item() == null) {
            return new WeaponChargeAbility(player);
        }
        switch (is.item().useType()) {
            case "none":
                return DO_NOTHING;
            case "tool":
                return new BlockBreakAbility(player);
            case "sword":
                return new WeaponChargeAbility(player);
            case "wand":
                return new SpellcastAbility(player);
            case "water_bucket":
                return new LiquidPlaceAbility(player);
            case "block":
                return new BlockPlaceAbility(player, ItemSlot.MAIN_HAND.item().blockType());
            default:
                throw new RuntimeException("Unknown use type: " + ItemSlot.MAIN_HAND.item().useType());
        }
    }

    public static void updateAbilities() {
        primary = itemSlotToAbility(ItemSlot.MAIN_HAND);
        secondary = itemSlotToAbility(ItemSlot.OFF_HAND);
        if (primary.getClass().equals(secondary.getClass())) {
            secondary = new ParryAbility(player);
        }
    }
}
