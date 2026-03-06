package btpos.mcmods.devutil.common.util.serialization

import btpos.mcmods.devutil.common.ext.vanilla.data.set
import btpos.mcmods.devutil.common.ext.vanilla.destructuring.component1
import btpos.mcmods.devutil.common.ext.vanilla.destructuring.component2
import btpos.mcmods.devutil.common.structure.program.IReverseCloneable
import com.mojang.serialization.Codec
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.NbtOps
import net.minecraft.world.level.storage.ValueInput
import net.minecraft.world.level.storage.ValueOutput
import org.jetbrains.annotations.Contract
import org.slf4j.LoggerFactory

private val LOGGER = LoggerFactory.getLogger("btpos - ICodecSerializable")

/**
 * Platform-agnostic NBT serialization helper using codecs.
 *
 * This is for IMMUTABLE OBJECTS only.  For mutable objects, see [ICodecSerializableMutable].
 *
 * Encodes/decodes the data of `this` to and from a [CompoundTag] using the [Codec] provided with [codec].
 */
interface ICodecSerializable<SELF : ICodecSerializable<SELF>>//, INbtSerializable
{
    @Suppress("UNCHECKED_CAST")
    private fun self(): SELF = this as SELF
    
    fun codec(): Codec<SELF>
}

interface ICodecSerializableMutable<SELF>
    : ICodecSerializable<SELF>, IReverseCloneable<SELF>
    where SELF : ICodecSerializable<SELF>