package game.items;

import world.BlockType;

public class ItemSlot {

    public static final ItemSlot[] INVENTORY = new ItemSlot[48];
    public static final ItemSlot[] QAW = new ItemSlot[8];
    public static final ItemSlot GRABBED = new ItemSlot();
    public static ItemSlot MAIN_HAND;
    public static ItemSlot OFF_HAND;

    static {
        for (int i = 0; i < INVENTORY.length; i++) {
            INVENTORY[i] = new ItemSlot();
        }
        for (int i = 0; i < QAW.length; i++) {
            QAW[i] = new ItemSlot();
        }
        addToInventory(new SwordItem(),
                new PickaxeItem(),
                new WandItem(),
                new BlockItem(BlockType.WOOD),
                new BlockItem(BlockType.WOOD));
    }

    private Item item;
    private int count;

    private ItemSlot() {
    }

    private boolean addItem(Item i) {
        if (i != null) {
            if (item == null || (item.equals(i) && count < item.maxStackSize())) {
                item = i;
                count++;
                return true;
            }
        }
        return false;
    }

    public static void addToInventory(Item... a) {
        for (Item i : a) {
            addToInventory(i);
        }
    }

    public static boolean addToInventory(Item i) {
        if (i == null) {
            return true;
        }
        for (ItemSlot is : QAW) {
            if (is.item != null && is.addItem(i)) {
                return true;
            }
        }
        for (ItemSlot is : INVENTORY) {
            if (is.item != null && is.addItem(i)) {
                return true;
            }
        }
        for (ItemSlot is : QAW) {
            if (is.addItem(i)) {
                return true;
            }
        }
        for (ItemSlot is : INVENTORY) {
            if (is.addItem(i)) {
                return true;
            }
        }
        return false;
    }

    public int count() {
        return count;
    }

    public Item item() {
        return item;
    }

    public void moveItemsTo(ItemSlot other) {
        while (other.addItem(item)) {
            count--;
            if (count == 0) {
                item = null;
            }
        }
    }

    public void moveItemsTo(ItemSlot other, int max) {
        while (max > 0 && other.addItem(item)) {
            max--;
            count--;
            if (count == 0) {
                item = null;
            }
        }
    }

    public void removeItem() {
        count--;
        if (count == 0) {
            item = null;
        }
    }
}
