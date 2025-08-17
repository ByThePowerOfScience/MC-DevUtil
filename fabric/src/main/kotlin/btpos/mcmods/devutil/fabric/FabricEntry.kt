package btpos.mcmods.devutil.fabric

import btpos.mcmods.devutil.CommonEntry
import net.fabricmc.api.ModInitializer

class FabricEntry : ModInitializer {
	override fun onInitialize() {
		CommonEntry.init()
	}
}