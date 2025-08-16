@file:Suppress("NOTHING_TO_INLINE", "unused")

package btpos.mcmods.devutil.common.dsl.brigadier

import btpos.mcmods.devutil.common.dsl.brigadier.Command.literal
import com.mojang.brigadier.Command
import com.mojang.brigadier.RedirectModifier
import com.mojang.brigadier.SingleRedirectModifier
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.builder.ArgumentBuilder
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.builder.RequiredArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import com.mojang.brigadier.tree.ArgumentCommandNode
import com.mojang.brigadier.tree.CommandNode
import com.mojang.brigadier.tree.LiteralCommandNode
import java.util.concurrent.CompletableFuture
import java.util.function.Predicate

object Command {
    /**
     * Start a command tree with the DSL builder.
     *
     * Example usage:
     * ```kotlin
     * // Start the command tree
     * literal<CommandSourceStack>("find") {
     *     // Add a literal. Command at this point is "/find my"
     *     "my" {
     *         // Add a "string" argument with the name "item_arg"
     *         "item_arg"(StringArgumentType.string()) {
     *             requires { it.isPlayer }
     *             // Add suggestions. The ! operator adds a literal, and the invoke adds a literal with a tooltip.
     *             suggests { ctx ->
     *                 !"ball"
     *                 !1
     *                 "car" { "This will be a tooltip!" }
     *                 2 { "This will be another tooltip!" }
     *                 withOffset(5) {
     *                     "future" { "Using knowledge of later words!" }
     *                 }
     *             }
     *             executes { ctx ->
     *                 val toFind = ctx.optionalArgument<String>("item_arg")
     *                     ?: return@executes ctx.sendFailure("You didn't ask for an item!")
     *
     *                 if (toFind == "car")
     *                     return@executes ctx.sendSuccess("yay!")
     *
     *                 return@executes 0
     *             }
     *             "here" {
     *                 executes(::myCommand)
     *             }
     *             +cachedCommandBuilder
     *         }
     *     }
     * }
     * ```
     *
     * Aliases for [LiteralArgumentBuilder.literal]:
     * ```kotlin
     * literal("foo") { }
     * "bar" { }
     * ```
     *
     * Aliases for [RequiredArgumentBuilder.argument]:
     * ```kotlin
     * argument("name", StringArgumentType.word()) { }
     * arg("name", StringArgumentType.string()) { }
     * BoolArgumentType.bool()("name") { }
     * "name"(BoolArgumentType.bool()) { }
     * ```
     */
    inline fun <T> literal(name: String, crossinline then: LiteralBuilder<T>.() -> Unit): LiteralArgumentBuilder<T> {
        return LiteralBuilder(LiteralArgumentBuilder.literal<T>(name)).apply(then).internal
    }
    
    
    /**
     * Start an argument node.  You shouldn't be giving this to a dispatcher, instead using this to create reusable argument trees.
     *
     * See [literal] for DSL usage.
     */
    inline fun <SOURCE, T> argument(
        name: String,
        type: ArgumentType<T>,
        crossinline then: ArgBuilder<SOURCE, T>.() -> Unit
    ): RequiredArgumentBuilder<SOURCE, T> {
        return ArgBuilder(RequiredArgumentBuilder.argument<SOURCE, T>(name, type)).apply(then).internal
    }
    
    /**
     * Returned from a [LiteralBuilder.executes] or [ArgBuilder.executes] block if the command was a success.
     */
    const val SUCCESS = 1
    const val FAIL = 0
}

inline fun <reified T : Any> CommandContext<*>.optionalArgument(name: String): T? {
    return try {
        this.getArgument(name, T::class.java)
    } catch (_: Exception) {
        null
    }
}


@DslMarker
internal annotation class BrigadierDSL

