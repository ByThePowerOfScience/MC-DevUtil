package btpos.mcmods.dungeondesigner.registry

import btpos.mcmods.devutil.common.ext.vanilla.stack
import btpos.mcmods.devutil.common.registry.IObjectRegistry
import btpos.mcmods.dungeondesigner.MODID
import dev.architectury.registry.CreativeTabRegistry
import net.minecraft.core.registries.Registries
import net.minecraft.network.chat.Component
import net.minecraft.world.item.ItemStack

@Suppress("unused")
object ModCreativeTabs : IObjectRegistry {
	val REGISTRY = createRegistry(Registries.CREATIVE_MODE_TAB)
	
	override fun register() {
		REGISTRY.register()
	}
	
	val TAB = REGISTRY.register(MODID) {
		CreativeTabRegistry.create { builder ->
			builder.icon { ModItems.TRIGGER_ITEM.stack() }
				.title(Component.translatable("btpos.dungeondesigner.category"))
				.displayItems { itemDisplayParameters, output ->
					output.acceptAll(ModBlocks_Builder.BLOCKS.map { ItemStack(it.get()) })
					output.acceptAll(ModItems.ITEMS.map { ItemStack(it.get()) })
				}
		}
	}
}