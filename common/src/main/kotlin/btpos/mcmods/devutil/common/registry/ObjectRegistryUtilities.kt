@file:Suppress("unused")

package btpos.mcmods.devutil.common.registry

import btpos.mcmods.devutil.common.ext.kotlin.safeGetDelegate
import com.mojang.datafixers.types.Type
import com.mojang.serialization.Codec
import net.minecraft.core.BlockPos
import net.minecraft.core.Registry
import net.minecraft.core.component.DataComponentType
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.StreamCodec
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.BlockItem
import net.minecraft.world.item.Item
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.world.level.block.state.BlockBehaviour
import net.minecraft.world.level.block.state.BlockState
import java.util.function.BiFunction
import kotlin.reflect.KProperty0


/**
 * Contains utilities for registering objects.
 * @see BlockRegistryUtilities
 * @see ItemRegistryUtilities
 * @see DataComponentRegistryUtilities
 */
interface ObjectRegistryUtilities {
	fun <T> createRegistry(id: String, key: ResourceKey<Registry<T>>): DeferredRegistrar<T> {
		return DeferredRegistrar.create(id, key)
	}
	
	fun register()
	
	/**
	 * Get the ID (ResourceLocation.path) of the object stored in this property.
	 *
	 * @throws IllegalStateException If the property is not delegated to a [RegistrySupplier]
	 */
	fun getId(prop: KProperty0<*>): ResourceLocation {
		return prop.safeGetDelegate<RegistrySupplier<*>>()?.id
		       ?: throw IllegalStateException("Property $prop must be a RegistrySupplier to get its ID.")
	}
	
	fun DeferredRegistrar<*>.modLoc(path: String): ResourceLocation = ResourceLocation.fromNamespaceAndPath(modId, path)
	
	fun <T> DeferredRegistrar<T>.getKeyForPath(path: String): ResourceKey<T> {
		return ResourceKey.create(registryKey, this.modLoc(path))
	}
}

interface ItemRegistryUtilities : ObjectRegistryUtilities {
	val ITEMS: DeferredRegistrar<Item>
	
	override fun register() {
		ITEMS.registerSelf()
	}
	
	/**
	 * Registers an item with the given name.
	 *
	 * @param name The path of the item in a ResourceLocation, e.g. "dirt" in "minecraft:dirt"
	 * @param propsFactory Creates a new properties instance. 1.21+ requires the ID as part of the properties, meaning we have to make new Properties instances each time.
	 * @param itemFactory Creates the item from the properties.
	 */
	fun <T : Item> item(name: String, propsFactory: () -> Item.Properties = { Item.Properties() }, itemFactory: (Item.Properties) -> T): RegistrySupplier<T> {
		return ITEMS.register(name) { itemFactory(propsFactory().setId(ITEMS.getKeyForPath(name))) }
	}
	
	/**
	 * Registers an item with the same id as the provided object: most likely a block.
	 *
	 * @param bprop A property that has a [RegistrySupplier] to get the id from.
	 * @param propsFactory Creates a new properties instance. 1.21+ requires the ID as part of the properties, meaning we have to make new Properties instances each time.
	 * @param itemFactory Creates the item from the properties.
	 */
	fun <T : Item> item(bprop: KProperty0<*>, propsFactory: () -> Item.Properties = Item::Properties, itemFactory: (Item.Properties) -> T): RegistrySupplier<T> {
		val name = getId(bprop).path
		return item(name, propsFactory, itemFactory)
	}
}

interface BlockRegistryUtilities : ItemRegistryUtilities {
	val BLOCKS: DeferredRegistrar<Block>
	val ENTITIES: DeferredRegistrar<BlockEntityType<*>>
	
	override fun register() {
		BLOCKS.registerSelf()
		super.register() // load items AFTER blocks!!!
		ENTITIES.registerSelf()
	}
	
	/**
	 * Registers a [BlockItem] for the provided block and gives it the same name.
	 * @param bprop Reference to the field holding the block, e.g. `::MY_BLOCK`
	 */
	fun <B : Block> blockItem(bprop: KProperty0<B>, props: () -> Item.Properties = Item::Properties): RegistrySupplier<BlockItem> {
		return item(bprop, props) { BlockItem(bprop.get(), it) }
	}
	
