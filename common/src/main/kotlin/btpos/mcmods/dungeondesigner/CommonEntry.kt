package btpos.mcmods.dungeondesigner

import btpos.mcmods.dungeondesigner.debugging.DebugCommands
import btpos.mcmods.dungeondesigner.registry.ModBlocks_Builder
import btpos.mcmods.dungeondesigner.registry.ModCreativeTabs
import btpos.mcmods.dungeondesigner.registry.ModItemComponents
import btpos.mcmods.dungeondesigner.registry.ModItems
import com.mojang.brigadier.CommandDispatcher
import dev.architectury.event.events.common.CommandRegistrationEvent
import net.minecraft.commands.CommandBuildContext
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

const val MODID = "dungeondesigner"
val MOD_LOGGER: Logger = LogManager.getLogger(MODID)

object CommonEntry {
    fun init() {
        MOD_LOGGER.log(Level.INFO, "$MODID has started!")
        
        CommandRegistrationEvent.EVENT.register(::registerCommands)
        ModBlocks_Builder.register()
        ModItems.register()
        ModItemComponents.register()
        ModCreativeTabs.register()
    }
    
    fun registerCommands(dispatcher: CommandDispatcher<CommandSourceStack>, registry: CommandBuildContext, selection: Commands.CommandSelection) {
        dispatcher.register(DebugCommands.make())
    }
}