package btpos.mcmods.devutil.multiversion.blockentities

import btpos.mcmods.devutil.common.util.serialization.ICodecSerializable
import btpos.mcmods.devutil.common.util.serialization.ICodecSerializableMutable
import btpos.mcmods.devutil.common.util.serialization.Serialization.decodeTag
import btpos.mcmods.devutil.common.util.serialization.Serialization.encodeToTag
import com.mojang.serialization.Codec
import net.minecraft.core.BlockPos
import net.minecraft.nbt.CompoundTag
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.storage.ValueInput
import net.minecraft.world.level.storage.ValueOutput
import kotlin.jvm.optionals.getOrNull

abstract class SaveableBlockEntity(type: BlockEntityType<*>, pos: BlockPos, blockState: BlockState)
    : BlockEntity(type, pos, blockState)
{
    final override fun saveAdditional(output: ValueOutput) {
        super.saveAdditional(output)
        saveAdditional(SaveOutput_ValueOutput(output))
    }
    
    final override fun loadAdditional(input: ValueInput) {
        super.loadAdditional(input)
        loadAdditional(SaveInput_ValueInput(input))
    }
    
    final override fun saveCustomOnly(p_422286_: ValueOutput) {
        super.saveCustomOnly(p_422286_)
        saveCustomOnly(SaveOutput_ValueOutput(p_422286_))
    }
    
    open fun saveCustomOnly(input: SaveOutput) {}
    
    open fun saveAdditional(output: SaveOutput) {}
    
    open fun loadAdditional(input: SaveInput) {}
}

/**
 * Abstraction of saving with codecs to both CompoundTag and ValueOutput between 1.21.1 and 1.21.7+
 */
interface SaveOutput {
    fun <T : Any> putCodecSerializable(key: String, serializable: T, codec: Codec<T>)
}

fun <T : ICodecSerializable<T>> SaveOutput.putCodecSerializable(key: String, serializable: T) {
    putCodecSerializable(key, serializable, serializable.codec())
}

@JvmInline value class SaveOutput_ValueOutput(private val output: ValueOutput) : SaveOutput {
    override fun <T : Any> putCodecSerializable(key: String, serializable: T, codec: Codec<T>) {
        output.store(key, codec, serializable)
    }
}

class SaveOutput_CompoundTag(private val tag: CompoundTag) : SaveOutput {
    override fun <T : Any> putCodecSerializable(key: String, serializable: T, codec: Codec<T>) {
        tag.put(key, codec.encodeToTag(serializable))
    }
}

/**
 * Abstraction of reading data with codecs from both CompoundTag and ValueInput between 1.21.1 and 1.21.7+
 */
interface SaveInput {
    /**
     * Read the data stored to a new object, and returns that object if present.
     *
     * If the value stored is absent in the data, returns null.
     */
    fun <T : Any> readCodecSerializable(key: String, codec: Codec<T>): T?
}

/**
 * Reads the value from the data and replaces the contents of [existing] with the contents of the new data,
 * according to its [ICodecSerializableMutable.copyFrom] method.
 *
 * @return True if the value existed in the saved data, false otherwise.
 */
fun <T : ICodecSerializableMutable<T>> SaveInput.readCodecSerializableToExisting(key: String, existing: T): Boolean {
    return readCodecSerializable(key, existing.codec())?.also {
        existing.copyFrom(it)
    } != null
}

class SaveInput_ValueInput(val input: ValueInput) : SaveInput {
    override fun <T : Any> readCodecSerializable(key: String, codec: Codec<T>): T? {
        return input.read(key, codec).getOrNull()
    }
}

class SaveInput_CompoundTag(val tag: CompoundTag) : SaveInput {
    override fun <T : Any> readCodecSerializable(key: String, codec: Codec<T>): T? {
        return tag.get(key)?.let { codec.decodeTag(it) }
    }
    
}