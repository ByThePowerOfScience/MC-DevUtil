package btpos.mcmods.devutil.neoforge

import btpos.mcmods.devutil.DevUtilEntry
import net.neoforged.fml.common.Mod

@Mod(DevUtilEntry.MODID)
object NeoForgeEntry {
	init {
		DevUtilEntry.init()
	}
}