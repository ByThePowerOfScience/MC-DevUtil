@file:Suppress("NOTHING_TO_INLINE")

package btpos.mcmods.devutil.common.ext.vanilla.data

import btpos.mcmods.devutil.common.ext.java.cast
import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.core.BlockPos
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.Tag
import java.util.Optional
import kotlin.jvm.optionals.getOrNull

//region Codecs
fun <T> MapCodec<Optional<T>>.optionalToNull(): MapCodec<T?> {
	return this.xmap<T?>({ it.orElse(null) }, { Optional.ofNullable(it).cast() })
}



/**
 * WHY does DFU throw an NPE when you put null as the default value???  Sometimes you just want null to be the default!!!
 * You can't even use [Minecraft's idiom][optionalToNull]: if the value isn't present, it'll throw a NPE.  I wonder if Mojang knows about that, or if they just assume it still works?
 *
 * Anyway, this is the best I could do: at least make it where I didn't have to use manual getters each time.
 * Still have to use manual constructor invocations, though...
 *
 * nevermind this doesn't work
 */
inline fun <ENCL, T> MapCodec<Optional<T>>.forNullableGetter(crossinline getter: (ENCL) -> T?): RecordCodecBuilder<ENCL, Optional<T>> {
    return this.forGetter<ENCL> { encl -> Optional.ofNullable(getter(encl)).cast() }
}

inline fun <ENCL, T> Codec<T>.nullSafeFieldOf(name: String, crossinline getter: (ENCL) -> T?): RecordCodecBuilder<ENCL, Optional<T>> {
    return this.optionalFieldOf(name).forGetter { Optional.ofNullable(getter(it)).cast() }
}
//endregion



inline operator fun CompoundTag.set(key: String, value: Tag) = this.put(key, value)



//region BlockPos
inline fun BlockPos.toCompoundTag(): CompoundTag {
	return CompoundTag().apply {
		putInt("X", x)
		putInt("Y", y)
		putInt("Z", z)
	}
}

fun CompoundTag.asBlockPos(): BlockPos? {
	val x = this.getInt("X").getOrNull() ?: return null
	val y = this.getInt("Y").getOrNull() ?: return null
	val z = this.getInt("Z").getOrNull() ?: return null
	return BlockPos(x, y, z)
}

fun CompoundTag.getBlockPos(key: String): BlockPos? {
	return getCompoundOrNull(key)?.asBlockPos()
}
//endregion

//region Get null instead of default
/**
 * Get a compound tag, *without* creating it if it doesn't exist.
 */
fun CompoundTag.getCompoundOrNull(key: String): CompoundTag? {
	return this.getCompound(key).getOrNull()
//	if (!this.contains(key, CompoundTag.TAG_COMPOUND.toInt())) {
//		return null
//	}
//	return this.getCompound(key)
}

fun CompoundTag.getStringOrNull(key: String): String? {
	return this.getString(key).getOrNull()
//	if (!this.contains(key, CompoundTag.TAG_STRING.toInt())) {
//		return null
//	}
//	return this.getString(key)
}

fun CompoundTag.getIntOrNull(key: String): Int? {
	return this.getInt(key).getOrNull()
//	if (!this.contains(key, CompoundTag.TAG_INT.toInt())) {
//		return null
//	}
//	return this.getInt(key)
}
//endregion

/**
 * Gets the CompoundTag for the given key if present.
 *
 * Otherwise, creates a new CompoundTag and adds it to this object, then returns the newly-created tag.
 */
fun CompoundTag.getOrCreateCompound(key: String): CompoundTag {
	val fromTag = this.getCompound(key)
	return if (fromTag.isEmpty) {
		CompoundTag().also {
			this.put(key, it)
		}
	} else {
		fromTag.get()
	}
//	if (!this.contains(key, CompoundTag.TAG_COMPOUND.toInt())) {
//		return CompoundTag().also {
//			this.put(key, it)
//		}
//	}
//	return this.get(key)!! as CompoundTag
}

/**
 * Macro for "if the value is null, remove the key, else set it using the function".
 */
inline fun <T> CompoundTag.setOrRemove(key: String, value: T?, setter: CompoundTag.(T) -> Unit) {
	if (value == null)
		this.remove(key)
	else
		this.setter(value)
}