@JvmInline
@BrigadierDSL
value class ArgBuilder<S, T>(val internal: RequiredArgumentBuilder<S, T>) {
    /**
     * Add an argument branch.  See the subclasses of [ArgumentType] for more information on types.
     */
    inline fun <T> argument(name: String, type: ArgumentType<T>, crossinline then: ArgBuilder<S, T>.() -> Unit) {
        val it = ArgBuilder<S, T>(RequiredArgumentBuilder.argument(name, type)).apply(then)
        internal.then(it.internal)
    }
    
    /**
     * Add an argument branch.  See the subclasses of [ArgumentType] for more information on types.
     */
    inline fun <T> arg(name: String, type: ArgumentType<T>, crossinline then: ArgBuilder<S, T>.() -> Unit) =
        argument(name, type, then)
    
    inline operator fun <T> ArgumentType<T>.invoke(name: String, crossinline then: ArgBuilder<S, T>.() -> Unit) =
        argument(name, this, then)
    
    inline operator fun <T> String.invoke(arg: ArgumentType<T>, crossinline then: ArgBuilder<S, T>.() -> Unit) =
        argument(this, arg, then)
    
    /**
     * Create a command branch off of this node accessed via this string literal.
     *
     * @see literal
     */
    inline operator fun String.invoke(crossinline then: LiteralBuilder<S>.() -> Unit) = literal(this, then)
    
    /**
     * Start a new branch (or continue the only branch) with a literal word as the designator.
     *
     * For example: `/foo bar` - a command `foo` with a subcommand `bar` - could be `literal("foo") { literal("bar") { ... } }`
     */
    inline fun literal(name: String, crossinline then: LiteralBuilder<S>.() -> Unit) {
        val it = LiteralBuilder<S>(LiteralArgumentBuilder.literal(name)).apply(then)
        internal.then(it.internal)
    }
    
    /**
     * Append a branching execution path to this tree from a precompiled node.
     */
    inline fun then(argument: CommandNode<S>) {
        internal.then(argument)
    }
    
    /**
     * Append a branching execution path to this tree.
     *
     * Called implicitly with "argument(...) {  }" or "literal(...) {  }" for neater writing, but these still exist in case you need them for any reason.
     */
    inline fun then(argument: ArgumentBuilder<S, *>) {
        internal.then(argument)
    }
    
    /**
     * Appends this command branch to the tree.
     */
    inline operator fun ArgumentBuilder<S, *>.unaryPlus() = this@ArgBuilder.then(this)
    
    /**
     * Appends this precompiled command branch to the tree.
     */
    inline operator fun CommandNode<S>.unaryPlus() = then(this)
    
    /**
     * Directs execution to the given target node (acquired with [ArgumentBuilder.build]) when parsed, applying the provided modifier to the command context before proceeding to the redirect target.
     *
     * I _think_ it's used for things like adding optional arguments to the front of a command, like `/dothing thisway x y z` instead of `/dothing x y z`.
     *
     * For example: you turn `x y z` into a commandnode, and have `/dothing x y z` be a redirect from `literal("dothing")` to the command node for `x y z`, with the modifier being a default value for "`thisway`".
     */
    inline fun redirect(target: CommandNode<S>, modifier: SingleRedirectModifier<S>? = null) {
        internal.redirect(target, modifier)
    }
    
    /**
     * From Mojang's docs:
     *
     * > If the command passes through a node that is [CommandNode.isFork] then it will be 'forked'.
     * > A forked command will not bubble up any [com.mojang.brigadier.exceptions.CommandSyntaxException]s, and the 'result' returned will turn into
     * > 'amount of successful commands executes'."
     *
     * Essentially, forked nodes just don't propagate exceptions, and act like batch jobs executed in parallel.  As the name suggests, it's like forking a process.
     */
    inline fun fork(target: CommandNode<S>, modifier: RedirectModifier<S>) {
        internal.fork(target, modifier)
    }
    
    /**
     * Direct access to the internals of redirect and fork.
     */
    inline fun forward(target: CommandNode<S>, modifier: RedirectModifier<S>, fork: Boolean) {
        internal.forward(target, modifier, fork)
    }
    
    /**
     * The action this command path performs. Should return 1 on success and 0 on failure.
     *
     * (See Minecraft's `Command.SINGLE_SUCCESS`)
     */
    inline fun executes(command: Command<S>) {
        internal.executes(command)
    }
    
    /**
     * The action this command path performs. Should return 1 on success and 0 on failure.
     *
     * (See Minecraft's `Command.SINGLE_SUCCESS`)
     */
    inline fun executes(noinline command: (CommandContext<S>) -> Int) {
        internal.executes(command)
    }
    
    /**
     * Finalize this builder into a command node for reuse in other "then", "fork", "redirect", etc. calls.
     */
    inline fun build(): ArgumentCommandNode<S, T> {
        return internal.build()
    }
    
    /**
     * Sets a condition that the user of either this command or any of its subcommands must satisfy to use it.
     */
    inline fun requires(noinline condition: (S) -> Boolean) {
        internal.requires(condition)
    }
    
    /**
     * Sets a condition that the user of either this command or any of its subcommands must satisfy to use it.
     */
    inline fun requires(condition: Predicate<S>) {
        internal.requires(condition)
    }
    
    /**
     * Add suggestions that the user will see while typing this command.
     *
     * @see SuggestionsBuilderDSL
     */
    inline fun suggests(crossinline suggestionProvider: SuggestionsBuilderDSL.(CommandContext<S>) -> Unit) {
        internal.suggests { ctx, builder ->
            SuggestionsBuilderDSL(builder).apply {
                suggestionProvider(ctx)
            }.buildFuture()
        }
    }
    
    /**
     * From Mojang's documentation:
     * > The suggestions provided will be in the context of the end of the parsed input string, but may suggest new or replacement strings for earlier in the input string. For example, if the end of the string was foobar but an argument preferred it to be minecraft:foobar, it will suggest a replacement for that whole segment of the input.
     */
    @BrigadierDSL
    @JvmInline
    value class SuggestionsBuilderDSL(
        /**
         * The original builder. Left accessible for use in `for` loops or other things.
         *
         * @see plusAssign
         */
        val builder: SuggestionsBuilder
    ) {
        //region Properties
        /**
         * The entire string the user has already entered into the command line at this point in time.
         *
         * For example, if the command line reads `/foo bar`, the value of `input` would be "`foo bar`".
         */
        inline val input: String
            inline get() = builder.input
        
        /**
         * The portion of the full input string located after the cursor position.
         *
         * For example, if the command line reads `/foo bar b|az`, where `|` signifies the cursor position, the value of `remaining` would be "`az`".
         */
        inline val remaining: String
            inline get() = builder.remaining
        
        /**
         * The portion of the full input string located after the cursor position, but in lowercase.
         * @see remaining
         */
        inline val remainingLowerCase: String
            inline get() = builder.remainingLowerCase
        
        /**
         * The start index of the currently-selected word in [input].
         *
         * For example, if the command line reads `/foo bar b|a`, where the cursor position is signified with `|` and is located before the character at index 9 in the string, the value of `start` would be `8`: the index of the beginning of the partial word "`ba`", which is currently being hovered over.
         */
        inline val start: Int
            inline get() = builder.start
        //endregion
        
        //region Literal Suggestions
        /**
         * Adds a String to the outgoing list of suggestions.
         */
        inline fun suggest(suggestion: String) {
            builder.suggest(suggestion)
        }
        
        /**
         * Adds an integer to the outgoing list of suggestions.
         */
        inline fun suggest(suggestion: Int) {
            builder.suggest(suggestion)
        }
        
        /**
         * Adds a String to the outgoing list of suggestions.
         */
        inline operator fun String.invoke() {
            builder.suggest(this)
        }
        
        /**
         * Adds an integer to the outgoing list of suggestions.
         */
        inline operator fun Int.invoke() {
            builder.suggest(this)
        }
        
        /**
         * Adds a string to the outgoing list of suggestions.
         *
         * Why a `!`? Because `+` was already taken for Int, and only having it for String but not for Int would be confusing.
         */
        inline operator fun String.not() {
            builder.suggest(this)
        }
        /**
         * Adds an integer to the outgoing list of suggestions.
         *
         * Why a `!`? Because `+` was already taken for Int, and only having it for String but not for Int would be confusing.
         */
        inline operator fun Int.not() {
            builder.suggest(this)
        }
        //endregion
        
        //region With Tooltip
        /**
         * Adds a String to the suggestion list, with the supplied message appearing as a tooltip when this suggestion is hovered over.
         */
        inline operator fun String.invoke(noinline msg: () -> String) {
            builder.suggest(this, msg)
        }
        
        /**
         * Adds an integer to the suggestion list, with the supplied message appearing as a tooltip when this suggestion is hovered over.
         */
        inline operator fun Int.invoke(noinline msg: () -> String) {
            builder.suggest(this, msg)
        }
        //endregion
        
        //region PlusAssign
        /**
         * Add this value as a suggestion.
         */
        inline operator fun SuggestionsBuilder.plusAssign(suggestion: String) {
            builder.suggest(suggestion)
        }
        
        /**
         * Add this value as a suggestion.
         */
        inline operator fun SuggestionsBuilder.plusAssign(suggestion: Int) {
            builder.suggest(suggestion)
        }
        /**
         * Add all members of this [SuggestionsBuilder] as suggestions.
         */
        inline operator fun SuggestionsBuilder.plusAssign(otherBuilder: SuggestionsBuilder) {
            builder.add(otherBuilder)
        }
        //endregion
        
        /**
         * Starts a new [SuggestionsBuilder] (with DSL) at the provided index into the input string.
         */
        inline fun withOffset(newStart: Int, configuration: SuggestionsBuilderDSL.() -> Unit): SuggestionsBuilder {
            return SuggestionsBuilderDSL(builder.createOffset(newStart)).apply(configuration).builder
        }
        
        
        /**
         * For internal use only.
         */
        inline fun buildFuture(): CompletableFuture<Suggestions> {
            return builder.buildFuture()
        }
        
        /**
         * Compiles the suggestions in this builder into a static form. Not needed unless you wish to reuse this builder.
         */
        inline fun build(): Suggestions = builder.build()
        
        
    }
}

