package btpos.mcmods.devutil.neoforge

import btpos.mcmods.testproject.CommonEntry
import btpos.mcmods.testproject.MODID
import net.neoforged.fml.common.Mod

@Mod(MODID)
object ForgeEntry {
	init {
		CommonEntry.init()
	}
}