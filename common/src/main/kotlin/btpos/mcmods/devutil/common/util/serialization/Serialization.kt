package btpos.mcmods.devutil.common.util.serialization

import btpos.mcmods.devutil.common.ext.vanilla.world.aabbOf
import btpos.mcmods.devutil.common.ext.vanilla.world.getMaxCorner
import btpos.mcmods.devutil.common.ext.vanilla.world.getMaxCornerBlock
import btpos.mcmods.devutil.common.ext.vanilla.world.getMinCorner
import btpos.mcmods.devutil.common.ext.vanilla.world.getMinCornerBlock
import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.core.BlockPos
import net.minecraft.nbt.NbtOps
import net.minecraft.nbt.Tag
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.Vec3
import org.slf4j.LoggerFactory

private val LOGGER = LoggerFactory.getLogger("btpos Serialization")

object Serialization {
	val CODEC_AABB: Codec<AABB> = RecordCodecBuilder.create {
		it.group(
				Vec3.CODEC.fieldOf("first").forGetter { aabb: AABB -> aabb.getMinCorner() },
				Vec3.CODEC.fieldOf("second").forGetter { aabb: AABB -> aabb.getMaxCorner() }
		).apply(it, ::AABB)
	}
	val CODEC_AABB_BLOCK: Codec<AABB> = RecordCodecBuilder.create {
		it.group(
				BlockPos.CODEC.fieldOf("first").forGetter { aabb: AABB -> aabb.getMinCornerBlock() },
				BlockPos.CODEC.fieldOf("second").forGetter { aabb: AABB -> aabb.getMaxCornerBlock() }
		).apply(it, { pos1, pos2 -> aabbOf(pos1, pos2) })
	}
	
	fun <T> Codec<T>.decodeTag(tag: Tag): T {
		return this.decode(NbtOps.INSTANCE, tag).getOrThrow().first
	}
	
	fun <T> Codec<T>.encodeToTag(item: T): Tag {
		return this.encodeStart(NbtOps.INSTANCE, item).getOrThrow()
	}
	
	/**
	 * A pair codec that actually supports "primitive" values (e.g. BlockPos). Idk why it only supports compounds natively.
	 */
	fun <A, B> pairCodec(first: Codec<A>, second: Codec<B>) = PrimitiveAblePairCodec(first, second)
}