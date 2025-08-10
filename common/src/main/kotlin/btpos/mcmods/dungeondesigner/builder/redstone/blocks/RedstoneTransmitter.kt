@file:Suppress("OVERRIDE_DEPRECATION", "DEPRECATION")

package btpos.mcmods.dungeondesigner.builder.redstone.blocks

import btpos.mcmods.devutil.common.ext.java.invoke
import btpos.mcmods.devutil.common.ext.vanilla.asComponent
import btpos.mcmods.devutil.common.ext.vanilla.isClientSide
import btpos.mcmods.devutil.common.ext.vanilla.sendSystemMessage
import btpos.mcmods.devutil.common.ext.vanilla.targetBlockEntity
import btpos.mcmods.devutil.common.ext.vanilla.world.get
import btpos.mcmods.devutil.common.ext.vanilla.world.sidedSuccess
import btpos.mcmods.devutil.common.ext.vanilla.world.with
import btpos.mcmods.devutil.common.structure.blocks.BlockWithEntity
import btpos.mcmods.devutil.common.util.serialization.putCodecSerializable
import btpos.mcmods.devutil.common.util.serialization.readCodecSerializableToExisting
import btpos.mcmods.devutil.multiplatform.api.IPlatformConnectRedstone
import btpos.mcmods.dungeondesigner.MultiplatformHooks.getItemHandler
import btpos.mcmods.dungeondesigner.POWERED
import btpos.mcmods.dungeondesigner.builder.redstone.IWirelessRedstone
import btpos.mcmods.dungeondesigner.builder.redstone.IWirelessRedstone.Companion.NO_CHANNEL
import btpos.mcmods.dungeondesigner.builder.redstone.IWirelessRedstoneTransmitter
import btpos.mcmods.dungeondesigner.builder.redstone.items.ItemRemoteLinker
import btpos.mcmods.dungeondesigner.builder.world.dungeonBuilderData
import btpos.mcmods.dungeondesigner.registry.ModBlocks_Builder
import btpos.mcmods.dungeondesigner.registry.ModItemComponents
import btpos.mcmods.dungeondesigner.registry.ModItems
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.core.component.DataComponentGetter
import net.minecraft.core.component.DataComponentMap
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.BlockItem
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.TooltipFlag
import net.minecraft.world.item.component.TooltipDisplay
import net.minecraft.world.item.context.BlockPlaceContext
import net.minecraft.world.item.context.UseOnContext
import net.minecraft.world.level.BlockGetter
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.StateDefinition
import net.minecraft.world.level.redstone.Orientation
import net.minecraft.world.level.storage.ValueInput
import net.minecraft.world.level.storage.ValueOutput
import net.minecraft.world.phys.BlockHitResult
import java.util.function.Consumer

/**
 * In the builder phase, this stores the channel of an emitter that's linked to it, or otherwise can be linked to an emitter.  Think "RFTools Redstone Transmitter".
 * When compiled, this is literally just a block that can be powered, and the wireless emitter powers it directly.
 */
class BlockRedstoneTransmitter(props: Properties) : Block(props), BlockWithEntity<TileRedstoneTransmitter>, WirelessRedstoneBlock, IPlatformConnectRedstone {
	companion object {
		const val id = "builder/redstone_transmitter"
		/*
		@Suppress("DuplicatedCode")
		override fun BlockStateProvider.buildModelsAndStates() {
			val txBase = "redstone/transmitter/transmitter"
			
			val off = models().cubeColumn(id.powered(false), "${txBase}_sides".powered(false).blockLoc(), "${txBase}_top".powered(false).blockLoc())
			val on = models().cubeColumn(id.powered(true), "${txBase}_sides".powered(true).blockLoc(), "${txBase}_top".powered(true).blockLoc())
			
			variantDsl(ModBlocks_Builder.REDSTONE_TRANSMITTER) {
				POWERED {
					true {
						model {
							modelFile(on)
						}
					}
					false {
						model {
							modelFile(off)
						}
					}
				}
			}
			
			simpleBlockItem(ModBlocks_Builder.REDSTONE_TRANSMITTER, off)
		}*/
	}
	
