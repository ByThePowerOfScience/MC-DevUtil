@file:Suppress("NOTHING_TO_INLINE")

package btpos.mcmods.devutil.common.dsl

import btpos.mcmods.devutil.common.ext.java.asOptional
import com.mojang.datafixers.kinds.App
import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.nbt.CompoundTag
import net.minecraft.world.level.SpawnData
import java.util.Optional

object CodecBuilderMacros {
	@JvmInline
	value class OptionalFieldMarker(val name: String)
	
	/** Optional field */
	inline operator fun String.unaryMinus(): OptionalFieldMarker = opt(this)
	
	
	inline operator fun <T> String.minus(codec: Codec<T>) = to(codec)
	inline infix fun <T> String.`=`(codec: Codec<T>) = to(codec)
	inline infix fun <T> String.to(codec: Codec<T>): MapCodec<T> = codec.fieldOf(this)
	
	inline infix fun <ENCL, T> String.to(pair: Pair<Codec<T>, (ENCL) -> T>): RecordCodecBuilder<ENCL, T> {
		val (codec, getter) = pair
		return codec.fieldOf(this).forGetter(getter)
	}
	
	inline fun opt(name: String): OptionalFieldMarker = OptionalFieldMarker(name)
	
	inline operator fun <T> OptionalFieldMarker.minus(codec: Codec<T>) = this.to(codec)
	inline infix fun <T> OptionalFieldMarker.to(codec: Codec<T>) = codec.optionalFieldOf(this.name)
	
	
	inline infix fun <ENCL, T> MapCodec<T>.`for getter`(noinline getter: (ENCL) -> T): RecordCodecBuilder<ENCL, T> = this.forGetter(getter)
	inline infix fun <ENCL, T> MapCodec<T>.gets(noinline getter: (ENCL) -> T): RecordCodecBuilder<ENCL, T> = this.forGetter(getter)
	
	// TODO make it so optional fields with nullable getters automatically make the default value null
	
	inline operator fun <ENCL, T> MapCodec<T>.minus(noinline getter: (ENCL) -> T) = this.forGetter(getter)
	
	inline fun <T> codec(noinline action: RecordCodecBuilder.Instance<T>.() -> App<RecordCodecBuilder.Mu<T>, T>): Codec<T> {
		return RecordCodecBuilder.create(action)
	}
}




