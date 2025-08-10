@file:Suppress("CONTEXT_RECEIVERS_DEPRECATED")

package btpos.mcmods.dungeondesigner.neoforge.datagen.models

import btpos.mcmods.devutil.common.ext.java.invoke
import btpos.mcmods.dungeondesigner.MODID
import btpos.mcmods.dungeondesigner.builder.blocks.actors.BlockFightController
import btpos.mcmods.dungeondesigner.builder.blocks.actors.BlockFlagReader
import btpos.mcmods.dungeondesigner.builder.blocks.actors.BlockFlagWriter
import btpos.mcmods.dungeondesigner.builder.blocks.actors.BlockTriggerHolder
import btpos.mcmods.dungeondesigner.builder.blocks.actors.FightStatus
import btpos.mcmods.dungeondesigner.builder.redstone.blocks.BlockRedstoneReceiver
import btpos.mcmods.dungeondesigner.builder.redstone.blocks.BlockRedstoneTransmitter
import btpos.mcmods.dungeondesigner.registry.ModBlocks_Builder
import btpos.mcmods.dungeondesigner.registry.ModItems
import net.minecraft.client.data.models.BlockModelGenerators
import net.minecraft.client.data.models.BlockModelGenerators.plainVariant
import net.minecraft.client.data.models.ItemModelGenerators
import net.minecraft.client.data.models.ModelProvider
import net.minecraft.client.data.models.MultiVariant
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator
import net.minecraft.client.data.models.blockstates.PropertyDispatch
import net.minecraft.client.data.models.model.ModelTemplate
import net.minecraft.client.data.models.model.ModelTemplates
import net.minecraft.client.data.models.model.TextureMapping
import net.minecraft.client.data.models.model.TextureSlot
import net.minecraft.client.renderer.block.model.VariantMutator
import net.minecraft.core.Direction
import net.minecraft.data.PackOutput
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.level.block.state.properties.BlockStateProperties
import net.minecraft.world.level.block.state.properties.Property

class ModelDataGen(output: PackOutput) : ModelProvider(output, MODID) {
	override fun registerModels(blockModels: BlockModelGenerators, itemModels: ItemModelGenerators) {
		blockModels.registerBlockModels()
		itemModels.registerItemModels()
	}
	
	private fun ItemModelGenerators.registerItemModels() {
		this.generateFlatItem(ModItems.REMOTE_LINKER, ModelTemplates.FLAT_ITEM)
		this.generateFlatItem(ModItems.FLAG_ITEM, ModelTemplates.FLAT_ITEM)
		this.generateFlatItem(ModItems.PIPETTE_ITEM, ModelTemplates.FLAT_ITEM)
		this.generateFlatItem(ModItems.TRIGGER_ITEM, ModelTemplates.FLAT_ITEM)
	}
	
	private fun BlockModelGenerators.registerBlockModels() {
		makeDungeonNexus()
		makeTriggerBlock()
		makeFightController()
		makeFlagReader()
		with (FlagWriters) {
			forSetter()
			forResetter()
		}
		with (WirelessRedstone) {
			forTransmitter()
			forReceiver()
		}
	}
	
	//region Block Models
	private fun BlockModelGenerators.makeDungeonNexus() {
		createTrivialCube(ModBlocks_Builder.DUNGEON_NEXUS)
	}
	
	private fun BlockModelGenerators.makeTriggerBlock() {
		val TEXTURE_TOP_BOTTOM = "logic_programmer_top"
		val TEXTURE_SIDES = "logic_programmer_side"
		val TEXTURE_TOP_BOTTOM_ON = "logic_programmer_top_on"
		val TEXTURE_SIDES_ON = "logic_programmer_side_on"
		
		val off = columnModel(BlockTriggerHolder.id.powered(false), blockLoc(TEXTURE_SIDES), blockLoc(TEXTURE_TOP_BOTTOM))
		val on = columnModel(BlockTriggerHolder.id.powered(true), blockLoc(TEXTURE_SIDES_ON), blockLoc(TEXTURE_TOP_BOTTOM_ON))
		
		MultiVariantGenerator.dispatch(ModBlocks_Builder.TRIGGER_BLOCK)
			.with(poweredInitialVariants(off, on))
			.submit()
		
		registerSimpleItemModel(ModBlocks_Builder.TRIGGER_BLOCK, off)
	}
	
