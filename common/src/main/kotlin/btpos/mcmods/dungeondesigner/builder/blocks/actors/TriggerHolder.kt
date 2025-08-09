@file:Suppress("OVERRIDE_DEPRECATION", "DEPRECATION")

package btpos.mcmods.dungeondesigner.builder.blocks.actors

import btpos.mcmods.devutil.common.ext.kotlin.isNullOrTrue
import btpos.mcmods.devutil.common.ext.kotlin.safeGetDelegate
import btpos.mcmods.devutil.common.ext.vanilla.asComponent
import btpos.mcmods.devutil.common.ext.vanilla.data.nullSafeFieldOf
import btpos.mcmods.devutil.common.ext.vanilla.destructuring.component1
import btpos.mcmods.devutil.common.ext.vanilla.destructuring.component2
import btpos.mcmods.devutil.common.ext.vanilla.plus
import btpos.mcmods.devutil.common.ext.vanilla.sendSystemMessage
import btpos.mcmods.devutil.common.ext.vanilla.stack
import btpos.mcmods.devutil.common.ext.vanilla.world.BlockInclusiveAABB
import btpos.mcmods.devutil.common.ext.vanilla.world.BlockInclusiveAABB.Companion.toBlockInclusive
import btpos.mcmods.devutil.common.ext.vanilla.world.blockEntity
import btpos.mcmods.devutil.common.ext.vanilla.world.dropItemAboveBlock
import btpos.mcmods.devutil.common.ext.vanilla.world.actOnServer
import btpos.mcmods.devutil.common.ext.vanilla.world.with
import btpos.mcmods.devutil.common.kfflib.forge.vectorutil.v3d.toVec3
import btpos.mcmods.devutil.common.macros.ChatUtils.toComponent
import btpos.mcmods.devutil.common.structure.composition.IOnChange
import btpos.mcmods.devutil.common.structure.program.IReverseCloneable
import btpos.mcmods.devutil.common.util.serialization.ICodecSerializableMutable
import btpos.mcmods.devutil.common.util.serialization.Serialization
import btpos.mcmods.devutil.common.util.serialization.putCodecSerializable
import btpos.mcmods.devutil.common.util.serialization.readCodecSerializableToExisting
import btpos.mcmods.devutil.parts.IItemRepresentable
import btpos.mcmods.devutil.util.properties.LazyCache
import btpos.mcmods.dungeondesigner.POWERED
import btpos.mcmods.dungeondesigner.WorldUtils
import btpos.mcmods.dungeondesigner.builder.items.ItemTriggerVariable
import btpos.mcmods.dungeondesigner.common.nbtadapters.getDisplayName
import btpos.mcmods.dungeondesigner.common.nbtadapters.setDisplayName
import btpos.mcmods.dungeondesigner.registry.ModBlocks
import btpos.mcmods.dungeondesigner.registry.ModItems
import com.mojang.datafixers.util.Pair
import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.ChatFormatting
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.BlockGetter
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.EntityBlock
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.entity.BlockEntityTicker
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.StateDefinition
import net.minecraft.world.level.redstone.Orientation
import net.minecraft.world.level.storage.ValueInput
import net.minecraft.world.level.storage.ValueOutput
import net.minecraft.world.level.storage.loot.LootParams
import net.minecraft.world.level.storage.loot.parameters.LootContextParams
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.BlockHitResult
import kotlin.jvm.optionals.getOrNull

