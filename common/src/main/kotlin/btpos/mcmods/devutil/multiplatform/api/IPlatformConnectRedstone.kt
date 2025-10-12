package btpos.mcmods.devutil.multiplatform.api

import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.world.level.BlockGetter
import net.minecraft.world.level.block.state.BlockState

/**
 * Implementing this interface on a block allows redstone dust to connect to it.
 *
 * NOTE: You must be using the Devutil Gradle Plugin for this to work on NeoForge.
 * - On NeoForge, this method is statically replaced with `IBlockExtensions#canConnectRedstone`.
 * - On Fabric, this is handled through [a Mixin][btpos.mcmods.devutil.mixin.api.MConnectRedstone].
 */
interface IPlatformConnectRedstone {
	fun canConnectRedstone(state: BlockState, level: BlockGetter, pos: BlockPos, direction: Direction?): Boolean
}