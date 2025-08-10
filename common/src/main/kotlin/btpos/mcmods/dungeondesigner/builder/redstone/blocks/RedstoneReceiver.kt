@file:Suppress("OVERRIDE_DEPRECATION", "DEPRECATION")

package btpos.mcmods.dungeondesigner.builder.redstone.blocks

import btpos.mcmods.devutil.common.ext.java.invoke
import btpos.mcmods.devutil.common.ext.vanilla.isClientSide
import btpos.mcmods.devutil.common.ext.vanilla.targetBlockEntity
import btpos.mcmods.devutil.common.ext.vanilla.world.get
import btpos.mcmods.devutil.common.ext.vanilla.world.actOnServerLevel
import btpos.mcmods.devutil.common.ext.vanilla.world.runOnServer
import btpos.mcmods.devutil.common.ext.vanilla.world.sidedSuccess
import btpos.mcmods.devutil.common.ext.vanilla.world.with
import btpos.mcmods.devutil.common.structure.blocks.BlockWithEntity
import btpos.mcmods.devutil.common.util.serialization.putCodecSerializable
import btpos.mcmods.devutil.common.util.serialization.readCodecSerializableToExisting
import btpos.mcmods.devutil.multiplatform.api.IPlatformConnectRedstone
import btpos.mcmods.dungeondesigner.MOD_LOGGER
import btpos.mcmods.dungeondesigner.POWERED
import btpos.mcmods.dungeondesigner.builder.redstone.IWirelessRedstone
import btpos.mcmods.dungeondesigner.builder.redstone.IWirelessRedstone.Companion.NO_CHANNEL
import btpos.mcmods.dungeondesigner.builder.world.dungeonBuilderData
import btpos.mcmods.dungeondesigner.registry.ModBlocks_Builder
import btpos.mcmods.dungeondesigner.registry.ModItemComponents
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
import net.minecraft.world.level.storage.ValueInput
import net.minecraft.world.level.storage.ValueOutput
import net.minecraft.world.phys.BlockHitResult
import java.util.function.Consumer

/**
 * In the builder phase, this stores the channel of an emitter that's linked to it, or otherwise can be linked to an emitter.  Think "RFTools Redstone Receiver".
 * When compiled, this is literally just a block that can be powered, and the wireless emitter powers it directly.
 */
class BlockRedstoneReceiver(props: Properties) : Block(props), BlockWithEntity<TileRedstoneReceiver>, WirelessRedstoneBlock, IPlatformConnectRedstone {
    companion object {
        const val id = "builder/redstone_receiver"
    }
    
    //region Setup
    init {
        registerDefaultState(stateDefinition.any().with(POWERED, false))
    }
    
    override fun createBlockStateDefinition(pBuilder: StateDefinition.Builder<Block, BlockState>) {
        super.createBlockStateDefinition(pBuilder)
        pBuilder.add(POWERED)
    }
    
    override fun getEntityType() = ModBlocks_Builder.REDSTONE_RECEIVER_ENTITY
    //endregion
    
    //region Placement
    override fun setPlacedBy(
        pLevel: Level,
        pPos: BlockPos,
        pState: BlockState,
        pPlacer: LivingEntity?,
        pStack: ItemStack
    ) {
        super.setPlacedBy(pLevel, pPos, pState, pPlacer, pStack)
        
        if (pStack.item !== ModBlocks_Builder.REDSTONE_RECEIVER_ITEM)
            return
        
        if (!pLevel.isClientSide) {
            pLevel.getOurEntity(pPos)?.let {
                val redstoneHandler = (pLevel as ServerLevel).dataStorage.dungeonBuilderData.redstoneHandler
                it.setChannel(redstoneHandler.registerWirelessReceiver(IWirelessRedstone.getFromStack(pStack)?.channel ?: NO_CHANNEL, pPos))
                if (redstoneHandler.isChannelPowered(it.channel)) {
                    pLevel.setBlockAndUpdate(pPos, pState.with(POWERED, true))
                }
            } ?: return MOD_LOGGER.error("No block entity found at pos $pPos!", Throwable())

        }
    }
    