	//region Setup
	init {
		registerDefaultState(stateDefinition.any().with(POWERED, false))
	}
	
	override fun createBlockStateDefinition(pBuilder: StateDefinition.Builder<Block, BlockState>) {
		super.createBlockStateDefinition(pBuilder)
		pBuilder.add(POWERED)
	}
	
	override fun getEntityType() = ModBlocks_Builder.REDSTONE_TRANSMITTER_ENTITY
	//endregion
	
	override fun setPlacedBy(
		pLevel: Level,
		pPos: BlockPos,
		pState: BlockState,
		pPlacer: LivingEntity?,
		pStack: ItemStack
	) {
		super.setPlacedBy(pLevel, pPos, pState, pPlacer, pStack)

//        if (pStack.item !== ModBlocks_Builder.REDSTONE_TRANSMITTER_ITEM)
//            return
//
//        if (!pLevel.isClientSide) {
//
//            pLevel.getOurEntity(pPos)?.run {
//                val toSet = IWirelessRedstone.getFromStack(pStack)?.channel?.takeIf{ it != NO_CHANNEL } ?:
//                            it.setChannel()
//            }
//
//            pLevel.getOurEntity(pPos)?.let {
//                val redstoneHandler = (pLevel as ServerLevel).dataStorage.dungeonBuilderData.redstoneHandler
//                it.channel = pStack.getData()?.channel ?: redstoneHandler.makeNewChannel()
//            } ?: return MOD_LOGGER.error("No block entity found at pos $pPos!", Throwable())
//
//        }
	}
	
	override fun neighborChanged(
		pState: BlockState,
		pLevel: Level,
		pPos: BlockPos,
		pNeighborBlock: Block,
		orientation: Orientation?,
		pMovedByPiston: Boolean
	) {
		super.neighborChanged(pState, pLevel, pPos, pNeighborBlock, orientation, pMovedByPiston)
		
		if (pLevel is ServerLevel) {
			val isPowered = pState[POWERED]
			val shouldBePowered = pLevel.hasNeighborSignal(pPos)
			if (isPowered != shouldBePowered) {
				pLevel.setBlockAndUpdate(pPos, pState.with(POWERED, !isPowered))
				if (shouldBePowered) {
					pLevel.getOurEntity(pPos)?.onStartReceivingSignal(pLevel)
					doRemoteLinkers(pLevel, pPos, true)
				} else {
					pLevel.getOurEntity(pPos)?.onStopReceivingSignal(pLevel)
					doRemoteLinkers(pLevel, pPos, false)
				}
			}
		}
	}
	
	private fun doRemoteLinkers(pLevel: ServerLevel, pPos: BlockPos, powered: Boolean) {
		getRemoteLinkers(pLevel, pPos).forEach {
			val targetPos = ItemRemoteLinker.getData(it)?.target ?: return@forEach
			val state = pLevel.getBlockState(targetPos)
			if (state.hasProperty(POWERED))
				pLevel.setBlockAndUpdate(targetPos, state.with(POWERED, powered))
		}
	}
	
	fun getRemoteLinkers(level: ServerLevel, pos: BlockPos): Sequence<ItemStack> {
		val cap = level.getBlockEntity(pos.above())?.getItemHandler(null) ?: return emptySequence()
		return cap.iterFullSlots().filter { it.item == ModItems.REMOTE_LINKER }
	}
	
	override fun useWithoutItem(state: BlockState, level: Level, pos: BlockPos, player: Player, hitResult: BlockHitResult): InteractionResult? {
		super<WirelessRedstoneBlock>.onRightClick(level, pos, player)
		return super.useWithoutItem(state, level, pos, player, hitResult)
	}
	
	//region Redstone
	override fun canConnectRedstone(state: BlockState, level: BlockGetter, pos: BlockPos, direction: Direction?) = true
	//endregion
	
