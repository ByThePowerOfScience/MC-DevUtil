package btpos.mcmods.devutil.neoforge

import btpos.mcmods.devutil.CommonEntry
import btpos.mcmods.devutil.MODID
import net.neoforged.fml.common.Mod

@Mod(MODID)
object NeoForgeEntry {
	init {
		CommonEntry.init()
	}
}