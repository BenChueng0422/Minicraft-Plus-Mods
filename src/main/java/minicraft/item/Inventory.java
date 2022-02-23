package minicraft.item;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.jetbrains.annotations.Nullable;

import minicraft.entity.furniture.Furniture;
import minicraftmodsapiinterface.*;

public class Inventory implements IInventory {
	private final Random random = new Random();
	private final List<Item> items = new ArrayList<>(); // The list of items that is in the inventory.
	
	/**
	 * Returns all the items which are in this inventory.
	 * @return ArrayList containing all the items in the inventory.
	 */
	public List<IItem> getItems() { return new ArrayList<>(items); }
	public void clearInv() { items.clear(); }
	public int invSize() { return items.size(); }

	/**
	 * Get one item in this inventory.
	 * @param idx The index of the item in the inventory's item array.
	 * @return The specified item.
	 */
	public IItem get(int idx) { return items.get(idx); }

	/**
	 * Remove an item in this inventory.
	 * @param idx The index of the item in the inventory's item array.
	 * @return The removed item.
	 */
	public IItem remove(int idx) { return items.remove(idx); }
	
	public void addAll(IInventory other) {
		for (IItem i: other.getItems())
			add(i.clone());
	}
	
	/** Adds an item to the inventory */
	public void add(@Nullable IItem item) {
		if (item != null)
			add(items.size(), item);  // Adds the item to the end of the inventory list
	}
	
	/**
	 * Adds several copies of the same item to the end of the inventory.
	 * @param item IItem to be added.
	 * @param num Amount of items to add.
	 */
	public void add(IItem item, int num) {
		for (int i = 0; i < num; i++)
			add(item.clone());
	}
	
	/**
	 * Adds an item to a specific spot in the inventory.
	 * @param slot Index to place item at.
	 * @param item IItem to be added.
	 */
	public void add(int slot, IItem item) {

		// Do not add to inventory if it is a PowerGlove
		if (item instanceof PowerGloveItem) {
			System.out.println("WARNING: tried to add power glove to inventory. stack trace:");
			Thread.dumpStack();
			return;
		}

		if (item instanceof StackableItem) { // If the item is a item...
			StackableItem toTake = (StackableItem) item; // ...convert it into a StackableItem object.
			
			boolean added = false;
			for (IItem value : items) {
				if (toTake.stacksWith(value)) {
					// Matching implies that the other item is stackable, too.
					((StackableItem) value).count += toTake.count;
					added = true;
					break;
				}
			}
			
			if (!added) items.add(slot, toTake);
		} else {
			items.add(slot, (Item)item); // Add the item to the items list
		}
	}
	
	/** Removes items from your inventory; looks for stacks, and removes from each until reached count. returns amount removed. */
	private int removeFromStack(StackableItem given, int count) {
		int removed = 0; // To keep track of amount removed.
		for (int i = 0; i < items.size(); i++) {
			if (!(items.get(i) instanceof StackableItem)) continue;
			StackableItem curItem = (StackableItem) items.get(i);
			if (!curItem.stacksWith(given)) continue; // Can't do equals, becuase that includes the stack size.
			// equals; and current item is stackable.
			int amountRemoving = Math.min(count-removed, curItem.count); // This is the number of items that are being removed from the stack this run-through.
			curItem.count -= amountRemoving;
			if (curItem.count == 0) { // Remove the item from the inventory if its stack is empty.
				remove(i);
				i--;
			}
			removed += amountRemoving;
			if (removed == count) break;
			if (removed > count) { // Just in case...
				System.out.println("SCREW UP while removing items from stack: " + (removed-count) + " too many.");
				break;
			}
			// If not all have been removed, look for another stack.
		}
		
		if (removed < count) System.out.println("Inventory: could not remove all items; " + (count-removed) + " left.");
		return removed;
	}
	
