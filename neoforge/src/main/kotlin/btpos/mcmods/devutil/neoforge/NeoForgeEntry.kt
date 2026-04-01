package btpos.mcmods.devutil.neoforge

import btpos.mcmods.devutil.DevUtilEntry
import btpos.mcmods.devutil.neoforge.impl.events.NeoForgeTransactionHandlers
import net.neoforged.fml.common.Mod
import thedarkcolour.kotlinforforge.neoforge.forge.FORGE_BUS

@Mod(DevUtilEntry.MODID)
object NeoForgeEntry {
	init {
		FORGE_BUS.register(NeoForgeTransactionHandlers)
		DevUtilEntry.init()
	}
}