    override fun useWithoutItem(pState: BlockState, pLevel: Level, pPos: BlockPos, pPlayer: Player, pHit: BlockHitResult): InteractionResult {
        super<WirelessRedstoneBlock>.onRightClick(pLevel, pPos, pPlayer)
        return super.useWithoutItem(pState, pLevel, pPos, pPlayer, pHit)
    }
    //endregion
    
    //region Redstone
    override fun canConnectRedstone(state: BlockState, level: BlockGetter, pos: BlockPos, direction: Direction?) = true
    
    override fun getSignal(pState: BlockState, pLevel: BlockGetter, pPos: BlockPos, pDirection: Direction): Int {
        return if (pState[POWERED]) 15 else 0
    }
    //endregion
    
    override fun asItem(): Item {
        return ModBlocks_Builder.REDSTONE_RECEIVER_ITEM
    }
}

class TileRedstoneReceiver(pPos: BlockPos, pState: BlockState, val redstone: IWirelessRedstone.Mutable = IWirelessRedstone.Mutable(NO_CHANNEL))
    : BlockEntity(ModBlocks_Builder.REDSTONE_RECEIVER_ENTITY, pPos, pState), IWirelessRedstone by redstone
{
    override fun loadAdditional(input: ValueInput) {
        super.loadAdditional(input)
        input.readCodecSerializableToExisting("wireless_redstone", redstone)
    }
    
    override fun saveAdditional(output: ValueOutput) {
        super.saveAdditional(output)
        output.putCodecSerializable("wireless_redstone", redstone)
    }
    
    override fun applyImplicitComponents(componentGetter: DataComponentGetter) {
        super.applyImplicitComponents(componentGetter)
        val channel = level?.runOnServer {
             componentGetter.get(ModItemComponents.WIRELESS_REDSTONE)?.channel
                ?: dataStorage.dungeonBuilderData.redstoneHandler.makeNewChannel()
        }
        if (channel == null)
            return
        
        redstone.channel = channel
    }
    
    override fun collectImplicitComponents(components: DataComponentMap.Builder) {
        super.collectImplicitComponents(components)
        components.set(ModItemComponents.WIRELESS_REDSTONE, redstone)
    }
    
    override fun preRemoveSideEffects(pos: BlockPos, state: BlockState) {
        super.preRemoveSideEffects(pos, state)
        level?.actOnServerLevel {
            dataStorage.dungeonBuilderData.redstoneHandler.unregisterWirelessReceiver(channel, pos)
        }
    }
    
    fun setChannel(channel: Int) {
        this.redstone.channel = channel
        setChanged()
    }
}

/**
 * Allows us to right-click on a transmitter or receiver in the world and copy its channel
 */
class ItemBlockRedstoneReceiver(props: Properties) : BlockItem(ModBlocks_Builder.REDSTONE_RECEIVER, props) {
    
    @Suppress("DuplicatedCode")
    override fun useOn(pContext: UseOnContext): InteractionResult {
        val target = pContext.targetBlockEntity
        
        if (WirelessRedstoneItem.useOn(target, pContext)) {
            return sidedSuccess(pContext.isClientSide)
        }
        
        return super.useOn(pContext)
    }
    
    override fun placeBlock(pContext: BlockPlaceContext, pState: BlockState): Boolean {
        return super.placeBlock(pContext, pState)
    }
    
    override fun appendHoverText(stack: ItemStack, context: TooltipContext, tooltipDisplay: TooltipDisplay, tooltipAdder: Consumer<Component>, flag: TooltipFlag) {
        super.appendHoverText(stack, context, tooltipDisplay, tooltipAdder, flag)
        WirelessRedstoneItem.appendHoverText(stack) { tooltipAdder(it) }
    }
}