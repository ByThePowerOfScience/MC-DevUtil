@file:Suppress("OVERRIDE_DEPRECATION", "DEPRECATION")

package btpos.mcmods.dungeondesigner.builder.redstone.items

import btpos.mcmods.devutil.common.ext.java.invoke
import btpos.mcmods.devutil.common.ext.vanilla.asComponent
import btpos.mcmods.devutil.common.ext.vanilla.data.getBlockPos
import btpos.mcmods.devutil.common.ext.vanilla.data.nullSafeFieldOf
import btpos.mcmods.devutil.common.ext.vanilla.data.setOrRemove
import btpos.mcmods.devutil.common.ext.vanilla.data.toCompoundTag
import btpos.mcmods.devutil.common.ext.vanilla.plus
import btpos.mcmods.devutil.common.ext.vanilla.sendSystemMessage
import btpos.mcmods.devutil.common.ext.vanilla.world.actOnServer
import btpos.mcmods.devutil.common.macros.ChatUtils.toComponent
import btpos.mcmods.dungeondesigner.registry.ModItemComponents
import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.core.BlockPos
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.chat.Component
import net.minecraft.world.InteractionResult
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.TooltipFlag
import net.minecraft.world.item.component.TooltipDisplay
import net.minecraft.world.item.context.UseOnContext
import java.util.function.Consumer
import kotlin.jvm.optionals.getOrNull

/**
 * This is an item that allows you to link a transmitter to an arbitrary redstone component in the world
 * In reality it just adds that "thing" to the channel
 *
 * Use a chest on top of it like usual
 */
class ItemRemoteLinker(props: Properties) : Item(props) {
    companion object {
        fun getData(stack: ItemStack): InternalData? {
            return stack.get(ModItemComponents.REMOTE_LINKER)
        }
        
        inline fun modifyOrCreateData(stack: ItemStack, mutator: InternalData.Mutable.() -> Unit) {
            val current = stack.get(ModItemComponents.REMOTE_LINKER)?.let(InternalData::Mutable) ?: InternalData.Mutable()
            current.mutator()
            stack.set(ModItemComponents.REMOTE_LINKER, current)
        }
        
//        override fun ItemModelProvider.buildModels() {
//            basicItem()
//        }
        
        const val id = "builder/remote_linker"
    }
    
    @JvmInline
    value class NbtAdapter(val tag: CompoundTag) {
        var target: BlockPos?
            get() = tag.getBlockPos("target")
            set(value) = tag.setOrRemove("target", value) { put("target", it.toCompoundTag()) }
    }
    
    override fun useOn(pContext: UseOnContext): InteractionResult {
        val player = pContext.player ?: return InteractionResult.PASS
        
        // Add target pos
        return pContext.level.actOnServer {
            val targetPos = pContext.clickedPos
            modifyOrCreateData(pContext.itemInHand) {
                target = targetPos
            }
            player.sendSystemMessage("Added pos: ".asComponent() + targetPos.toComponent())
        }.sidedSuccess
    }
    
    override fun appendHoverText(stack: ItemStack, context: TooltipContext, tooltipDisplay: TooltipDisplay, tooltipAdder: Consumer<Component?>, flag: TooltipFlag) {
        super.appendHoverText(stack, context, tooltipDisplay, tooltipAdder, flag)
        val pos = getData(stack)?.target ?: return
        tooltipAdder("Target: ".asComponent() + pos.toComponent())
    }
    
    interface InternalData {
        val target: BlockPos?
        
        data class Impl(override val target: BlockPos?) : InternalData
        data class Mutable(override var target: BlockPos? = null) : InternalData {
            constructor(other: InternalData) : this(other.target)
        }
        
        companion object {
            val CODEC: Codec<InternalData> = RecordCodecBuilder.create { inst ->
	            inst.group(
                        BlockPos.CODEC.nullSafeFieldOf("target", InternalData::target)
                ).apply(inst) { Impl(it.getOrNull()) }
            }
        }
    }
}