@JvmInline
@BrigadierDSL
value class LiteralBuilder<S>(val internal: LiteralArgumentBuilder<S>) {
    /**
     * Add an argument branch.  See the subclasses of [ArgumentType] for more information on types.
     */
    inline fun <T> argument(name: String, type: ArgumentType<T>, crossinline then: ArgBuilder<S, T>.() -> Unit) {
        val it = ArgBuilder<S, T>(RequiredArgumentBuilder.argument(name, type)).apply(then)
        internal.then(it.internal)
    }
    
    /**
     * Add an argument branch.  See the subclasses of [ArgumentType] for more information on types.
     */
    inline fun <T> arg(name: String, type: ArgumentType<T>, crossinline then: ArgBuilder<S, T>.() -> Unit) =
        argument(name, type, then)
    
    inline operator fun <T> ArgumentType<T>.invoke(name: String, crossinline then: ArgBuilder<S, T>.() -> Unit) =
        argument(name, this, then)
    
    inline operator fun <T> String.invoke(arg: ArgumentType<T>, crossinline then: ArgBuilder<S, T>.() -> Unit) =
        argument(this, arg, then)
    
    /**
     * Create a command branch off of this node accessed via this string literal.
     *
     * @see literal
     */
    inline operator fun String.invoke(crossinline then: LiteralBuilder<S>.() -> Unit) = literal(this, then)
    
    /**
     * Start a new branch (or continue the only branch) with a literal word as the designator.
     *
     * For example: `/foo bar` - a command `foo` with a subcommand `bar` - could be `literal("foo") { literal("bar") { ... } }`
     */
    inline fun literal(name: String, crossinline then: LiteralBuilder<S>.() -> Unit) {
        val it = LiteralBuilder<S>(LiteralArgumentBuilder.literal(name)).apply(then)
        internal.then(it.internal)
    }
    
    /**
     * Append a branching execution path to this tree from a precompiled node.
     */
    inline fun then(argument: CommandNode<S>) {
        internal.then(argument)
    }
    
    /**
     * Append a branching execution path to this tree.
     *
     * Called implicitly with "argument(...) {  }" or "literal(...) {  }" for neater writing, but these still exist in case you need them for any reason.
     */
    inline fun then(argument: ArgumentBuilder<S, *>) {
        internal.then(argument)
    }
    
    /**
     * Appends this command branch to the tree.
     */
    inline operator fun ArgumentBuilder<S, *>.unaryPlus() = this@LiteralBuilder.then(this)
    
    /**
     * Appends this precompiled command branch to the tree.
     */
    inline operator fun CommandNode<S>.unaryPlus() = then(this)
    
    /**
     * Directs execution to the given target node (acquired with [ArgumentBuilder.build]) when parsed, applying the provided modifier to the command context before proceeding to the redirect target.
     *
     * I _think_ it's used for things like adding optional arguments to the front of a command, like `/dothing thisway x y z` instead of `/dothing x y z`.
     *
     * For example: you turn `x y z` into a commandnode, and have `/dothing x y z` be a redirect from `literal("dothing")` to the command node for `x y z`, with the modifier being a default value for "`thisway`".
     */
    inline fun redirect(target: CommandNode<S>, modifier: SingleRedirectModifier<S>? = null) {
        internal.redirect(target, modifier)
    }
    
    /**
     * From Mojang's docs:
     *
     * > If the command passes through a node that is [CommandNode.isFork] then it will be 'forked'.
     * > A forked command will not bubble up any [com.mojang.brigadier.exceptions.CommandSyntaxException]s, and the 'result' returned will turn into
     * > 'amount of successful commands executes'."
     *
     * Essentially, forked nodes just don't propagate exceptions, and act like batch jobs executed in parallel.  As the name suggests, it's like forking a process.
     */
    inline fun fork(target: CommandNode<S>, modifier: RedirectModifier<S>) {
        internal.fork(target, modifier)
    }
    
    /**
     * Direct access to the internals of redirect and fork.
     */
    inline fun forward(target: CommandNode<S>, modifier: RedirectModifier<S>, fork: Boolean) {
        internal.forward(target, modifier, fork)
    }
    
    /**
     * The action this command path performs. Should return 1 on success and 0 on failure.
     *
     * (See Minecraft's `Command.SINGLE_SUCCESS`)
     */
    inline fun executes(command: Command<S>) {
        internal.executes(command)
    }
    
    /**
     * The action this command path performs. Should return 1 on success and 0 on failure.
     *
     * (See Minecraft's `Command.SINGLE_SUCCESS`)
     */
    inline fun executes(noinline command: (CommandContext<S>) -> Int) {
        internal.executes(command)
    }
    
    /**
     * Finalize this builder into a command node for reuse in other "then", "fork", "redirect", etc. calls.
     */
    inline fun build(): LiteralCommandNode<S> {
        return internal.build()
    }
    
    /**
     * Sets a condition that the user of either this command or any of its subcommands must satisfy to use it.
     */
    inline fun requires(noinline condition: (S) -> Boolean) {
        internal.requires(condition)
    }
    
    /**
     * Sets a condition that the user of either this command or any of its subcommands must satisfy to use it.
     */
    inline fun requires(condition: Predicate<S>) {
        internal.requires(condition)
    }
}

