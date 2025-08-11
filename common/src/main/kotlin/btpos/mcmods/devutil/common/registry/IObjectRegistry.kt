package btpos.mcmods.devutil.common.registry

import btpos.mcmods.devutil.common.ext.kotlin.safeGetDelegate
import com.mojang.datafixers.types.Type
import com.mojang.serialization.Codec
import dev.architectury.registry.registries.DeferredRegister
import dev.architectury.registry.registries.RegistrySupplier
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
import java.lang.invoke.MethodHandle
import java.lang.invoke.MethodHandles
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty0
import kotlin.reflect.jvm.isAccessible

@Suppress("NonExtendableApiUsage")
class ObjectHolderDelegate<T>(val registryObject: RegistrySupplier<T>) : ReadOnlyProperty<Any?, T>, RegistrySupplier<T> by registryObject {
	override fun getValue(thisRef: Any?, property: KProperty<*>): T {
		return registryObject.get()
	}
	
	override fun getRegisteredName(): String? {
		return registryObject.getRegisteredName()
	}
}

fun <REG : Any, T : REG> DeferredRegister<REG>.registerObject(id: String, supplier: () -> T): ObjectHolderDelegate<T> {
	return ObjectHolderDelegate(this.register(id, supplier))
}

//region BlockEntityType Reflection
/**
 * Arch doesn't have [BlockEntityType]'s constructor public, so this unreflects it and caches the method handle for performance.
 */
@JvmInline
private value class BlockEntityTypeConstructor private constructor(val handle: MethodHandle) {
	constructor() : this(Unit.run {
		val constructor = BlockEntityType::class.java.declaredConstructors.first { it.parameterTypes.contentEquals(arrayOf(BlockEntityType.BlockEntitySupplier::class.java, Set::class.java)) }
		constructor.isAccessible = true
		MethodHandles.lookup().unreflectConstructor(constructor)
	})
	
	@Suppress("UNCHECKED_CAST")
	operator fun <T : BlockEntity> invoke(factory: BlockEntityType.BlockEntitySupplier<T>, acceptedBlocks: Set<Block>): BlockEntityType<T> {
		return handle.invokeExact(factory, acceptedBlocks) as BlockEntityType<T>
	}
}
private val BETypeCtor: BlockEntityTypeConstructor by lazy { BlockEntityTypeConstructor() }
//endregion
/**
 * Use cached reflection to invoke constructor, since it's private.
 */
fun <T : BlockEntity> BlockEntityType(factory: (BlockPos, BlockState) -> T, vararg acceptedBlocks: Block): BlockEntityType<T> {
	return BETypeCtor(BlockEntityType.BlockEntitySupplier<T>(factory), acceptedBlocks.toSet())
}

typealias PlatformRegistry<T> = DeferredRegister<T>

interface IObjectRegistry {
	fun <T> createRegistry(id: String, key: ResourceKey<Registry<T>>): PlatformRegistry<T> {
		return DeferredRegister.create(id, key)
	}
	
	fun register()
	
	fun getId(prop: KProperty0<*>): ResourceLocation {
		prop.isAccessible = true
		
		return prop.safeGetDelegate<ObjectHolderDelegate<*>>()?.registryObject?.id
		       ?: throw IllegalStateException("Property $prop is not a registry delegate!")
	}
	
	val PlatformRegistry<*>.modid
		get() = this.registrarManager.modId
	
	fun PlatformRegistry<*>.modLoc(path: String) = ResourceLocation.fromNamespaceAndPath(modid, path)
	
	fun <T> PlatformRegistry<T>.getKeyForPath(path: String): ResourceKey<T> {
		return ResourceKey.create(this.registrar.key(), this.modLoc(path))
	}
}

interface IItemRegistry : IObjectRegistry {
	val ITEMS: PlatformRegistry<Item>
	
	override fun register() {
		ITEMS.register()
	}
	
	fun <T : Item> item(name: String, props: () -> Item.Properties = { Item.Properties() }, supplier: (Item.Properties) -> T): ObjectHolderDelegate<T> {
		return ITEMS.registerObject(name, { supplier(props().setId(ITEMS.getKeyForPath(name))) })
	}
	
	fun <T : Item> item(bprop: KProperty0<*>, props: () -> Item.Properties, factory: (Item.Properties) -> T): ObjectHolderDelegate<T> {
		val name = getId(bprop).path
		return item(name, props, factory)
	}
}

interface IBlockRegistry : IItemRegistry {
	val BLOCKS: PlatformRegistry<Block>
	val ENTITIES: PlatformRegistry<BlockEntityType<*>>
	
