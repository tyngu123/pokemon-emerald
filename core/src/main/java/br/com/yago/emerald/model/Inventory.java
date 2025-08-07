package br.com.yago.emerald.model;

import com.badlogic.gdx.utils.Array;

public class Inventory {

    private final Array<Item> items = new Array<>();

    public void addItem(Item newItem) {
        for (Item existingItem : items) {
            if (existingItem.getName().equals(newItem.getName()) && existingItem.getCategory() == newItem.getCategory()) {
                existingItem.addQuantity(newItem.getQuantity());
                return;
            }
        }
        items.add(newItem);
    }

    public Array<Item> getItemsByCategory(Item.ItemCategory category) {
        Array<Item> filteredItems = new Array<>();
        for (Item item : items) {
            if (item.getCategory() == category) {
                filteredItems.add(item);
            }
        }
        return filteredItems;
    }

    public Array<Item> getAllItems() {
        return new Array<>(items);
    }
}