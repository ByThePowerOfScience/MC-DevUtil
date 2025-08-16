@file:Suppress("NOTHING_TO_INLINE")

package btpos.mcmods.devutil.common.ext.vanilla.world

import btpos.mcmods.devutil.common.kfflib.forge.vectorutil.v3d.toVec3
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.resources.ResourceKey
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.item.ItemEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.ChunkPos
import net.minecraft.world.level.Level
import net.minecraft.world.level.LevelAccessor
import net.minecraft.world.level.LevelReader
import net.minecraft.world.level.SignalGetter
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.chunk.ChunkAccess
import net.minecraft.world.phys.Vec3

inline fun LevelAccessor.getChunk(pos: ChunkPos): ChunkAccess = this.getChunk(pos.x, pos.z)


/**
 * Macro to drop an itemstack in the world at the given position.
 *
 * @param pos The position of the block the item should be spawned on top of.
 * @param velocity Velocity of the spawned item. Defaults to random.
 */
fun ServerLevel.dropItem(item: ItemStack, pos: Vec3, velocity: Vec3? = null): Boolean {
	val ent = if (velocity != null) {
		ItemEntity(this, pos.x, pos.y, pos.z, item, velocity.x, velocity.y, velocity.z)
	} else {
		ItemEntity(this, pos.x, pos.y, pos.z, item)
	}
	
	return this.addFreshEntity(ent)
}

/**
 * Macro to drop an itemstack 1 y-level above a block, since I'm doing that a lot.
 *
 * @param pos The position of the block the item should be spawned on top of.
 * @param velocity Velocity of the spawned item. Defaults to [0, 0.1, 0]. Set to `null` for random.
 */
fun ServerLevel.dropItemAboveBlock(item: ItemStack, pos: BlockPos, velocity: Vec3? = Vec3(0.0, 0.1, 0.0)): Boolean {
	return this.dropItem(item, pos.above().toVec3(), velocity)
}

/**
 * Apply a transformation to the blockstate at the given position.
 * Updates the block on both the client and server.
 *
 * A macro for `level.setBlockAndUpdate(pos, level.getBlock(pos).something())`.
 *
 * @param updater An inlined function that accepts the old state and returns the new.
 */
inline fun Level.modifyBlockAndUpdate(pos: BlockPos, updater: (BlockState) -> BlockState): Boolean {
	return this.setBlockAndUpdate(pos, updater(this.getBlockState(pos)))
}

/**
 * Apply a transformation to the blockstate at the given position.
 *
 * A macro for `level.setBlock(pos, level.getBlock(pos).something(), flags)`.
 *
 * @param updater Function that accepts the old state and returns the new.
 */
inline fun Level.modifyBlock(pos: BlockPos, flags: Int, updater: (BlockState) -> BlockState) = this.setBlock(pos, this.getBlockState(pos).let(updater), flags)

operator fun ResourceKey<Level>.contains(player: Player): Boolean {
	return player.level().dimension() == this
}

/**
 * Just a macro for `level.getSignal(pos.relative(dir), dir)`.
 *
 * @param ourPos The pos of this block
 * @param checkingDir The direction it's looking for signal from
 */
inline fun SignalGetter.getSignalReceivedFrom(ourPos: BlockPos, checkingDir: Direction) = this.getSignal(ourPos.relative(checkingDir), checkingDir)

/**
 * A [LevelReader] wrapped with some additional methods for after a sided run.
 *
 * Since the methods are pretty much *only* useful in that specific case,
 * this acts as a namespace so they don't constantly appear in autocomplete.
 *
 * @see actOnServer
 * @see actOnClient
 * @see actOnServerLevel
 */
@JvmInline
value class AfterSidedRun(val level: LevelReader) {
	inline val sidedSuccess: InteractionResult
		inline get() = sidedSuccess(level.isClientSide)
}

/**
 * Runs [action] if this level is not on the logical client.
 *
 * Returns reference to the given level with additional methods attached.
 *
 * @see actOnClient
 * @see actOnServerLevel
 * @see runOnServer runOnServer: The version of this method that returns a value.
 */
inline fun LevelReader.actOnServer(action: () -> Unit): AfterSidedRun {
    if (!this.isClientSide)
        action()
    
	return AfterSidedRun(this)
}

/**
 * Runs [action] if this level is not on the logical client.
 *
 * Returns `this` but with additional methods attached.
 *
 * @param action Action to be run on the server, with the receiver being `this as ServerLevel`.
 *
 * @see actOnClient
 * @see actOnServer
 */
inline fun LevelReader.actOnServerLevel(action: ServerLevel.() -> Unit): AfterSidedRun {
	if (!this.isClientSide)
		action(this as ServerLevel)
	
	return AfterSidedRun(this)
}

/**
 * Runs [action] if the receiver level is not on the logical client.
 *
 * Returns `this` but with additional methods attached.
 *
 * @param action Action to be run on the server, with the receiver being `this as ServerLevel`.
 * @see actOnServer
 * @see actOnServerLevel
 */
inline fun LevelReader.actOnClient(action: () -> Unit): AfterSidedRun {
	if (this.isClientSide)
		action()
	
	return AfterSidedRun(this)
}

/**
 * Calculates the value if this level is not on the logical client, or else returns null.
 *
 * Named such because I kept accidentally thinking [the old runOnServer][actOnServer] would act the same as [run].
 *
 * @return `null` if this is the client, otherwise the value provided by [mapper].
 *
 * @see actOnServer
 */
inline fun <R> LevelReader.runOnServer(mapper: ServerLevel.() -> R): R? {
	if (this.isClientSide)
		return null
	
	return (this as ServerLevel).mapper()
}