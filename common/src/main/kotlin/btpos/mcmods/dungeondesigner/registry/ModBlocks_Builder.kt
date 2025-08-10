package btpos.mcmods.dungeondesigner.registry

import btpos.mcmods.devutil.common.registry.BlockEntityType
import btpos.mcmods.devutil.common.registry.IBlockRegistry
import btpos.mcmods.dungeondesigner.builder.blocks.BlockDungeonNexus
import btpos.mcmods.dungeondesigner.builder.blocks.actors.BlockTriggerHolder
import btpos.mcmods.dungeondesigner.builder.blocks.TileDungeonNexus
import btpos.mcmods.dungeondesigner.builder.blocks.actors.BlockFightController
import btpos.mcmods.dungeondesigner.builder.blocks.actors.BlockFlagReader
import btpos.mcmods.dungeondesigner.builder.blocks.actors.BlockFlagWriter
import btpos.mcmods.dungeondesigner.builder.blocks.actors.TileFightController
import btpos.mcmods.dungeondesigner.builder.blocks.actors.TileFlagHolder
import btpos.mcmods.dungeondesigner.builder.blocks.actors.TileTriggerHolder
import btpos.mcmods.dungeondesigner.builder.redstone.blocks.BlockRedstoneReceiver
import btpos.mcmods.dungeondesigner.builder.redstone.blocks.BlockRedstoneTransmitter
import btpos.mcmods.dungeondesigner.builder.redstone.blocks.ItemBlockRedstoneReceiver
import btpos.mcmods.dungeondesigner.builder.redstone.blocks.ItemBlockRedstoneTransmitter
import btpos.mcmods.dungeondesigner.builder.redstone.blocks.TileRedstoneReceiver
import btpos.mcmods.dungeondesigner.builder.redstone.blocks.TileRedstoneTransmitter
import net.minecraft.core.registries.Registries
import net.minecraft.world.item.Item
import net.minecraft.world.level.block.state.BlockBehaviour
import net.minecraft.world.level.material.MapColor
import net.minecraft.world.level.material.PushReaction


object ModBlocks_Builder : IBlockRegistry {
	override val BLOCKS = createRegistry(Registries.BLOCK)
	override val ITEMS = createRegistry(Registries.ITEM)
	override val ENTITIES = createRegistry(Registries.BLOCK_ENTITY_TYPE)
	
	val logicBlockProperties = { BlockBehaviour.Properties.of()
		.pushReaction(PushReaction.IGNORE)
		.mapColor(MapColor.COLOR_MAGENTA)
		.strength(-1.0F, 3600000.0F)
		.noLootTable()
		.isValidSpawn { _, _, _, _ -> false } }
	
	val logicItemProperties = { Item.Properties().fireResistant() }
	
	
	
	
	val DUNGEON_NEXUS by block(BlockDungeonNexus.id, logicBlockProperties, withItem=true) { BlockDungeonNexus(it) }
	val DUNGEON_NEXUS_ENTITY by ent(::DUNGEON_NEXUS, ::TileDungeonNexus)
	
	val TRIGGER_BLOCK by block(BlockTriggerHolder.id, logicBlockProperties, withItem=true) { BlockTriggerHolder(it) }
	val TRIGGER_BLOCK_ENTITY by ent(::TRIGGER_BLOCK, ::TileTriggerHolder)
    
    //region Flag Blocks
    /**
	 * Emits signal when the flag is on
	 */
	val FLAG_READER by block("flag_reader", logicBlockProperties, withItem=true) { BlockFlagReader(it) }
	
	/**
	 * Turns the flag ON
	 */
	val FLAG_SETTER by block(BlockFlagWriter.id_setter, logicBlockProperties, withItem=true) { BlockFlagWriter(it, true) }
	
	/**
	 * Turns the flag OFF
	 */
	val FLAG_RESETTER by block(BlockFlagWriter.id_resetter, logicBlockProperties, withItem=true) { BlockFlagWriter(it, false) }
	
	val FLAG_BLOCK_ENTITY by ent("flag_block") { BlockEntityType(::TileFlagHolder, FLAG_READER, FLAG_SETTER, FLAG_RESETTER) }
    //endregion
	
	
	
	val FIGHT_CONTROLLER by block(BlockFightController.id, logicBlockProperties, withItem=true) { BlockFightController(it) }
	val FIGHT_CONTROLLER_ENTITY by ent(::FIGHT_CONTROLLER, ::TileFightController)
	
	
	
	val REDSTONE_RECEIVER by block(BlockRedstoneReceiver.id, logicBlockProperties, withItem=false) { BlockRedstoneReceiver(it) }
	val REDSTONE_RECEIVER_ENTITY by ent(::REDSTONE_RECEIVER, ::TileRedstoneReceiver)
	val REDSTONE_RECEIVER_ITEM by item(::REDSTONE_RECEIVER, logicItemProperties) { ItemBlockRedstoneReceiver(it) }
	
	val REDSTONE_TRANSMITTER by block(BlockRedstoneTransmitter.id, logicBlockProperties, withItem=false) { BlockRedstoneTransmitter(it) }
	val REDSTONE_TRANSMITTER_ENTITY by ent(::REDSTONE_TRANSMITTER, ::TileRedstoneTransmitter)
	val REDSTONE_TRANSMITTER_ITEM by item(::REDSTONE_TRANSMITTER, logicItemProperties) { ItemBlockRedstoneTransmitter(it) }
}