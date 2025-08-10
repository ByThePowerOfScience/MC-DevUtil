package btpos.mcmods.devutil.common.structure.composition

import net.minecraft.world.item.ItemStack
import org.jetbrains.annotations.Contract

/**
 * Wraps some value, and provides functions converting it to and from an [net.minecraft.world.item.ItemStack].
 */
interface IItemRepresentable<T> {
	/**
	 * The ItemStack to be returned if [writeToItem] returns `null`.
	 *
	 * Defaults to [net.minecraft.world.item.ItemStack.EMPTY]
	 */
	val itemForNull: ItemStack
		get() = ItemStack.EMPTY
	
	
	/**
	 * The actual value being wrapped.
	 *
	 * This is used as the default assignment operator target, but there can be multiple value fields in this class.
	 */
	var value: T?
	
	/**
	 * The ItemStack representation of [value].
	 *
	 * If the value of [value] is null, returns [itemForNull]. (defaults to [ItemStack.EMPTY])
	 */
	var asItem: ItemStack
		get() {
			return writeToItem() ?: itemForNull
		}
		set(stack) {
			setItemWithFeedback(stack)
		}
	
	/**
	 * Sets this object's internal state from an ItemStack.
	 *
	 * @return True if the state was changed, false otherwise.
	 */
	fun setItemWithFeedback(stack: ItemStack): Boolean {
		return when {
			stack.isEmpty -> onEmptyItemStack()
			acceptsItem(stack) -> setFromItem(stack)
			else -> false
		}
	}
	
	/**
	 * Store this object's state in item form.
	 *
	 * Return null to use [itemForNull] instead.
	 */
	@Contract(pure=true)
	fun writeToItem(): ItemStack?
	
	/**
	 * Should set the value of [value] based on the ItemStack provided.
	 *
	 * @param stack An ItemStack. May be empty, even if acceptsItem does not allow it.
	 */
	@Contract(pure=false)
	fun setFromItem(stack: ItemStack): Boolean
	
	/**
	 * Check whether this should accept the provided itemstack.
	 *
	 * @param stack The itemstack the caller is attempting to insert into this object.
	 */
	@Contract(pure=true)
	fun acceptsItem(stack: ItemStack): Boolean
	
	/**
	 * Called when someone attempts to set [asItem] to [ItemStack.EMPTY].
	 *
	 * Since ItemStacks cannot be null, this should handle any "would set to null" behaviors.
	 *
	 * @return True if this modified the state and therefore should be accepted, false otherwise.
	 */
	@Contract(pure=false)
	fun onEmptyItemStack(): Boolean {
		return false
	}
}