package btpos.unittest.dungeondesigner.utils

import btpos.mcmods.devutil.common.ext.vanilla.world.aabbOf
import btpos.mcmods.devutil.common.util.serialization.Serialization
import btpos.mcmods.devutil.common.util.serialization.Serialization.decodeTag
import btpos.mcmods.devutil.common.util.serialization.Serialization.encodeToTag
import net.minecraft.core.BlockPos
import net.minecraft.nbt.CompoundTag
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.Vec3
import org.junit.jupiter.api.Test
import kotlin.test.*

class CodecAABB {
	@Test
	fun standard_totag() {
		val v1 = Vec3(0.0, 0.0, 0.0)
		val v2 = Vec3(1.0, 1.0, 1.0)
		val bb = AABB(v1, v2)
		
		val expected = CompoundTag().apply {
			put("first", Vec3.CODEC.encodeToTag(v1))
			put("second", Vec3.CODEC.encodeToTag(v2))
		}
		
		val actual = Serialization.CODEC_AABB.encodeToTag(bb)
		
		assertEquals(expected, actual)
	}
	
	@Test
	fun standard_fromtag() {
		val v1 = Vec3(0.0, 0.0, 0.0)
		val v2 = Vec3(1.0, 1.0, 1.0)
		val expected = AABB(v1, v2)
		
		val tag = CompoundTag().apply {
			put("first", Vec3.CODEC.encodeToTag(v1))
			put("second", Vec3.CODEC.encodeToTag(v2))
		}
		
		val actual = Serialization.CODEC_AABB.decodeTag(tag)
		
		assertEquals(expected, actual)
	}
	
	@Test
	fun block_totag() {
		val v1 = BlockPos(0, 0, 0)
		val v2 = BlockPos(1, 1, 1)
		val bb = aabbOf(v1, v2)
		
		val expected = CompoundTag().apply {
			put("first", BlockPos.CODEC.encodeToTag(v1))
			put("second", BlockPos.CODEC.encodeToTag(v2))
		}
		
		val actual = Serialization.CODEC_AABB_BLOCK.encodeToTag(bb)
		
		assertEquals(expected, actual)
	}
	
	@Test
	fun block_fromtag() {
		val v1 = BlockPos(0, 0, 0)
		val v2 = BlockPos(1, 1, 1)
		val expected = aabbOf(v1, v2)
		
		val tag = CompoundTag().apply {
			put("first", BlockPos.CODEC.encodeToTag(v1))
			put("second", BlockPos.CODEC.encodeToTag(v2))
		}
		
		val actual = Serialization.CODEC_AABB_BLOCK.decodeTag(tag)
		
		assertEquals(expected, actual)
	}
}