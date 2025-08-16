package btpos.mcmods.devutil.common.util.serialization

import com.mojang.serialization.Codec
import net.minecraft.nbt.CompoundTag
import net.minecraft.world.level.storage.ValueInput
import net.minecraft.world.level.storage.ValueOutput
import kotlin.jvm.optionals.getOrNull

interface INbtSerializable {
	fun populateFromNbt(tag: CompoundTag)
	fun writeAsNbt(tag: CompoundTag)
}

fun CompoundTag.putNbtSerializable(key: String, serializable: INbtSerializable) {
	val tag = CompoundTag()
	serializable.writeAsNbt(tag)
	this.put(key, tag)
}

fun <T : ICodecSerializable<T>> ValueOutput.putCodecSerializable(key: String, serializable: T) {
	this.store(key, serializable.codec(), serializable)
}

/**
 * Reads the tag and populates an existing item with its state.
 */
fun <T : ICodecSerializableMutable<T>> ValueInput.readCodecSerializableToExisting(key: String, serializable: T): Boolean {
	val fromTag = this.read(key, serializable.codec()).getOrNull() ?: return false
	serializable.copyFrom(fromTag)
	return true
}

/**
 * Creates a new instance of the object the tag represents.
 */
fun <T: ICodecSerializable<T>> ValueInput.readCodecSerializable(key: String, codec: Codec<T>): T? {
	return this.read(key, codec).getOrNull()
}
