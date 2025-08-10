@file:Suppress("NOTHING_TO_INLINE")

package btpos.mcmods.dungeondesigner.neoforge.datagen.lang

import btpos.mcmods.dungeondesigner.MODID
import btpos.mcmods.dungeondesigner.registry.ModBlocks_Builder
import btpos.mcmods.dungeondesigner.registry.ModItems
import net.minecraft.data.PackOutput
import net.minecraft.world.item.Item
import net.minecraft.world.level.block.Block
import net.neoforged.neoforge.common.data.LanguageProvider

class LangGen_English(output: PackOutput) : LanguageProvider(output, MODID, "en_us") {
	override fun addTranslations() {
		addBlocks()
		addItems()
		addCreativeTabs()
	}
	
	fun addBlocks() {
		with (ModBlocks_Builder) {
			DUNGEON_NEXUS("Dungeon Nexus")
			
			TRIGGER_BLOCK("Trigger Watcher")
			
			REDSTONE_TRANSMITTER("Redstone Transmitter")
			REDSTONE_RECEIVER("Redstone Receiver")
			
			FLAG_READER("Flag Reader")
			FLAG_SETTER("Flag Setter")
			FLAG_RESETTER("Flag Resetter")
			
			FIGHT_CONTROLLER("Fight Controller")
		}
	}
	
	fun addItems() {
		with (ModItems) {
			TRIGGER_ITEM("Trigger Painter", "RClick to set first corner", "Shift+RClick to set second corner")
			PIPETTE_ITEM("Entity Selector", "Set up an entity for the Fight Controller.", "RClick on an entity to save its data,", "RClick on the ground to save the spawn position and rotation")
			FLAG_ITEM("Flag")
			REMOTE_LINKER("Remote Linker")
		}
	}
	
	
	fun addCreativeTabs() {
		add("btpos.dungeondesigner.category", "Dungeon Designer")
	}
	
	
	
	// region Utils
	operator fun Block.invoke(translation: String, tooltip: String? = null, customItemName: String? = null) {
		this@LangGen_English.add(this, translation)
		
		// Add item name
		(customItemName ?: translation).let {
			this@LangGen_English.add(this.asItem().descriptionId, it)
		}
		
		// Add tooltip
		if (tooltip != null) {
			this@LangGen_English.add(this.descriptionId + ".tooltip", tooltip)
		}
	}
	
	operator fun Item.invoke(translation: String, vararg tooltip: String) {
		this@LangGen_English.add(this, translation)
		tooltip.forEachIndexed { i, it ->
			this@LangGen_English.add(this.descriptionId + ".tooltip$i", it)
		}
	}
	// endregion
}