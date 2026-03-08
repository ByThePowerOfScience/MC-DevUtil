package btpos.mcmods.devutil.common.structure.blocks

import btpos.mcmods.devutil.common.ext.vanilla.world.blockEntity
import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.BlockGetter
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.EntityBlock
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.entity.BlockEntityTicker
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.gameevent.GameEventListener

/**
 * Utilities for a [net.minecraft.world.level.block.Block] that can only have a single type of [BlockEntity].
 *
 * If this block can _ever_ have a different type of BlockEntity present, users should extend [EntityBlock] directly.
 */
interface BlockWithEntity<ENT_TYPE : BlockEntity> : EntityBlock {
	/**
	 * The sole [BlockEntityType] registered for this block. Should not contain any logic, just a trivial reference to something in your registrar.
	 */
	fun getEntityType(): BlockEntityType<ENT_TYPE>
	
	override fun newBlockEntity(pos: BlockPos, state: BlockState): BlockEntity? {
		return getEntityType().create(pos, state)
	}
	
	/**
	 * Get the BlockEntity for this block's position, asserting that it's the same type as this block's sole [BlockEntityType].
	 */
	fun BlockGetter.getOurEntity(pos: BlockPos): ENT_TYPE? {
		return blockEntity(pos, getEntityType())
	}
	
	
	/**
	 * Called when this block is placed or updated.  Returns a stateless function that will be called for this block every tick.
	 *
	 * Important:
	 * - This method is called on both the server and the client, so implementers may find the [Level#runOnServer][btpos.mcmods.devutil.common.ext.vanilla.world.runOnServer] method useful to create a ticker tied to the server-side. (e.g. `return level.runOnServer { { )
	 * - This lambda should only use the arguments provided, and should not capture any references to external objects.
	 *
	 * @return The new ticker, or `null` if no ticker is needed for this block. (default behavior)
	 */
	fun getTicker_typed(pLevel: Level, pState: BlockState, pBlockEntityType: BlockEntityType<ENT_TYPE>): ((level: Level, pos: BlockPos, state: BlockState, ent: ENT_TYPE) -> Unit)? = null
	
	/**
	 * Create an object that listens to game events in a certain radius around its position.
	 *
	 * This only listens for _vanilla_ game events, so it's only sound-based things that Sculk Sensors can react to.
	 *
	 * @return A listener, or `null` if no listener is desired. (default behavior)
	 */
	fun getListener_typed(level: ServerLevel, entity: ENT_TYPE): GameEventListener? = null
	
	
	@Suppress("UNCHECKED_CAST") @Deprecated("Use the strictly-typed variant.", level=DeprecationLevel.HIDDEN, replaceWith=ReplaceWith("getTicker_typed(level, state, blockEntityType)"))
	override fun <T : BlockEntity> getTicker(level: Level, state: BlockState, blockEntityType: BlockEntityType<T>): BlockEntityTicker<T>? {
		return getTicker_typed(level, state, blockEntityType as BlockEntityType<ENT_TYPE>)?.let { BlockEntityTicker(it) } as BlockEntityTicker<T>?
	}
	
	@Suppress("UNCHECKED_CAST") @Deprecated("Use the strictly-typed variant.", level=DeprecationLevel.HIDDEN, replaceWith=ReplaceWith("getListener_typed(level, blockEntity)"))
	override fun <T : BlockEntity> getListener(level: ServerLevel, blockEntity: T): GameEventListener? {
		return getListener_typed(level, blockEntity as ENT_TYPE)
	}
}