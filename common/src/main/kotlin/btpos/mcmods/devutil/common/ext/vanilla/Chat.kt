@file:Suppress("NOTHING_TO_INLINE")

package btpos.mcmods.devutil.common.ext.vanilla

import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.player.Player

inline fun String?.asComponent() = Component.literal(this ?: "null")

inline operator fun MutableComponent.plus(next: Component): MutableComponent = this.append(next)
inline operator fun MutableComponent.plus(next: String): MutableComponent = this.append(Component.literal(next))

inline fun Player.sendSystemMessage(message: Component, onActionBar: Boolean = false) {
	this.displayClientMessage(message, onActionBar)
}