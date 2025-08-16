@file:Suppress("NOTHING_TO_INLINE")

package btpos.mcmods.devutil.common.ext.vanilla

import net.minecraft.world.entity.Entity
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.context.UseOnContext
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.phys.EntityHitResult

inline fun Item.stack() = ItemStack(this)
inline fun Item.stack(count: Int) = ItemStack(this, count)

inline val UseOnContext.targetBlockState: BlockState
	inline get() = level.getBlockState(clickedPos)

inline val UseOnContext.targetBlockEntity: BlockEntity?
	inline get() = level.getBlockEntity(clickedPos)

inline val UseOnContext.isClientSide: Boolean
	inline get() = level.isClientSide

inline val UseOnContext.targetEntity: Entity?
	inline get() = player?.run {
		val raycast = pick(pickRadius.toDouble(), 1f, false) as? EntityHitResult
		return@run raycast?.entity
	}

inline fun ItemStack.isNotEmpty() = !isEmpty