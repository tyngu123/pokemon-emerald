package br.com.yago.emerald.model;

public class Item {

    public enum ItemCategory {
        ITEMS("ITEMS"),
        POKEBALLS("POKé BALLS"),
        TMS_HMS("TMs & HMs"),
        BERRIES("BERRIES"),
        KEY_ITEMS("KEY ITEMS");

        private final String displayName;

        ItemCategory(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    private String name;
    private ItemCategory category;
    private int quantity;

    public Item(String name, ItemCategory category, int quantity) {
        this.name = name;
        this.category = category;
        this.quantity = quantity;
    }

    public String getName() {
        return name;
    }

    public ItemCategory getCategory() {
        return category;
    }

    public int getQuantity() {
        return quantity;
    }

    public void addQuantity(int amount) {
        this.quantity += amount;
    }

    @Override
    public String toString() {
        return name.toUpperCase() + " X" + quantity;
    }
}