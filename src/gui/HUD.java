package gui;

import engine.Input;
import game.Player;
import static game.Settings.SHOW_DEBUG_HUD;
import game.inventory.ItemSlot;
import static util.MathUtils.mod;
import util.vectors.Vec2d;
import util.vectors.Vec3d;
import util.vectors.Vec4d;
import static world.World.CHUNK_SIZE;
import world.biomes.Biome;

public class HUD extends GUIItem {

    private final GUIText position;
    private final GUIText biome;
    private final GUIInventorySquare mainHand, offHand;
    private GUIHealthbar healthbar;

    public HUD() {
        GUISprite crosshares = new GUISprite("crosshares.png");
        crosshares.color = new Vec4d(1, 1, 1, .5);

        position = new GUIText(null);
        position.offset = new Vec2d(-790, 425);
        position.centered = false;

        biome = new GUIText(null);
        biome.offset = new Vec2d(-790, 390);
        biome.centered = false;

        mainHand = new GUIInventorySquare(ItemSlot.MAIN_HAND);
        mainHand.offset = new Vec2d(-50, -400);

        offHand = new GUIInventorySquare(ItemSlot.OFF_HAND);
        offHand.offset = new Vec2d(50, -400);

        add(crosshares, position, biome, mainHand, offHand);
    }

    @Override
    protected void render() {
        mainHand.color = new Vec4d(.6, .6, .6, Input.mouseDown(0) ? .8 : .5);
        mainHand.itemSlot = ItemSlot.MAIN_HAND;
        offHand.color = new Vec4d(.6, .6, .6, Input.mouseDown(1) ? .8 : .5);
        offHand.itemSlot = ItemSlot.OFF_HAND;
    }

    public void update(Player player) {
        if (SHOW_DEBUG_HUD) {
            Vec3d pos = player.position.position;
            position.setText(String.format("%.2f, %.2f, %.2f", pos.x, pos.y, pos.z));
            Biome b = player.physics.world.heightmappedChunks
                    .get(player.physics.world.getChunkPos(player.position.position)).biomemap[(int) mod(player.position.position.x,
                    CHUNK_SIZE)][(int) mod(player.position.position.y, CHUNK_SIZE)].plurality();
            biome.setText(b.toString());
        } else {
            position.setText(null);
            biome.setText(null);
        }
        if (healthbar == null) {
            healthbar = new GUIHealthbar(player.creature);
            healthbar.offset = new Vec2d(-550, -400);
            healthbar.size = new Vec2d(400, 50);
            add(healthbar);
        }
    }
}
