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

    private final String name;
    private final String description;
    private final String spriteName;
    private final ItemCategory category;
    private int quantity;

    public Item(String name, String description, String spriteName, ItemCategory category, int quantity) {
        this.name = name;
        this.description = description;
        this.spriteName = spriteName;
        this.category = category;
        this.quantity = quantity;
    }

    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getSpriteName() { return spriteName; }
    public ItemCategory getCategory() { return category; }
    public int getQuantity() { return quantity; }

    public void addQuantity(int amount) {
        this.quantity += amount;
    }

    /**
     * ## NOVO MÉTODO ##
     * Diminui a quantidade do item.
     * @param amount A quantidade a ser removida.
     */
    public void decreaseQuantity(int amount) {
        this.quantity -= amount;
        if (this.quantity < 0) {
            this.quantity = 0;
        }
    }

    @Override
    public String toString() {
        return name.toUpperCase() + " X" + quantity;
    }
}