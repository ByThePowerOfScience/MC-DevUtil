package btpos.mcmods.devutil.fabric

import btpos.mcmods.devutil.DevUtilEntry
import net.fabricmc.api.ModInitializer

class FabricEntry : ModInitializer {
	override fun onInitialize() {
		DevUtilEntry.init()
	}
}