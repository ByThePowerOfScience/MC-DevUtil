@file:Suppress("NOTHING_TO_INLINE")

package btpos.mcmods.devutil.common.macros

import net.minecraft.ChatFormatting
import net.minecraft.core.Vec3i
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.ComponentContents
import net.minecraft.network.chat.MutableComponent
import net.minecraft.network.chat.Style
import net.minecraft.world.phys.Vec3

object ChatUtils {
	
	fun Vec3i?.toComponent(): MutableComponent {
		if (this == null) {
			return Component.literal("[null]")
		}
		return Component.literal("[$x, $y, $z]")
	}
    
    //region unaryMinus String to Component Macros
    inline operator fun String.unaryMinus(): MutableComponent = Component.literal(this)
	@JvmName("stringMinusNullable")
	inline operator fun String?.unaryMinus(): MutableComponent = this?.let(Component::literal) ?: Component.literal("[null]")
    //endregion
    
    //region Style Macros
    inline operator fun MutableComponent.get(formatting: ChatFormatting): MutableComponent = this.withStyle(formatting)
	inline operator fun MutableComponent.get(vararg formatting: ChatFormatting): MutableComponent = this.withStyle(*formatting)
	inline operator fun MutableComponent.get(formatting: Style): MutableComponent = this.withStyle(formatting)
	inline operator fun MutableComponent.invoke(noinline styleMapper: (Style) -> Style): MutableComponent = this.withStyle(styleMapper)
	
	// For when the unaryMinus doesn't mesh with the color
	inline infix fun MutableComponent.style(formatting: ChatFormatting): MutableComponent = this.withStyle(formatting)
	
	// I wonder what the operator precedence is. For example, -"foo"[BLUE] doesn't work, nor does -("foo"[BLUE])
	// oh maybe I could even skip the unaryMinus altogether since it's implied that String.get(ChatFormatting) would OBVIOUSLY be a component
	// oh duh of course -("foo"[BLUE]) doesn't work. it would have to be (-"foo")[BLUE], which still doesn't look too bad
	
	inline operator fun String.get(formatting: ChatFormatting): MutableComponent = (-this).withStyle(formatting)
	@JvmName("stringGetNullable")
	inline operator fun String?.get(formatting: ChatFormatting): MutableComponent = (-this).withStyle(formatting)
    //endregion
}