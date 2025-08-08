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

    /**
     * ## MÉTODO ATUALIZADO ##
     * Remove uma quantidade específica de um item. Se a quantidade chegar a zero,
     * o item é removido completamente da lista.
     * @param itemToToss O item a ser modificado.
     * @param quantityToToss A quantidade a ser descartada.
     */
    public void removeItem(Item itemToToss, int quantityToToss) {
        if (itemToToss == null || !items.contains(itemToToss, true)) {
            return;
        }

        itemToToss.decreaseQuantity(quantityToToss);

        if (itemToToss.getQuantity() <= 0) {
            items.removeValue(itemToToss, true);
        }
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