	override fun asItem(): Item {
		return ModBlocks_Builder.REDSTONE_TRANSMITTER_ITEM
	}
	
	
}

class TileRedstoneTransmitter(pPos: BlockPos, pState: BlockState, val redstone: IWirelessRedstone.Mutable = IWirelessRedstone.Mutable(NO_CHANNEL))
	: BlockEntity(ModBlocks_Builder.REDSTONE_TRANSMITTER_ENTITY, pPos, pState), IWirelessRedstoneTransmitter, IWirelessRedstone by redstone {
	init {
		redstone.onChange = this::setChanged
	}
	
	override fun applyImplicitComponents(componentGetter: DataComponentGetter) {
		super.applyImplicitComponents(componentGetter)
		if (level!!.isClientSide) {
			return
		}
		
		val channelToSet = componentGetter.get(ModItemComponents.WIRELESS_REDSTONE)
			                   ?.channel
			                   ?.takeIf { it != NO_CHANNEL }
		                   ?: (level as ServerLevel).dataStorage.dungeonBuilderData.redstoneHandler.makeNewChannel()
		
		redstone.channel = channelToSet
	}
	
	override fun collectImplicitComponents(components: DataComponentMap.Builder) {
		super.collectImplicitComponents(components)
		components.set(ModItemComponents.WIRELESS_REDSTONE, this.redstone)
	}
	
	override fun saveAdditional(output: ValueOutput) {
		super.saveAdditional(output)
		output.putCodecSerializable("wireless_redstone", redstone)
	}
	
	override fun loadAdditional(input: ValueInput) {
		super.loadAdditional(input)
		input.readCodecSerializableToExisting("wireless_redstone", redstone)
	}
	
	fun setChannel(channel: Int) {
		this.redstone.channel = channel
	}
}

class ItemBlockRedstoneTransmitter(props: Properties) : BlockItem(ModBlocks_Builder.REDSTONE_TRANSMITTER, props) {
	override fun useOn(pContext: UseOnContext): InteractionResult {
		val target = pContext.targetBlockEntity
		
		if (WirelessRedstoneItem.useOn(target, pContext)) {
			return sidedSuccess(pContext.isClientSide)
		}
		
		return super<BlockItem>.useOn(pContext)
	}
	
	override fun placeBlock(pContext: BlockPlaceContext, pState: BlockState): Boolean {
		return super.placeBlock(pContext, pState)
	}
	
	override fun appendHoverText(stack: ItemStack, context: TooltipContext, tooltipDisplay: TooltipDisplay, tooltipAdder: Consumer<Component>, flag: TooltipFlag) {
		super.appendHoverText(stack, context, tooltipDisplay, tooltipAdder, flag)
		WirelessRedstoneItem.appendHoverText(stack) { tooltipAdder(it) }
	}
}

/**
 * Consolidates some logic for items wrapping wireless redstone so my IDE stops yelling at me about duplicates,
 *  namely hover text and assigning channels to the block entity you click on.
 */
object WirelessRedstoneItem {
	inline fun appendHoverText(pStack: ItemStack, addTooltip: (Component) -> Unit) {
		IWirelessRedstone.getFromStack(pStack)?.channel?.takeIf { it != NO_CHANNEL }?.let {
			addTooltip("Channel: $it".asComponent())
		}
	}
	
	fun useOn(target: BlockEntity?, pContext: UseOnContext): Boolean {
		if (target !is IWirelessRedstone) { // TODO make this a capability instead
			return false
		}
		
		if (!pContext.isClientSide) {
			IWirelessRedstone.modifyData(pContext.itemInHand) {
				it.channel = target.channel
			}
		}
		
		return true
	}
}

interface WirelessRedstoneBlock {
	fun onRightClick(pLevel: Level, pPos: BlockPos, pPlayer: Player) {
		if (pLevel.isClientSide)
			return
		val ent = pLevel.getBlockEntity(pPos) as? IWirelessRedstone ?: return
		pPlayer.sendSystemMessage("Channel: ${ent.channel}".asComponent())
	}
}