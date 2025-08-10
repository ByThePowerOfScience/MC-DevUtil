@file:Suppress("OVERRIDE_DEPRECATION", "DEPRECATION")

package btpos.mcmods.dungeondesigner.builder.items

import btpos.mcmods.devutil.common.ext.java.invoke
import btpos.mcmods.devutil.common.ext.vanilla.asComponent
import btpos.mcmods.devutil.common.ext.vanilla.data.nullSafeFieldOf
import btpos.mcmods.devutil.common.ext.vanilla.plus
import btpos.mcmods.devutil.common.ext.vanilla.sendSystemMessage
import btpos.mcmods.devutil.common.ext.vanilla.world.actOnServer
import btpos.mcmods.devutil.common.macros.ChatUtils.toComponent
import btpos.mcmods.dungeondesigner.registry.ModItemComponents
import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.ChatFormatting
import net.minecraft.core.BlockPos
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
 * Draws a trigger in the world.
 *
 * NBT: {
 *      "trigger_brush": [TriggerBounds]
 * }
 */
class ItemTriggerVariable(props: Properties) : Item(props) {
	companion object {
		const val id = "builder/trigger_variable"
		
		// NBT Tag Keys
		const val TAGKEY_STATE = "trigger_brush"
		
//		override fun ItemModelProvider.buildModels() {
//			this.basicItem()
//		}
		
		
		fun getData(stack: ItemStack): InternalData? {
			return stack.get(ModItemComponents.TRIGGER_VARIABLE_DATA)
//			return stack.tag?.let(::getTriggerBoundsNbt)?.let(::NbtAdapter)
		}
		
		/**
		 * Functional transformation of immutable objects, except it's janky and bad because I'm tired
		 */
		inline fun modifyOrCreateData(stack: ItemStack, mutator: InternalData.Mutable.() -> Unit) {
			val current: InternalData.Mutable = stack.get(ModItemComponents.TRIGGER_VARIABLE_DATA)?.let(InternalData::Mutable) ?: InternalData.Mutable()
			current.mutator()
			stack.set(ModItemComponents.TRIGGER_VARIABLE_DATA, current)
		}
	}
	
	
	
	override fun useOn(ctx: UseOnContext): InteractionResult {
		return whenUsedOnBlock(ctx)
	}
	
	fun whenUsedOnBlock(ctx: UseOnContext): InteractionResult {
		val player = ctx.player ?: return InteractionResult.FAIL
		
		return ctx.level.actOnServer {
			if (!player.isShiftKeyDown) {
				modifyOrCreateData(ctx.itemInHand) {
					first = ctx.clickedPos
				}
				player.sendSystemMessage(
					Component.literal("Set first corner to ")
						.append(ctx.clickedPos.toComponent().withStyle(ChatFormatting.YELLOW))
				)
			} else {
				modifyOrCreateData(ctx.itemInHand) {
					second = ctx.clickedPos
				}
				player.sendSystemMessage(
					Component.literal("Set second corner to ")
						.append(ctx.clickedPos.toComponent().withStyle(ChatFormatting.YELLOW))
				)
			}
		}.sidedSuccess
	}
	
	override fun appendHoverText(pStack: ItemStack, context: TooltipContext, tooltipDisplay: TooltipDisplay, tooltipAdder: Consumer<Component>, pIsAdvanced: TooltipFlag) {
		super.appendHoverText(pStack, context, tooltipDisplay, tooltipAdder, pIsAdvanced)
		getData(pStack)?.run {
			first?.let { tooltipAdder("First corner: ".asComponent() + it.toComponent()) }
			second?.let { tooltipAdder("Second corner: ".asComponent() + it.toComponent()) }
		}
	}
	
//	/**
//	 * Wrapper giving managed structural access to a CompoundTag, because Items + Codecs = unfun.
//	 */
//	@JvmInline
//	value class NbtAdapter(val tag: CompoundTag) : InternalData {
//		override var first: BlockPos?
//			get() = tag.getBlockPos("first")
//			set(value) {
//				if (value == null)
//					tag.remove("first")
//				else
//					tag.put("first", value.toCompoundTag())
//			}
//
//		override var second: BlockPos?
//			get() = tag.getBlockPos("second")
//			set(value) {
//				if (value == null)
//					tag.remove("second")
//				else
//					tag.put("second", value.toCompoundTag())
//			}
//	}
	
	interface InternalData {
		val first: BlockPos?
		val second: BlockPos?
		
		fun isComplete(): Boolean {
			return first != null && second != null
		}
		
		
		companion object {
			val CODEC: Codec<InternalData> = RecordCodecBuilder.create {
				it.group(
						BlockPos.CODEC.nullSafeFieldOf("first", InternalData::first),
						BlockPos.CODEC.nullSafeFieldOf("second", InternalData::second),
				).apply(it) { i, j ->
					Mutable(i.getOrNull(), j.getOrNull())
				}
			}
			
			operator fun invoke(first: BlockPos? = null, second: BlockPos? = null): InternalData {
				return Impl(first, second)
			}
		}
		
		data class Mutable(override var first: BlockPos? = null, override var second: BlockPos? = null) : InternalData {
			constructor(other: InternalData) : this(other.first, other.second)
		}
		data class Impl(override val first: BlockPos?, override val second: BlockPos?) : InternalData
	}
}