	private fun BlockModelGenerators.makeFightController() {
		val id = BlockFightController.Companion.id
		val t_off = blockLoc("fight_controller/${id}_inactive")
		val out_off = blockLoc("fight_controller/${id}_inactive_out")
		
		val t_ip = blockLoc("fight_controller/${id}_ip")
		val out_ip = blockLoc("fight_controller/${id}_ip_out")
		
		val t_com = blockLoc("fight_controller/${id}_complete")
		val out_com = blockLoc("fight_controller/${id}_complete_out")
		
		val off = sidedBlock(id + "_inactive", t_off, t_off, out_off, t_off, t_off, t_off)
		val com = sidedBlock(id + "_complete", t_com, t_com, out_com, t_com, t_com, t_com)
		val in_progress = sidedBlock(id + "_ip", t_ip, t_ip, out_ip, t_ip, t_ip, t_ip)
		
		MultiVariantGenerator.dispatch(ModBlocks_Builder.FIGHT_CONTROLLER)
			.with(
					BlockFightController.STATUS(
							FightStatus.INACTIVE to plainVariant(off),
							FightStatus.IN_PROGRESS to plainVariant(in_progress),
							FightStatus.COMPLETE to plainVariant(com)
			)).with(ROTATION_HORIZONTAL_FACING).submit()
		
		registerSimpleItemModel(ModBlocks_Builder.FIGHT_CONTROLLER, off)
	}
	
	private fun BlockModelGenerators.makeFlagReader() {
		val off = sidedBlock(BlockFlagReader.id.powered(false), rest=blockLoc("flag/${BlockFlagReader.id.powered(false)}"))
		val on = sidedBlock(BlockFlagReader.id.powered(true), rest=blockLoc("flag/${BlockFlagReader.id.powered(true)}"))
		
		MultiVariantGenerator.dispatch(ModBlocks_Builder.FLAG_READER)
			.with(poweredInitialVariants(off, on))
			.submit()
		
		registerSimpleItemModel(ModBlocks_Builder.FLAG_READER, off)
	}
	
	@Suppress("DuplicatedCode")
	private object FlagWriters {
		fun getTexture(id: String, powered: Boolean, input: Boolean = false): ResourceLocation {
			return blockLoc("flag/$id".powered(powered) + (if (input) "_input" else ""))
		}
		
		
		fun BlockModelGenerators.forSetter() {
			val setterLoc = BlockFlagWriter.id_setter
			
			val setter_off = sidedBlock(setterLoc.powered(false), north=getTexture(setterLoc, false, input=true), rest=getTexture(setterLoc, false))
			
			MultiVariantGenerator.dispatch(ModBlocks_Builder.FLAG_SETTER, plainVariant(setter_off))
				.with(ROTATION_HORIZONTAL_FACING)
				.submit()
			
			registerSimpleItemModel(ModBlocks_Builder.FLAG_SETTER, setter_off)
		}

		fun BlockModelGenerators.forResetter() {
			val setterLoc = BlockFlagWriter.id_resetter
			
			val setter_off = sidedBlock(setterLoc.powered(false), north=getTexture(setterLoc, false, input=true), rest=getTexture(setterLoc, false))
			
			MultiVariantGenerator.dispatch(ModBlocks_Builder.FLAG_RESETTER, plainVariant(setter_off))
				.with(ROTATION_HORIZONTAL_FACING)
				.submit()
			
			registerSimpleItemModel(ModBlocks_Builder.FLAG_RESETTER, setter_off)
		}
	}
	
	private object WirelessRedstone {
		const val txBase = "redstone/"
		
		fun BlockModelGenerators.forTransmitter() {
			val txSpecific = txBase + "transmitter/transmitter"
			val off = columnModel(BlockRedstoneTransmitter.Companion.id.powered(false), blockLoc("${txSpecific}_sides").powered(false), blockLoc("${txSpecific}_top").powered(false))
			val on = columnModel(BlockRedstoneTransmitter.Companion.id.powered(true), blockLoc("${txSpecific}_sides").powered(true), blockLoc("${txSpecific}_top").powered(true))
			
			MultiVariantGenerator.dispatch(ModBlocks_Builder.REDSTONE_TRANSMITTER)
				.with(poweredInitialVariants(off, on))
				.submit()
			
			registerSimpleItemModel(ModBlocks_Builder.REDSTONE_TRANSMITTER, off)
		}
		
		fun BlockModelGenerators.forReceiver() {
			val txSpecific = txBase + "receiver/receiver"
			val off = columnModel(BlockRedstoneReceiver.Companion.id.powered(false), blockLoc("${txSpecific}_sides").powered(false), blockLoc("${txSpecific}_top").powered(false))
			val on = columnModel(BlockRedstoneReceiver.Companion.id.powered(true), blockLoc("${txSpecific}_sides").powered(true), blockLoc("${txSpecific}_top").powered(true))
			
			MultiVariantGenerator.dispatch(ModBlocks_Builder.REDSTONE_RECEIVER)
				.with(poweredInitialVariants(off, on))
				.submit()
			
			
			registerSimpleItemModel(ModBlocks_Builder.REDSTONE_RECEIVER, off)
		}
	}
	//endregion
	
	
}


