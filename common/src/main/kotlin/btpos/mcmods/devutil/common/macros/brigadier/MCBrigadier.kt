@file:Suppress("NOTHING_TO_INLINE")

package btpos.mcmods.devutil.common.macros.brigadier

import btpos.mcmods.devutil.common.dsl.brigadier.ArgBuilder
import btpos.mcmods.devutil.common.dsl.brigadier.Command
import btpos.mcmods.devutil.common.dsl.brigadier.LiteralBuilder
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.builder.RequiredArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import net.minecraft.commands.CommandSourceStack
import net.minecraft.network.chat.Component

/**
 * LiteralArgumentBuilder<CommandSourceStack>
 */
typealias CommandLiteral = LiteralArgumentBuilder<CommandSourceStack>
/**
 * RequiredArgumentBuilder<CommandSourceStack, T>
 */
typealias CommandArgument<T> = RequiredArgumentBuilder<CommandSourceStack, T>
/**
 * CommandContext<CommandSourceStack>
 */
typealias CommandContext = CommandContext<CommandSourceStack>

/**
 * Macro for literal<CommandSourceStack>()
 */
inline fun literal(name: String, crossinline then: LiteralBuilder<CommandSourceStack>.() -> Unit): CommandLiteral =
    Command.literal(name, then)
inline fun <T> argument(name: String, type: ArgumentType<T>, crossinline then: ArgBuilder<CommandSourceStack, T>.() -> Unit) =
    Command.argument(name, type, then)

/**
 * Macro for sending a success and returning from a Command in the same line.
 *
 * Example: `return ctx.sendSuccess({Component.literal("foo")})`
 */
inline fun CommandContext<CommandSourceStack>.sendSuccess(noinline msg: () -> Component, allowLogging: Boolean = true): Int {
    source.sendSuccess(msg, allowLogging)
    return 1
}

/**
 * Macro for sending a failure and returning in the same line.
 */
inline fun CommandContext<CommandSourceStack>.sendFailure(msg: Component): Int {
    source.sendFailure(msg)
    return 0
}