class BlockTriggerHolder(
	props: Properties
) : Block(props), EntityBlock {
	init {
		registerDefaultState(stateDefinition.any().with(POWERED, false))
	}
	
	companion object{
		const val id = "trigger_holder"
	}
	
	
	//region Redstone
	override fun isSignalSource(pState: BlockState) = true
	
	override fun getSignal(pState: BlockState, pLevel: BlockGetter, pPos: BlockPos, pDirection: Direction): Int {
		if (pState.getValue(POWERED))
			return 15
		else
			return 0
	}
	//endregion
	
	
	//region Configuration
	override fun createBlockStateDefinition(pBuilder: StateDefinition.Builder<Block, BlockState>) {
		super.createBlockStateDefinition(pBuilder)
		pBuilder.add(POWERED)
	}
	
	override fun newBlockEntity(pos: BlockPos, state: BlockState): BlockEntity? = ModBlocks.TRIGGER_BLOCK_ENTITY.create(pos, state)
	//endregion
	
	
	override fun neighborChanged(pState: BlockState, pLevel: Level, pPos: BlockPos, pNeighborBlock: Block, orientation: Orientation?, pMovedByPiston: Boolean) {
		super.neighborChanged(pState, pLevel, pPos, pNeighborBlock, orientation, pMovedByPiston)
		if (pState.getValue(POWERED) == true && shouldStopCheckingTrigger(pLevel, pPos)) {
			pLevel.setBlockAndUpdate(pPos, pState.with(POWERED, false))
		}
	}
	
	override fun useItemOn(
		itemInHand: ItemStack, pState: BlockState, pLevel: Level,
		pPos: BlockPos, pPlayer: Player,
		pHand: InteractionHand, pHit: BlockHitResult
	): InteractionResult {
		
		val ourEnt = pLevel.blockEntity(pPos, ModBlocks.TRIGGER_BLOCK_ENTITY) ?: return InteractionResult.PASS
		
		// Pop out trigger item into world if it exists
		if (pPlayer.isShiftKeyDown) {
			return pLevel.actOnServer {
				ourEnt.dropItem()
				ourEnt.triggerItem = ItemStack.EMPTY
			}.sidedSuccess
		}
		
		// Else add it to the block
		if (itemInHand.`is`(ModItems.TRIGGER_ITEM) && !ourEnt.hasTrigger()) {
			return pLevel.actOnServer {
				if (ourEnt.setItemWithFeedback(itemInHand)) {
					itemInHand.shrink(1)
				}
			}.sidedSuccess
		}
		
		// Finally, if no other action has occurred, show the bounds.
		return pLevel.actOnServer {
			if (!ourEnt.hasTrigger()) {
				pPlayer.sendSystemMessage("No bbox".asComponent())
			} else {
				val (first, second) = ourEnt.triggerCorners!!
				pPlayer.sendSystemMessage(
						Component.literal("Trigger bounds: ")
						+ first.toComponent().withStyle(ChatFormatting.YELLOW)
						+ " to "
						+ second.toComponent().withStyle(ChatFormatting.YELLOW)
				)
			}
		}.sidedSuccess
	}
	
	override fun getDrops(pState: BlockState, pParams: LootParams.Builder): MutableList<ItemStack> {
		val sup = super.getDrops(pState, pParams)
		val item = (pParams.getOptionalParameter(LootContextParams.BLOCK_ENTITY) as? TileTriggerHolder)?.triggerItem
		           ?: return sup
		sup += item
		return sup
	}
	
	override fun <T : BlockEntity> getTicker(pLevel: Level, pState: BlockState, pBlockEntityType: BlockEntityType<T>): BlockEntityTicker<T>? {
		return if (pLevel.isClientSide) null else BlockEntityTicker { level, pos, state, ent ->
			if (ent !is TileTriggerHolder || ent.triggerBoundingBox == null)
				return@BlockEntityTicker
			
			
			if (shouldStopCheckingTrigger(level, pos)) {
				// Don't tick if receiving redstone power from above
				return@BlockEntityTicker
			}
			
			val isPowered = state.getValue(POWERED)
			
			if (isPowered != WorldUtils.isPlayerInBoundingBox(ent.triggerBoundingBox!!, level)) {
				level.setBlockAndUpdate(pos, state.with(POWERED, !isPowered))
			}
		}
	}
	
	private fun shouldStopCheckingTrigger(level: Level, pos: BlockPos): Boolean = level.hasSignal(pos.above(), Direction.UP)
}

class TileTriggerHolder(p0: BlockPos, p1: BlockState) : BlockEntity(ModBlocks.TRIGGER_BLOCK_ENTITY, p0, p1) {
	companion object {
		private const val TAGKEY_STATE = "trigger"
	}
	
	private val state = TriggerHolderState(pOnChange = this::setChanged)
	
	var triggerCorners by state::corners
	val triggerBoundingBox get() = state.aabb?.bb
	
	var triggerItem: ItemStack by state.triggerDelegate::asItem
	
	fun hasTrigger() = state.hasTrigger()
	
	/**
	 * Drops the trigger held by this blockentity as an [ItemTriggerVariable] in the world above this block.
	 *
	 * Return true if successfully dropped, false otherwise.
	 */
	fun dropItem(): Boolean {
		if (!hasTrigger() || level?.isClientSide.isNullOrTrue())
			return false
		
		return (level as ServerLevel).dropItemAboveBlock(triggerItem, blockPos)
	}
	