	override fun register() {
		BLOCKS.register()
		super.register() // load items AFTER blocks!!!
		ENTITIES.register()
	}
	
	fun <B : Block> blockItem(bprop: KProperty0<B>, props: () -> Item.Properties = Item::Properties): ObjectHolderDelegate<BlockItem> {
		return item(bprop, props) { BlockItem(bprop.get(), it) }
	}
	
	/**
	 * Registers this block, with optional [BlockItem].
	 *
	 * @param id The id of the block.
	 * @param propertiesFactory Properties require the id of the block now, so we can't just use the same object for all of them anymore.  We have to make a new instance each time, hence the factory.
	 * @param withItem If true, also register a standard BlockItem for this block with the same ID.
	 * @param itemProps The BlockItem's properties. Only matters if [withItem] is set. Should not be `null`; it's only nullable so we can prevent an object allocation on the default value.
	 * @param supplier The factory to create the block instance. Accepts the properties supplied by [propertiesFactory].
	 */
	fun <T : Block> block(id: String, propertiesFactory: () -> BlockBehaviour.Properties, withItem: Boolean = false, itemProps: () -> Item.Properties = Item::Properties, supplier: (BlockBehaviour.Properties) -> T): ObjectHolderDelegate<T> {
		val propsInst = propertiesFactory().setId(BLOCKS.getKeyForPath(id))
		
		return BLOCKS.registerObject(id, { supplier(propsInst) }).also { bDelegate ->
			if (withItem)
				item(id, itemProps) { BlockItem(bDelegate.get(), it) }
		}
	}
	
	
	
	
	
	
	fun <T : BlockEntity> ent(name: String, supplier: () -> BlockEntityType<T>): ObjectHolderDelegate<BlockEntityType<T>> {
		return ENTITIES.registerObject(name, supplier)
	}
	
	fun <B : Block, T : BlockEntity> ent(bprop: KProperty0<B>, supplier: (BlockPos, BlockState) -> T, type: Type<*>? = null): ObjectHolderDelegate<BlockEntityType<T>> {
		return ent(getId(bprop).path) {
			@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
			BlockEntityType(supplier, bprop.get())
		}
	}
	
	
	
	/* // you can't destructure in fields ;_;
	fun <B : Block, ITEM: BlockItem> registerBlock(name: String, blockSupplier: () -> B, itemSupplier: () -> ITEM): Pair<ObjectHolderDelegate<B>, ObjectHolderDelegate<ITEM>> {
		return Pair(BLOCKS.registerObject(name, blockSupplier), ITEMS.registerObject(name, itemSupplier))
	}
	
	fun <B : Block, ENT : BlockEntity> registerBlock(name: String, blockSupplier: () -> B, entFactory: (BlockPos, BlockState) -> ENT, dataType: Type<*>? = null): Triple<ObjectHolderDelegate<B>, ObjectHolderDelegate<BlockItem>, ObjectHolderDelegate<BlockEntityType<ENT>>> {
		val block = BLOCKS.registerObject(name, blockSupplier)
		val entDelegate = ENTITIES.registerObject(name) {
			@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
			BlockEntityType.Builder.of(entFactory, block.get()).build(dataType)
		}
		return Triple(block, ITEMS.registerObject(name, {
			BlockItem(block.get(), Item.Properties())
		}), entDelegate)
	}
	
	fun <B : Block, ENT : BlockEntity, ITEM : Item> registerBlock(name: String, blockSupplier: () -> B, itemSupplier: () -> ITEM, entFactory: (BlockPos, BlockState) -> ENT, dataType: Type<*>? = null): Triple<ObjectHolderDelegate<B>, ObjectHolderDelegate<ITEM>, ObjectHolderDelegate<BlockEntityType<ENT>>> {
		val block = BLOCKS.registerObject(name, blockSupplier)
		val entDelegate = ENTITIES.registerObject(name) {
			@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
			BlockEntityType.Builder.of(entFactory, block.get()).build(dataType)
		}
		return Triple(block, ITEMS.registerObject(name, itemSupplier), entDelegate)
	}
	*/
	
	
}

/**
 * Helper methods for any registry that handles 1.20.6+ data components
 */
interface IDataComponentRegistry : IObjectRegistry {
	val COMPONENTS: PlatformRegistry<DataComponentType<*>>
	
	fun <T : Any> component(name: String, generator: () -> DataComponentType<T>): ObjectHolderDelegate<DataComponentType<T>> {
		return COMPONENTS.registerObject(name, generator)
	}
	
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