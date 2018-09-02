package game;

import behaviors.AccelerationBehavior;
import behaviors.PlayerPhysicsBehavior;
import behaviors.PositionBehavior;
import behaviors.SpaceOccupierBehavior;
import behaviors.VelocityBehavior;
import engine.Behavior;
import engine.Input;
import game.creatures.Creature;
import game.items.BlockItem;
import game.items.Item;
import game.items.ItemSlot;
import game.items.PickaxeItem;
import game.items.SwordItem;
import game.items.WandItem;
import game.spells.SpellInfo;
import game.spells.SpellInfo.SpellTarget;
import game.spells.TypeDefinitions.SpellEffectFinal;
import static game.spells.TypeDefinitions.SpellEffectType.DESTRUCTION;
import static game.spells.TypeDefinitions.SpellElement.FIRE;
import game.spells.TypeDefinitions.SpellShapeInitial;
import game.spells.shapes.S_Burst;
import game.spells.shapes.S_Projectile;
import game.spells.shapes.SpellShapeMissile;
import graphics.Animation;
import graphics.Sprite;
import static graphics.VoxelRenderer.DIRS;
import gui.GUIManager;
import static java.lang.Math.round;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import opengl.Camera;
import static opengl.Camera.camera3d;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_A;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_C;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_D;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT_CONTROL;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT_SHIFT;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_S;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_SPACE;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_W;
import util.MathUtils;
import static util.MathUtils.ceil;
import static util.MathUtils.mod;
import static util.MathUtils.vecMap;
import util.vectors.Vec2d;
import util.vectors.Vec3d;
import util.vectors.Vec4d;
import world.Raycast.RaycastHit;
import static world.Raycast.raycastDistance;
import static world.World.CHUNK_SIZE;

public class Player extends Behavior {

    private static final double PLAYER_SCALE = 2;

    public final PositionBehavior position = require(PositionBehavior.class);
    public final VelocityBehavior velocity = require(VelocityBehavior.class);
    public final AccelerationBehavior acceleration = require(AccelerationBehavior.class);
    public final PlayerPhysicsBehavior physics = require(PlayerPhysicsBehavior.class);
    public final SpaceOccupierBehavior spaceOccupier = require(SpaceOccupierBehavior.class);
    //public final Creature creature = require(Creature.class);

    public GUIManager gui;
    public boolean flying;
    public boolean breakingBlocks;
    public Map<Vec3d, Double> blocksToBreak = new HashMap();

    @Override
    public void createInner() {
        acceleration.acceleration = new Vec3d(0, 0, -32).mul(PLAYER_SCALE);
        physics.hitboxSize1Stand = new Vec3d(.3, .3, .9).mul(PLAYER_SCALE);
        physics.hitboxSize2Stand = new Vec3d(.3, .3, .9).mul(PLAYER_SCALE);
        physics.hitboxSize1Crouch = new Vec3d(.3, .3, .9).mul(PLAYER_SCALE);
        physics.hitboxSize2Crouch = new Vec3d(.3, .3, .5).mul(PLAYER_SCALE);
    }

    private RaycastHit firstSolid() {
        List<RaycastHit> raycast = raycastDistance(Camera.camera3d.position, Camera.camera3d.facing(), 4 * PLAYER_SCALE);
        for (int i = 0; i < raycast.size(); i++) {
            if (physics.world.getBlock(raycast.get(i).hitPos) != null) {
                return raycast.get(i);
            }
        }
        return null;
    }

    private RaycastHit lastEmpty() {
        List<RaycastHit> raycast = raycastDistance(Camera.camera3d.position, Camera.camera3d.facing(), 4 * PLAYER_SCALE);
        for (int i = 0; i < raycast.size() - 1; i++) {
            if (physics.world.getBlock(raycast.get(i).hitPos) != null) {
                return null;
            }
            if (physics.world.getBlock(raycast.get(i + 1).hitPos) != null) {
                return raycast.get(i);
            }
        }
        return null;
    }

    @Override
    public void render() {
        if (breakingBlocks) {
            Animation blockBreak = Animation.load("blockbreak_anim");
            for (Entry<Vec3d, Double> e : blocksToBreak.entrySet()) {
                if (e.getValue() > .05) {
                    Sprite s = blockBreak.getSpriteOrNull("", (int) (blockBreak.length * (e.getValue() - .05)));
                    for (Vec3d dir : DIRS) {
                        Vec3d drawPos = e.getKey().floor().add(new Vec3d(.5, .5, .5)).sub(dir.mul(.501));
                        s.draw3d(drawPos, dir, 0, new Vec2d(1, 1), new Vec4d(1, 1, 1, .5));
                    }
                }
            }
        } else {
            blocksToBreak.clear();
        }
    }

