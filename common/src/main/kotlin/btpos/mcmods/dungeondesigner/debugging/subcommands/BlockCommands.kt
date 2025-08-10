package btpos.mcmods.dungeondesigner.debugging.subcommands

import btpos.mcmods.devutil.common.macros.brigadier.literal
import btpos.mcmods.devutil.common.ext.vanilla.asComponent
import btpos.mcmods.devutil.common.ext.vanilla.plus
import btpos.mcmods.devutil.common.ext.vanilla.world.blockEntity
import btpos.mcmods.devutil.common.ext.vanilla.world.with
import btpos.mcmods.devutil.common.macros.ChatUtils.toComponent
import btpos.mcmods.dungeondesigner.builder.blocks.actors.BlockFightController
import btpos.mcmods.dungeondesigner.builder.blocks.actors.FightStatus
import btpos.mcmods.dungeondesigner.builder.nbt.toComponent
import btpos.mcmods.dungeondesigner.debugging.CommandBuilder
import btpos.mcmods.dungeondesigner.debugging.CommandContext
import btpos.mcmods.dungeondesigner.debugging.CommandHandler
import btpos.mcmods.dungeondesigner.registry.ModBlocks_Builder
import net.minecraft.ChatFormatting
import net.minecraft.core.BlockPos
import net.minecraft.network.chat.Component
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.HitResult

object BlockCommands : CommandHandler {
    fun make(): CommandBuilder {
        return literal("block") {
            "fight" {
                "get_pipettes" {
                    executes { ctx ->
                       val pos = getLookedAtPos(ctx) ?: return@executes 0
                        val ent = ctx.source.level.blockEntity(pos, ModBlocks_Builder.FIGHT_CONTROLLER_ENTITY) ?: return@executes ctx.sendFailure("No fight controller at pos $pos".asComponent())
                        
                        val data = ent.getPipetteData() ?: return@executes ctx.sendFailure("No pipette data found for entity at $pos.".asComponent())
                        
                        ctx.sendSuccess({ val map = data.map { it.toComponent() }
                            println(map.size)
                            map.fold(Component.literal("")) { x, y -> x.append("\n").append(y) } })
                        return@executes 1
                    }
                }
                "trigger" {
                    executes { ctx ->
                        val pos = getLookedAtPos(ctx) ?: return@executes 0
                        val ent = ctx.source.level.blockEntity(pos, ModBlocks_Builder.FIGHT_CONTROLLER_ENTITY) ?: return@executes ctx.sendFailure("No fight controller at pos $pos".asComponent())
                        
                        ent.startFight()
                        
                        return@executes 1
                    }
                }
                "reset" {
                    executes { ctx ->
                        val pos = getLookedAtPos(ctx) ?: return@executes ctx.sendFailure("Not looking at block.")
                        val block = ctx.source.level.getBlockState(pos).also {
                            if (!it.`is`(ModBlocks_Builder.FIGHT_CONTROLLER)) {
                                return@executes ctx.sendFailure("No fight controller at pos $pos")
                            }
                        }
                        ctx.source.level.setBlockAndUpdate(pos, block.with(BlockFightController.STATUS, FightStatus.INACTIVE))
                        return@executes ctx.sendSuccess({"Set block at pos ".asComponent() + pos.toComponent().withStyle(ChatFormatting.YELLOW) + " to status: inactive." })
                    }
                }
            }
        }
    }
    
    
    fun getLookedAtPos(ctx: CommandContext): BlockPos? {
        val lookedAtBlock = ctx.source.playerOrException.pick(10.0, 1.0f, false)
        if (lookedAtBlock.type != HitResult.Type.BLOCK) {
            ctx.source.sendFailure("No block found".asComponent())
            return null
        }
        return (lookedAtBlock as? BlockHitResult)?.blockPos ?: run {
            ctx.sendFailure("No block found".asComponent())
            return null
        }
    }
}