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


object test {
	operator fun String.invoke(action: () -> Unit) {
	
	}
	
	class Thing(var x: String, var y: String)
	
	infix fun String.with(closure: Thing.() -> Unit) {
	
	}
	
	
	init {
		"" {
			// omg you can do this!!!!!!
		}
		
		"" with {
			x = "fizz"
			y = "buzz"
		}
	}
}

class CodecDSLTest(val name: String) {
	val mapMembers = mutableListOf<RecordCodecBuilder<*, *>>()
	
	operator fun String.invoke(action: CodecDSLTest.() -> Unit) {
	
	}
	
	@JvmRecord
	data class CodecBuilderThing<T>(val name: String, val type: Codec<T>)
	
	infix fun <T> String.to(value: Codec<T>): CodecBuilderThing<T> {
		return CodecBuilderThing(this, value)
	}
	
	infix fun <T, SUPER> CodecBuilderThing<T>.gets(getter: (SUPER) -> T) {
		mapMembers += this.type.fieldOf(this.name).forGetter(getter)
	}
	
	infix fun <T> String.list(value: Codec<T>) {
	
	}
	
	init {
		"field" {
			"foo" to Codec.BOOL
			"bar" list Codec.BYTE
		}
	}
	
	
	fun build() {
	
	}
}




class MyRecordCodecBuilder<L> {
	val groupMembers = mutableListOf<RecordCodecBuilder<*, *>>() // can't splat the `group` method :(
	var factory: Function<L>? = null
	
	@JvmRecord
	data class FieldIntermediate<T>(val name: String, val codec: Codec<T>)
	
	infix fun <T> String.to(codec: Codec<T>): FieldIntermediate<T> {
		return FieldIntermediate(this, codec)
	}
	
	infix fun <T, SUPER> FieldIntermediate<T>.gets(getter: (SUPER) -> T) {
		val (name, codec) = this
		groupMembers += codec.fieldOf(name).forGetter(getter)
	}
	
	@JvmInline
	value class OptionalFieldIntermediate1(val name: String)
	
	@JvmRecord
	data class OptionalFieldIntermediate<T>(val name: String, val codec: Codec<T>)
	
	infix fun <T> OptionalFieldIntermediate1.to(codec: Codec<T>): OptionalFieldIntermediate<T> = OptionalFieldIntermediate(name, codec)
	
	infix fun <T, SUPER> OptionalFieldIntermediate<T>.gets(getter: (SUPER) -> Optional<T>) {
		val (name, codec) = this
		groupMembers += codec.optionalFieldOf(name).forGetter(getter)
	}
	
	fun optional(name: String): OptionalFieldIntermediate1 {
		return OptionalFieldIntermediate1(name)
	}
}


/*
public static final Codec<SpawnData> CODEC = RecordCodecBuilder.create(
	instance -> instance.group(
			CompoundTag.CODEC.fieldOf("entity").forGetter(spawnData -> spawnData.entityToSpawn),
			SpawnData.CustomSpawnRules.CODEC.optionalFieldOf("custom_spawn_rules").forGetter(spawnData -> spawnData.customSpawnRules)
		)
		.apply(instance, SpawnData::new)
);
 */


//fun foo() {
//	with (CodecBuilderMacros) {
//		codec<SpawnData> {
//			group(
//					"entity" `=` CompoundTag.CODEC `for getter` SpawnData::entityToSpawn,
//					opt("custom_spawn_rules") to SpawnData.CustomSpawnRules.CODEC gets SpawnData::customSpawnRules,
//			).apply(this, ::SpawnData)
//		}
//	}
//}

// region Object Method :(
class Builder<SUPER, T> {
	lateinit var codec: Codec<T>
	lateinit var getter: (SUPER) -> T
}

operator fun <SUPER, T> String.invoke(builder: Builder<SUPER, T>.() -> Unit) {
	val x = Builder<SUPER, T>().also(builder)
}

fun foo2() {
	"" {
		codec = CompoundTag.CODEC
		getter = SpawnData::entityToSpawn
	}
}
// endregion Object Method :(

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
}


inline fun <T> codec(noinline action: RecordCodecBuilder.Instance<T>.() -> App<RecordCodecBuilder.Mu<T>, T>): Codec<T> {
	return RecordCodecBuilder.create(action)
}

