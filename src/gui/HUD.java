package gui;

import engine.Input;
import game.Player;
import static game.Settings.SHOW_DEBUG_HUD;
import game.items.ItemSlot;
import graphics.Graphics;
import java.util.HashSet;
import java.util.Set;
import opengl.Camera;
import util.math.MathUtils;
import static util.math.MathUtils.mod;
import util.math.Vec2d;
import util.math.Vec3d;
import util.math.Vec4d;
import static world.World.CHUNK_SIZE;
import static world.World.PROVINCE_SIZE;
import world.biomes.Biome;
import world.regions.chunks.PlannedChunk;
import world.regions.provinces.StructuredProvince;

public class HUD extends GUIItem {

    private final GUIText position;
    private final GUIText biome;
    private final GUIInventorySquare mainHand, offHand;
    private GUIHealthbar healthbar;

    private static final int MAP_SIZE = 100;
    private static final int MAP_SQUARE_SIZE = 2;
    private final Vec4d[][] map = new Vec4d[MAP_SIZE][MAP_SIZE];
    private final Set<Vec2d> villagePositions = new HashSet();

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

        for (int i = 0; i < MAP_SIZE; i++) {
            for (int j = 0; j < MAP_SIZE; j++) {
                double mapAngle = Math.PI / 2 - Camera.camera3d.horAngle;
                Vec2d centerOffset = new Vec2d(i - MAP_SIZE / 2., j - MAP_SIZE / 2.).mul(MAP_SQUARE_SIZE);
                Graphics.drawRectangle(new Vec2d(670, 320).add(MathUtils.rotate(centerOffset, mapAngle)), mapAngle, new Vec2d(MAP_SQUARE_SIZE, MAP_SQUARE_SIZE), map[i][j]);
            }
        }
        for (Vec2d v : villagePositions) {
            double mapAngle = Math.PI / 2 - Camera.camera3d.horAngle;
            Vec2d centerOffset = v.mul(MAP_SQUARE_SIZE);
            Graphics.drawCircle(new Vec2d(670, 320).add(MathUtils.rotate(centerOffset, mapAngle)), MAP_SQUARE_SIZE, new Vec4d(0, 0, 0, 1));
        }
    }

    public void update(Player player) {
        if (SHOW_DEBUG_HUD) {
            Vec3d pos = player.position.position;
            position.setText(String.format("%.2f, %.2f, %.2f", pos.x, pos.y, pos.z));
            Biome b = player.physics.world.heightmappedChunks
                    .get(player.position.position).biomemap[(int) mod(player.position.position.x,
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
        villagePositions.clear();
        for (int i = 0; i < MAP_SIZE; i++) {
            for (int j = 0; j < MAP_SIZE; j++) {
                Vec3d pos = player.position.position.add(new Vec3d(i - MAP_SIZE / 2., j - MAP_SIZE / 2., 0).mul(CHUNK_SIZE));
                Biome b = player.physics.world.getRegionMap(PlannedChunk.class).get(pos).bd.plurality();
                map[i][j] = biomeToColor(b);
                for (Vec3d v : player.physics.world.getRegionMap(StructuredProvince.class).get(pos).villagePositions) {
                    v = v.div(PROVINCE_SIZE).floor().sub(player.position.position.div(PROVINCE_SIZE).floor()).setZ(0);
                    if (Math.abs(v.x) < MAP_SIZE / 2. && Math.abs(v.y) < MAP_SIZE / 2.) {
                        villagePositions.add(new Vec2d(v.x, v.y));
                    }
                }
            }
        }
    }

    private static Vec4d biomeToColor(Biome b) {
        Vec4d color = new Vec4d(1, 0, 1, 1);
        switch (b) {
            case FOREST:
                color = new Vec4d(.2, .55, .2, 1);
                break;
            case JUNGLE:
                color = new Vec4d(0, .4, 0, 1);
                break;
            case PLAINS:
                color = new Vec4d(.3, .7, .3, 1);
                break;
            case DESERT:
                color = new Vec4d(1, 1, .3, 1);
                break;
            case COLD_DESERT:
                color = new Vec4d(1, .85, .4, 1);
                break;
            case SNOW:
                color = new Vec4d(1, 1, 1, 1);
                break;
            case ROCK:
                color = new Vec4d(.5, .5, .5, 1);
                break;
            case TUNDRA:
                color = new Vec4d(.3, .5, .4, 1);
                break;
            case TAIGA:
                color = new Vec4d(.2, .45, .2, 1);
                break;
        }
        return color;
    }
}
