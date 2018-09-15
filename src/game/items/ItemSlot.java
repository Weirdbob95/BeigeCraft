package game.items;

import definitions.ItemType;
import static definitions.Loader.getItem;

public class ItemSlot {

    public static final ItemSlot[] INVENTORY = makeItemSlots(48);
    public static final ItemSlot[] QAW = makeItemSlots(8);
    public static final ItemSlot GRABBED = new ItemSlot();
    public static ItemSlot MAIN_HAND;
    public static ItemSlot OFF_HAND;

    static {
        addToInventory(
                getItem("sword"),
                getItem("dagger"),
                getItem("hammer"),
                getItem("spear"),
                getItem("pickaxe"),
                getItem("wand"),
                getItem("waterBucket"),
                getItem("block.lava"));
    }

    private ItemType item;
    private int count;

    private ItemSlot() {
    }

    private boolean addItem(ItemType i) {
        if (i != null) {
            if (item == null || (item.equals(i) && count < item.maxStackSize)) {
                item = i;
                count++;
                return true;
            }
        }
        return false;
    }

    public static void addToInventory(ItemType... a) {
        for (ItemType i : a) {
            addToInventory(i);
        }
    }

    public static boolean addToInventory(ItemType i) {
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

    public boolean isEmpty() {
        return item == null;
    }

    public ItemType item() {
        return item;
    }

    public static ItemSlot[] makeItemSlots(int num) {
        ItemSlot[] slots = new ItemSlot[num];
        for (int i = 0; i < num; i++) {
            slots[i] = new ItemSlot();
        }
        return slots;
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

    public void swapItems(ItemSlot other) {
        ItemType tempItem = other.item;
        int tempCount = other.count;
        other.item = item;
        other.count = count;
        item = tempItem;
        count = tempCount;
    }
}
