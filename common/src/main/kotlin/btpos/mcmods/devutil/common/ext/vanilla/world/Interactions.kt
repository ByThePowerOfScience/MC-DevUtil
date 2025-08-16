@file:Suppress("NOTHING_TO_INLINE")

package btpos.mcmods.devutil.common.ext.vanilla.world

import net.minecraft.world.InteractionResult

fun sidedSuccess(isClientSide: Boolean): InteractionResult {
	return if (isClientSide) {
		InteractionResult.SUCCESS
	} else {
		InteractionResult.SUCCESS_SERVER
	}
}