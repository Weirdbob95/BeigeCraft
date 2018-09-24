package world.structures;

import definitions.BlockType;
import static definitions.Loader.getBlock;
import static definitions.Loader.getTerrainObject;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import static util.math.MathUtils.min;
import static util.math.MathUtils.mod;
import world.TerrainObjectInstance;
import static world.World.CHUNK_SIZE;
import world.regions.chunks.StructuredChunk;

public class House extends Structure {

    private final Random random;

    public House(StructuredChunk sc, int x, int y, int z, int width, int height) {
        super(sc, x, y, z);
        priority += 10;
        random = sc.random;
        Rectangle base = new Rectangle(-width / 2, -height / 2, width, height);
        int floorHeight = 6 + random.nextInt(3);
        int numFloors = 1 + random.nextInt(3);
        for (int floor = 0; floor < numFloors; floor++) {
            int wallBottom = floor * floorHeight;
            List<Rectangle> rooms = base.recursivelySubdivide();
            for (Rectangle room : rooms) {
                room.buildWalls(wallBottom, wallBottom + floorHeight - 1, getBlock("plaster"));
            }
            for (RoomBorder rb : findRoomBorders(rooms)) {
                if (random.nextDouble() < .2) {
                    rb.buildOpening(wallBottom + 1, wallBottom + floorHeight - 1, rb.length, null);
                } else {
                    rb.buildOpening(wallBottom + 1, wallBottom + 4, 2, null);
                }
            }
            List<RoomBorder> outsideBorders = findOutsideBorders(base, rooms);
            if (floor == 0) {
                int numDoors = 1 + random.nextInt(3);
                for (int i = 0; i < numDoors; i++) {
                    RoomBorder rb = outsideBorders.get(random.nextInt(outsideBorders.size()));
                    int doorPos = rb.buildOpening(wallBottom + 1, wallBottom + 4, 2, null);
                    terrainObjects.add(new TerrainObjectInstance(getTerrainObject("door"), sc.pos,
                            x + rb.x + (rb.horizontal ? doorPos : 0), y + rb.y + (rb.horizontal ? 0 : doorPos), z + wallBottom + 1, rb.horizontal ? 0 : 1));
                }
            }
            int numWindows = 4 + random.nextInt(10);
            for (int i = 0; i < numWindows; i++) {
                RoomBorder rb = outsideBorders.get(random.nextInt(outsideBorders.size()));
                rb.buildOpening(wallBottom + 3, wallBottom + 4, 2, null);
            }
            base.buildCorners(wallBottom, wallBottom + floorHeight - 1, getBlock("log"));
            base.buildFloor(floor * floorHeight, getBlock("planks"));
            if (floor > 0) {
                base.buildWalls(floor * floorHeight, floor * floorHeight, getBlock("log"));
            }
        }
        int minRoofHeight = numFloors * floorHeight;
        int maxRoofHeight = random.nextInt(20);
        base.buildFloor(minRoofHeight, getBlock("planks"));
        base.buildWalls(minRoofHeight, minRoofHeight, getBlock("log"));
        if (random.nextDouble() < .5) {
            for (int i = -1; i < base.w + 2; i++) {
                for (int j = -1; j < base.h + 2; j++) {
                    int roofHeight = minRoofHeight + min(i + 1, base.w + 1 - i, j + 1, base.h + 1 - j, maxRoofHeight);
                    blocks.setRange(i + base.x, j + base.y, Math.max(minRoofHeight, roofHeight - 1), roofHeight, getBlock("slate"));
                }
            }
        } else {
            boolean roofDir = random.nextDouble() < .5;
            for (int i = -1; i < base.w + 2; i++) {
                for (int j = -1; j < base.h + 2; j++) {
                    int roofHeight = minRoofHeight + (roofDir ? min(i + 1, base.w + 1 - i, maxRoofHeight) : min(j + 1, base.h + 1 - j, maxRoofHeight));
                    blocks.setRange(i + base.x, j + base.y, Math.max(minRoofHeight, roofHeight - 1), roofHeight, getBlock("slate"));
                    if ((i == 0 || i == base.w || j == 0 || j == base.h) && roofHeight > minRoofHeight + 2) {
                        blocks.setRange(i + base.x, j + base.y, minRoofHeight + 1, roofHeight - 2, getBlock("plaster"));
                    }
                }
            }
        }
        for (int i = base.x; i <= base.maxX(); i++) {
            for (int j = base.y; j <= base.maxY(); j++) {
                int worldElev = sc.world.heightmappedChunks.get(sc.worldPos(x + i, y + j, 0)).elevationAt(mod(x + i, CHUNK_SIZE), mod(y + j, CHUNK_SIZE)) - z;
                if (worldElev < 0) {
                    blocks.setRange(i, j, worldElev, -1, getBlock("dirt"));
                }
            }
        }
    }

