@file:Suppress("NOTHING_TO_INLINE")
@file:OptIn(ExperimentalContracts::class)

package btpos.mcmods.devutil.common.ext.vanilla.world

import net.minecraft.core.BlockPos
import net.minecraft.world.level.BlockGetter
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.properties.Property
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind.EXACTLY_ONCE
import kotlin.contracts.contract
import kotlin.jvm.optionals.getOrNull


/**
 * Convert its optional to nullable for use with Kotlin's nullability idioms
 */
inline fun <T : BlockEntity> BlockGetter.blockEntity(pos: BlockPos, type: BlockEntityType<T>): T? {
	return getBlockEntity(pos, type).getOrNull()
}

// ============= BLOCKSTATES =============
/**
 * setValue but renamed basically, just so I don't forget that it doesn't actually change the state in any way
 */
inline fun <T : Comparable<T>> BlockState.with(property: Property<T>, value: T): BlockState = this.setValue(property, value)

inline fun <T : Comparable<T>> BlockState.transform(property: Property<T>, transformer: (T) -> T): BlockState {
	contract {
		callsInPlace(transformer, EXACTLY_ONCE)
	}
	
//	debug(!this.hasProperty(property)) {
//		TLOGGER.error("Calling transform on blockstate without that property!\nState: {}\nProperty: {}", this, property)
//	}
	
	return this.setValue(property, transformer(this.getValue(property)))
}

inline operator fun <T : Comparable<T>> BlockState.get(prop: Property<T>) = this.getValue(prop)

inline fun BlockState.getEntity(level: Level, pos: BlockPos): BlockEntity? = if (this.hasBlockEntity()) level.getBlockEntity(pos) else null