	/**
	 * Registers this block, with optional [BlockItem].
	 *
	 * @param id The id of the block.
	 * @param propertiesFactory Properties require the id of the block now, so we can't just use the same Properties object for all of them anymore.  We have to make a new instance each time, hence the factory.
	 * @param withItem If true, also register a standard BlockItem for this block with the same ID.
	 * @param itemProps The BlockItem's properties. Only matters if [withItem] is set.
	 * @param supplier The factory to create the block instance. Accepts the properties supplied by [propertiesFactory].
	 */
	fun <T : Block> block(id: String, propertiesFactory: () -> BlockBehaviour.Properties = BlockBehaviour.Properties::of, withItem: Boolean = false, itemProps: () -> Item.Properties = Item::Properties, supplier: (BlockBehaviour.Properties) -> T): RegistrySupplier<T> {
		val propsInst = propertiesFactory().setId(BLOCKS.getKeyForPath(id))
		
		return BLOCKS.register(id) { supplier(propsInst) }.also { bDelegate ->
			if (withItem)
				item(id, itemProps) { BlockItem(bDelegate.get(), it) }
		}
	}
	
	/**
	 * A public constructor for [BlockEntityType][net.minecraft.world.level.block.entity.BlockEntityType] using accessible classes.
	 *
	 * @param entityFactory Creates an instance of your BlockEntity from the position it's being made for and the blockstate of the block asking for it.
	 * @param allowedBlocks What blocks are allowed to have this BlockEntity.  I believe this is checked at runtime, but I could be wrong.
	 */
	fun <T : BlockEntity> BlockEntityType(entityFactory: BiFunction<BlockPos, BlockState, T>, allowedBlocks: Set<Block>): BlockEntityType<T> {
		return net.minecraft.world.level.block.entity.BlockEntityType(entityFactory::apply, allowedBlocks)
	}
	
	/**
	 * Register a block entity type by the given name.
	 */
	fun <T : BlockEntity> ent(name: String, supplier: () -> BlockEntityType<T>): RegistrySupplier<BlockEntityType<T>> {
		return ENTITIES.register<BlockEntityType<T>>(name, supplier)
	}
	
	/**
	 * Register a [BlockEntityType] specifically for the provided block, with the same name as the block.
	 *
	 * @param bprop Property reference to the block you want to register this entity for.
	 * @param factory Creates an instance of the [BlockEntity] during gameplay.
	 * @param type Pretty much always null unless you just reprint a vanilla block, since we can't use custom datafixers ;_;
	 */
	fun <B : Block, T : BlockEntity> ent(bprop: KProperty0<B>, factory: (BlockPos, BlockState) -> T, type: Type<*>? = null): RegistrySupplier<BlockEntityType<T>> {
		return ent(getId(bprop).path) {
			@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
			BlockEntityType(factory, setOf(bprop.get()))
		}
	}
}

/**
 * Helper methods for any registry that handles 1.20.6+ data components
 */
interface DataComponentRegistryUtilities : ObjectRegistryUtilities {
	val COMPONENTS: DeferredRegistrar<DataComponentType<*>>
	
	/**
	 * Register a [DataComponentType] with the given name.
	 */
	fun <T : Any> component(name: String, generator: () -> DataComponentType<T>): RegistrySupplier<DataComponentType<T>> {
		return COMPONENTS.register<DataComponentType<T>>(name, generator)
	}
	
	/**
	 * I don't really know? but it's here
	 */
	fun <T> buildPersistentComponent(codec: Codec<T>, networkSynchronizer: StreamCodec<in RegistryFriendlyByteBuf, T>? = null, cacheEncoding: Boolean = false): DataComponentType<T> {
		return DataComponentType.builder<T>().persistent(codec).let { bld ->
			networkSynchronizer?.let { bld.networkSynchronized(it) } ?: bld
		}.let {
			if (cacheEncoding) {
				it.cacheEncoding()
			} else {
				it
			}
		}.build()
	}
}