    @Override
    public void update(double dt) {

//        if (dt > 1 / 20.) {
//            System.out.println(dt);
//        }
        breakingBlocks = false;

        gui.hud.setBiome(physics.world.heightmappedChunks
                .get(physics.world.getChunkPos(position.position)).biomemap[(int) mod(position.position.x, CHUNK_SIZE)][(int) mod(position.position.y, CHUNK_SIZE)].plurality());
        gui.hud.setPos(position.position);

        Vec3d desCamPos = position.position.add(new Vec3d(0, 0, physics.crouch ? .4 : .7).mul(PLAYER_SCALE));
        //camera.position = desCamPos;
        camera3d.position = camera3d.position.lerp(desCamPos, 1 - Math.pow(1e-6, dt));

        Vec3d idealVel = new Vec3d(0, 0, 0);

        if (!gui.freezeMouse()) {
            // Look around
            camera3d.horAngle -= Input.mouseDelta().x / 300;
            camera3d.vertAngle += Input.mouseDelta().y / 300;

            if (camera3d.vertAngle > 1.55) {
                camera3d.vertAngle = 1.55f;
            }
            if (camera3d.vertAngle < -1.55) {
                camera3d.vertAngle = -1.55f;
            }
        }

        if (!gui.freezeMovement()) {
            // Move
            if (Input.keyJustPressed(GLFW_KEY_LEFT_CONTROL)) {
                flying = !flying;
            }
            double speed = (flying ? 100 : physics.crouch ? 2 : 4) * PLAYER_SCALE;

            Vec3d forwards = camera3d.facing();
            if (!flying) {
                forwards = forwards.setZ(0).normalize();
            }
            Vec3d sideways = camera3d.up.cross(forwards);

            if (Input.keyDown(GLFW_KEY_W)) {
                idealVel = idealVel.add(forwards);
            }
            if (Input.keyDown(GLFW_KEY_A)) {
                idealVel = idealVel.add(sideways);
            }
            if (Input.keyDown(GLFW_KEY_S)) {
                idealVel = idealVel.sub(forwards);
            }
            if (Input.keyDown(GLFW_KEY_D)) {
                idealVel = idealVel.sub(sideways);
            }
            if (idealVel.lengthSquared() > 0) {
                idealVel = idealVel.normalize().mul(speed);
            }

            if (!flying) {
                idealVel = idealVel.setZ(velocity.velocity.z);
            }

            // Jump
            if (Input.keyDown(GLFW_KEY_SPACE)) {
                if (physics.onGround || flying) {
                    velocity.velocity = velocity.velocity.setZ((flying ? 100 : 12) * PLAYER_SCALE);
                }
            }

            // Crouch
            physics.shouldCrouch = Input.keyDown(GLFW_KEY_LEFT_SHIFT);
        }

        if (!gui.freezeMouse()) {
            // Use items
            Item mainItem = ItemSlot.MAIN_HAND == null ? null : ItemSlot.MAIN_HAND.item();
            if (Input.mouseJustPressed(0)) {
                useItemPress(mainItem, true);
            }
            if (Input.mouseDown(0)) {
                useItemHold(mainItem, true, dt);
            }
            Item offItem = ItemSlot.OFF_HAND == null ? null : ItemSlot.OFF_HAND.item();
            if (Input.mouseJustPressed(1)) {
                useItemPress(offItem, false);
            }
            if (Input.mouseDown(1)) {
                useItemHold(offItem, false, dt);
            }
        }

        if (Input.keyJustPressed(GLFW_KEY_C)) {
            RaycastHit block = firstSolid();
            if (block != null) {
                Chest c = new Chest();
                c.model.position.position = vecMap(block.hitPos, x -> (double) round(x)).sub(block.hitDir);
                c.create();
            }
        }

        velocity.velocity = velocity.velocity.lerp(idealVel, 1 - Math.pow(1e-4, dt));
    }

    private void useItemHold(Item i, boolean isMainHand, double dt) {
        if (i == null || i instanceof PickaxeItem) {
            // Break block
            breakingBlocks = true;
            RaycastHit block = firstSolid();
            if (block != null) {
                int handSize = ceil(PLAYER_SCALE);
                Vec3d origin = block.hitPos
                        //.add(block.hitDir.mul(handSize / 2.))
                        .sub(new Vec3d(1, 1, 1).mul(.5 * (handSize - 1)));
                List<Vec3d> targets = new ArrayList();
                for (int x = 0; x < handSize; x++) {
                    for (int y = 0; y < handSize; y++) {
                        for (int z = 0; z < handSize; z++) {
                            if (physics.world.getBlock(origin.add(new Vec3d(x, y, z))) != null) {
                                targets.add(origin.add(new Vec3d(x, y, z)).floor());
                            }
                        }
                    }
                }
                for (Vec3d v : new ArrayList<>(blocksToBreak.keySet())) {
                    if (!targets.contains(v)) {
                        blocksToBreak.remove(v);
                    }
                }
                for (Vec3d v : targets) {
                    blocksToBreak.putIfAbsent(v, 0.);
                    blocksToBreak.put(v, blocksToBreak.get(v) + dt * 8 / targets.size());
                    if (blocksToBreak.get(v) > 1) {
                        ItemSlot.addToInventory(new BlockItem(physics.world.getBlock(v)));
                        physics.world.setBlock(v, null);
                        blocksToBreak.remove(v);
                    }
                }
            } else {
                blocksToBreak.clear();
            }
        }
    }

    private void useItemPress(Item i, boolean isMainHand) {
        if (i instanceof BlockItem) {
            RaycastHit block = lastEmpty();
            if (block != null) {
                (isMainHand ? ItemSlot.MAIN_HAND : ItemSlot.OFF_HAND).removeItem();
                physics.world.setBlock(block.hitPos, ((BlockItem) i).blockType);
            }
        } else if (i instanceof SwordItem) {
            new ArrayList<>(Creature.ALL).forEach(c -> {
                Vec3d delta = c.position.position.sub(position.position);
                if (delta.length() < 5 && delta.normalize().dot(Camera.camera3d.facing()) > .8) {
                    c.damage(5, Camera.camera3d.facing());
                }
            });
        } else if (i instanceof WandItem) {
            SpellShapeMissile missile = new S_Projectile();
            missile.isMultishot = true;
            SpellShapeInitial shape = missile.onHit(new S_Burst().onHit(new SpellEffectFinal()));
            SpellInfo info = new SpellInfo(new SpellTarget(Camera.camera3d.position.add(MathUtils.randomInSphere().mul(.1))), Camera.camera3d.facing(), 1, FIRE, DESTRUCTION, physics.world);
            shape.cast(info, Camera.camera3d.facing());
        }
    }
}