	fun setItemWithFeedback(stack: ItemStack): Boolean {
		return state.triggerDelegate.setItemWithFeedback(stack)
	}
	
	override fun saveAdditional(tag: ValueOutput) {
		super.saveAdditional(tag)
		tag.putCodecSerializable(TAGKEY_STATE, state)
	}
	
	override fun loadAdditional(tag: ValueInput) {
		super.loadAdditional(tag)
		tag.readCodecSerializableToExisting(TAGKEY_STATE, state)
	}
}


class TriggerHolderState(
	pTrigger: Pair<BlockPos, BlockPos>? = null,
	pItemName: String? = null,
	pOnChange: () -> Unit = {}
) : IOnChange, ICodecSerializableMutable<TriggerHolderState> {
	override var onChange = pOnChange
		set(callback) {
			field = callback
			triggerDelegate.onChange = callback
		}
	
	//region Codec
	override fun codec() = CODEC
	override fun copyFrom(other: TriggerHolderState) {
		this.triggerDelegate.copyFrom(other.triggerDelegate)
	}
	//endregion
	
	val triggerDelegate = TriggerVarItemConverter(pTrigger, pItemName, pOnChange)
	
	var corners by triggerDelegate::value
	val itemName by triggerDelegate::name
	val aabb by triggerDelegate::cachedInclusiveAABB
	
	
	companion object {
		const val TAGKEY_BOUNDS = "trigger"
		const val TAGKEY_NAME = "item_name"
		
		val CODEC = RecordCodecBuilder.create {
			it.group(
					Serialization.pairCodec(BlockPos.CODEC, BlockPos.CODEC)
						.nullSafeFieldOf(TAGKEY_BOUNDS, TriggerHolderState::corners),
					Codec.STRING.nullSafeFieldOf(TAGKEY_NAME, TriggerHolderState::itemName)
			).apply(
					it,
					{ pTrigger, pItemName ->
						TriggerHolderState(
								pTrigger.getOrNull(),
								pItemName.getOrNull()
						)
					}
			)
		}
	}
	
	fun hasTrigger(): Boolean {
		return corners != null
	}
}

class TriggerVarItemConverter(triggerIn: Pair<BlockPos, BlockPos>? = null, nameIn: String? = null, override var onChange: () -> Unit = {}) : IOnChange, IItemRepresentable<Pair<BlockPos, BlockPos>>, IReverseCloneable<TriggerVarItemConverter> {
	/**
	 * A cache of the bounding box for the trigger we check every tick in [BlockTriggerHolder.getTicker].
	 *
	 * Derived from [value], and invalidated by [value]'s setter.
	 */
	val cachedInclusiveAABB: BlockInclusiveAABB? by LazyCache { value?.run { AABB(first.toVec3(), second.toVec3()).toBlockInclusive() } }
	
	/**
	 * Store the corners as BlockPos instead of the value as an AABB so we can return the item in the same way it was given,
	 * since the AABB constructor changes the corners around.
	 */
	override var value: Pair<BlockPos, BlockPos>? = triggerIn
		set(v) {
			onChange()
			::cachedInclusiveAABB.safeGetDelegate<LazyCache<*>>()?.invalidate()
			field = v
		}
	
	/**
	 * Also store the anvil name of the item so we can restore it with its name when it's popped out.
	 *
	 * We don't need to hook this up to [notify] since it's always set at the same time as [value] in [readFromTag].
	 */
	var name: String? = nameIn
		private set
	
	override fun setFromItem(stack: ItemStack): Boolean {
		val tagData = ItemTriggerVariable.getData(stack)?.takeIf { it.isComplete() } ?: return false
		value = Pair(tagData.first, tagData.second)
		getDisplayName(stack)?.let {
			name = it
		}
		
		return true
	}
	
	override fun writeToItem(): ItemStack? {
		if (value == null)
			return null
		
		return ModItems.TRIGGER_ITEM.stack().also { stack ->
			ItemTriggerVariable.modifyOrCreateData(stack) {
				first = value!!.first
				second = value!!.second
			}
			name?.let { setDisplayName(stack, it) }
		}
	}
	
	override fun acceptsItem(stack: ItemStack): Boolean {
		return stack.`is`(ModItems.TRIGGER_ITEM)
	}
	
	override fun copyFrom(other: TriggerVarItemConverter) {
		this.name = other.name
		this.value = other.value
	}
	
	override fun onEmptyItemStack(): Boolean {
		this.name = null
		this.value = null
		
		return true
	}
}