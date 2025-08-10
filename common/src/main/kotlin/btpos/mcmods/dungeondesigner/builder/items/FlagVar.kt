package btpos.mcmods.dungeondesigner.builder.items

import btpos.mcmods.dungeondesigner.registry.ModItems
import net.minecraft.world.item.Item

class ItemFlagVariable(props: Properties) : Item(props) {
	companion object {
		const val id = "builder/flag_variable"
		
//		override fun ItemModelProvider.buildModels() {
//			basicItem(ModItems.FLAG_ITEM)
//		}
	}
}