private fun blockLoc(path: String): ResourceLocation {
	return ResourceLocation.fromNamespaceAndPath(MODID, "block/$path")
}

//region Macros
context(BlockModelGenerators)
private fun ModelTemplate.create(id: String, textureMapping: TextureMapping): ResourceLocation {
	return this.create(blockLoc(id), textureMapping, this@BlockModelGenerators.modelOutput)
}

private fun blockFaces(top: ResourceLocation, bottom: ResourceLocation, north: ResourceLocation, south: ResourceLocation, east: ResourceLocation, west: ResourceLocation): TextureMapping {
	return TextureMapping().put(TextureSlot.UP, top)
		.put(TextureSlot.DOWN, bottom)
		.put(TextureSlot.NORTH, north)
		.put(TextureSlot.EAST, east)
		.put(TextureSlot.SOUTH, south)
		.put(TextureSlot.WEST, west)
}


//region Model Instantiation
context(BlockModelGenerators)
private fun sidedBlock(id: String, top: ResourceLocation, bottom: ResourceLocation, north: ResourceLocation, south: ResourceLocation, east: ResourceLocation, west: ResourceLocation, particle: ResourceLocation = south): ResourceLocation {
	return ModelTemplates.CUBE.create(id, blockFaces(top, bottom, north, south, east, west).put(TextureSlot.PARTICLE, particle))
}

/**
 * For use with named arguments.
 *
 * `sidedBlock("name", top=myTopTxt, north=outputTxt, rest=otherTxt)
 */
context(BlockModelGenerators)
private fun sidedBlock(id: String, rest: ResourceLocation, top: ResourceLocation = rest, bottom: ResourceLocation = rest, north: ResourceLocation = rest, south: ResourceLocation = rest, east: ResourceLocation = rest, west: ResourceLocation = rest, particle: ResourceLocation = rest): ResourceLocation {
	return sidedBlock(id, top, bottom, north, south, east, west, particle)
}

private fun BlockModelGenerators.columnModel(id: String, side: ResourceLocation, updown: ResourceLocation): ResourceLocation {
	return ModelTemplates.CUBE_COLUMN.create(blockLoc(id), TextureMapping.column(side, updown), modelOutput)
}


//region Variant Building
private fun poweredInitialVariants(off: ResourceLocation, on: ResourceLocation): PropertyDispatch.C1<MultiVariant?, Boolean> {
	return PropertyDispatch.initial(BlockStateProperties.POWERED)
		.select(true, plainVariant(on))
		.select(false, plainVariant(off))
}

private operator fun <T : Comparable<T>> Property<T>.invoke(vararg pairs: Pair<T, MultiVariant>): PropertyDispatch.C1<MultiVariant, T> {
	val it = PropertyDispatch.initial(this)
	var current = it.select(pairs[0].first, pairs[0].second)
	for (i in 1..<pairs.size) {
		current = current.select(pairs[i].first, pairs[i].second)
	}
	return current
}

@JvmName("invokeMutator")
private operator fun <T : Comparable<T>> Property<T>.invoke(vararg pairs: Pair<T, VariantMutator>): PropertyDispatch.C1<VariantMutator, T> {
	val it = PropertyDispatch.modify(this)
	var current = it.select(pairs[0].first, pairs[0].second)
	for (i in 1..<pairs.size) {
		current = current.select(pairs[i].first, pairs[i].second)
	}
	return current
}

context(BlockModelGenerators)
private fun MultiVariantGenerator.submit() {
	this@BlockModelGenerators.blockStateOutput(this)
}

private val ROTATION_HORIZONTAL_FACING: PropertyDispatch<VariantMutator?> = PropertyDispatch.modify(BlockStateProperties.HORIZONTAL_FACING)
	.select(Direction.EAST, BlockModelGenerators.Y_ROT_90)
	.select(Direction.SOUTH, BlockModelGenerators.Y_ROT_180)
	.select(Direction.WEST, BlockModelGenerators.Y_ROT_270)
	.select(Direction.NORTH, BlockModelGenerators.NOP)


private fun ResourceLocation.powered(isPowered: Boolean): ResourceLocation {
	return if (isPowered) {
		this.withSuffix("_powered")
	} else {
		this.withSuffix("_unpowered")
	}
}

private fun String.powered(isPowered: Boolean): String {
	return this + if (isPowered) "_powered" else "_unpowered"
}