    public RoomBorder findRoomBorder(Rectangle r1, Rectangle r2) {
        if (r1.maxX() == r2.x || r2.maxX() == r1.x) {
            int x = (r1.maxX() == r2.x) ? r2.x : r1.x;
            int minY = Math.max(r1.y, r2.y) + 1;
            int maxY = Math.min(r1.maxY(), r2.maxY()) - 1;
            if (maxY - minY >= 1) {
                return new RoomBorder(r1, r2, x, minY, maxY - minY + 1, false);
            }
        }
        if (r1.maxY() == r2.y || r2.maxY() == r1.y) {
            int y = (r1.maxY() == r2.y) ? r2.y : r1.y;
            int minX = Math.max(r1.x, r2.x) + 1;
            int maxX = Math.min(r1.maxX(), r2.maxX()) - 1;
            if (maxX - minX >= 1) {
                return new RoomBorder(r1, r2, minX, y, maxX - minX + 1, true);
            }
        }
        return null;
    }

    public List<RoomBorder> findRoomBorders(List<Rectangle> rooms) {
        List<RoomBorder> r = new LinkedList();
        for (int i = 0; i < rooms.size(); i++) {
            for (int j = i + 1; j < rooms.size(); j++) {
                RoomBorder rb = findRoomBorder(rooms.get(i), rooms.get(j));
                if (rb != null) {
                    r.add(rb);
                }
            }
        }
        return r;
    }

    public List<RoomBorder> findOutsideBorders(Rectangle base, List<Rectangle> rooms) {
        List<RoomBorder> r = new LinkedList();
        for (Rectangle room : rooms) {
            if (room.x == base.x) {
                r.add(new RoomBorder(room, null, room.x, room.y + 1, room.h - 1, false));
            }
            if (room.maxX() == base.maxX()) {
                r.add(new RoomBorder(room, null, room.maxX(), room.y + 1, room.h - 1, false));
            }
            if (room.y == base.y) {
                r.add(new RoomBorder(room, null, room.x + 1, room.y, room.w - 1, true));
            }
            if (room.maxY() == base.maxY()) {
                r.add(new RoomBorder(room, null, room.x + 1, room.maxY(), room.w - 1, true));
            }
        }
        return r;
    }

    public class Rectangle {

        private static final int MIN_SIZE = 5;

        public final int x, y, w, h;

        public Rectangle(int x, int y, int w, int h) {
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
        }

        public void buildCorners(int zMin, int zMax, BlockType bt) {
            blocks.setRange(x, y, zMin, zMax, bt);
            blocks.setRange(maxX(), y, zMin, zMax, bt);
            blocks.setRange(x, maxY(), zMin, zMax, bt);
            blocks.setRange(maxX(), maxY(), zMin, zMax, bt);
        }

        public void buildFloor(int z, BlockType bt) {
            for (int i = x + 1; i < maxX(); i++) {
                for (int j = y + 1; j < maxY(); j++) {
                    blocks.set(i, j, z, bt);
                }
            }
        }

        public void buildWalls(int zMin, int zMax, BlockType bt) {
            for (int i = x; i <= maxX(); i++) {
                blocks.setRange(i, y, zMin, zMax, bt);
                blocks.setRange(i, maxY(), zMin, zMax, bt);
            }
            for (int i = y; i <= maxY(); i++) {
                blocks.setRange(x, i, zMin, zMax, bt);
                blocks.setRange(maxX(), i, zMin, zMax, bt);
            }
        }

        public int maxX() {
            return x + w;
        }

        public int maxY() {
            return y + h;
        }

        public List<Rectangle> recursivelySubdivide() {
            int wChanges = Math.max(w - MIN_SIZE * 2, 0);
            int hChanges = Math.max(h - MIN_SIZE * 2, 0);
            if (wChanges + hChanges == 0) {
                return Arrays.asList(this);
            }
            int choice = random.nextInt(wChanges + hChanges + MIN_SIZE);
            if (choice < wChanges) {
                int w1 = choice + MIN_SIZE;
                Rectangle r1 = new Rectangle(x, y, w1, h);
                Rectangle r2 = new Rectangle(x + w1, y, w - w1, h);
                List<Rectangle> l = new LinkedList();
                l.addAll(r1.recursivelySubdivide());
                l.addAll(r2.recursivelySubdivide());
                return l;
            } else if (choice < wChanges + hChanges) {
                int h1 = choice - wChanges + MIN_SIZE;
                Rectangle r1 = new Rectangle(x, y, w, h1);
                Rectangle r2 = new Rectangle(x, y + h1, w, h - h1);
                List<Rectangle> l = new LinkedList();
                l.addAll(r1.recursivelySubdivide());
                l.addAll(r2.recursivelySubdivide());
                return l;
            } else {
                return Arrays.asList(this);
            }
        }
    }

    public class RoomBorder {

        public final Rectangle r1, r2;
        public final int x, y, length;
        public final boolean horizontal;

        public RoomBorder(Rectangle r1, Rectangle r2, int x, int y, int length, boolean horizontal) {
            this.r1 = r1;
            this.r2 = r2;
            this.x = x;
            this.y = y;
            this.length = length;
            this.horizontal = horizontal;
        }

        public int buildOpening(int zMin, int zMax, int length, BlockType bt) {
            int pos = random.nextInt(this.length - length + 1);
            for (int i = pos; i < pos + length; i++) {
                blocks.setRange(x + (horizontal ? i : 0), y + (horizontal ? 0 : i), zMin, zMax, bt);
            }
            return pos;
        }
    }
}