	/** 
	 * Removes the item from the inventory entirely, whether it's a stack, or a lone item.
	 */
	public void removeItem(IItem i) {
		//if (Game.debug) System.out.println("original item: " + i);
		if (i instanceof StackableItem)
			removeItems(i.clone(), ((StackableItem)i).count);
		else
			removeItems(i.clone(), 1);
	}
	
	/**
	 * Removes items from this inventory. Note, if passed a stackable item, this will only remove a max of count from the stack.
	 * @param given IItem to remove.
	 * @param count Max amount of the item to remove.
	 */
	public void removeItems(IItem given, int count) {
		if (given instanceof StackableItem)
			count -= removeFromStack((StackableItem)given, count);
		else {
			for (int i = 0; i < items.size(); i++) {
				IItem curItem = items.get(i);
				if (curItem.equals(given)) {
					remove(i);
					count--;
					if (count == 0) break;
				}
			}
		}
		
		if (count > 0)
			System.out.println("WARNING: could not remove " + count + " " + given + (count>1?"s":"") + " from inventory");
	}
	
	/** Returns the how many of an item you have in the inventory. */
	public int count(IItem given) {
		if (given == null) return 0; // null requests get no items. :)
		
		int found = 0; // Initialize counting var
		// Assign current item
		for (IItem curItem : items) { // Loop though items in inv
			// If the item can be a stack...
			if (curItem instanceof StackableItem && ((StackableItem) curItem).stacksWith(given))
				found += ((StackableItem) curItem).count; // Add however many items are in the stack.
			else if (curItem.equals(given))
				found++; // Otherwise, just add 1 to the found count.
		}
		
		return found;
	}
	
	/**
	 * Generates a string representation of all the items in the inventory which can be sent
	 * over the network.
	 * @return String representation of all the items in the inventory.
	 */
	public String getItemData() {
		StringBuilder itemdata = new StringBuilder();
		for (IItem i: items)
			itemdata.append(i.getData()).append(":");
		
		if (itemdata.length() > 0)
			itemdata = new StringBuilder(itemdata.substring(0, itemdata.length() - 1)); // Remove extra ",".
		
		return itemdata.toString();
	}
	
	/**
	 * Replaces all the items in the inventory with the items in the string.
	 * @param items String representation of an inventory.
	 */
	public void updateInv(String items) {
		clearInv();
		
		if (items.length() == 0) return; // There are no items to add.
		
		for (String item: items.split(":")) // This still generates a 1-item array when "items" is blank... [""].
			add(Items.get(item));
	}
	
	/**
	 * Tries to add an item to the inventory.
	 * @param chance Chance for the item to be added.
	 * @param item IItem to be added.
	 * @param num How many of the item.
	 * @param allOrNothing if true, either all items will be added or none, if false its possible to add
	 * between 0-num items.
	 */
	public void tryAdd(int chance, IItem item, int num, boolean allOrNothing) {
		if (!allOrNothing || random.nextInt(chance) == 0)
			for (int i = 0; i < num; i++)
				if (allOrNothing || random.nextInt(chance) == 0)
					add(item.clone());
	}
	public void tryAdd(int chance, @Nullable IItem item, int num) {
		if (item == null) return;
		if (item instanceof StackableItem) {
			((StackableItem)item).count *= num;
			tryAdd(chance, item, 1, true);
		} else
			tryAdd(chance, item, num, false);
	}
	public void tryAdd(int chance, @Nullable IItem item) { tryAdd(chance, item, 1); }
	public void tryAdd(int chance, IToolType type, int lvl) {
		tryAdd(chance, new ToolItem((ToolType)type, ItemLevel.LevelInstances.get(lvl)));
	}
	
	/**
	 * Tries to add an Furniture to the inventory.
	 * @param chance Chance for the item to be added.
	 * @param type Type of furniture to add.
	 */
	public void tryAdd(int chance, IFurniture type) {
		tryAdd(chance, new FurnitureItem((Furniture)type));
	}
}
