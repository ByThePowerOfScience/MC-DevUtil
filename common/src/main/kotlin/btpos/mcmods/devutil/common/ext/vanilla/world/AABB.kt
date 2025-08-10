package btpos.mcmods.devutil.common.ext.vanilla.world

import net.minecraft.core.BlockPos
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.Vec3
import org.jetbrains.annotations.Contract

fun AABB.getMinCorner(): Vec3 {
	return Vec3(minX, minY, minZ)
}

fun AABB.getMaxCorner(): Vec3 {
	return Vec3(maxX, maxY, maxZ)
}

fun AABB.getMinCornerBlock(): BlockPos {
	return BlockPos(minX.toInt(), minY.toInt(), minZ.toInt())
}

fun AABB.getMaxCornerBlock(): BlockPos {
	return BlockPos(maxX.toInt(), maxY.toInt(), maxZ.toInt())
}

/**
 * Marker class to assert that this AABB is *endpoint-inclusive*, meaning it *fully covers* both blocks at its corners instead of stopping at the beginning of one of them.
 */
@JvmInline
value class BlockInclusiveAABB private constructor(val bb: AABB) {
	fun toBlockExclusiveAABB(): AABB {
		return bb.apply {
			maxX -= 1
			maxY -= 1
			maxZ -= 1
		}
	}
	
	companion object {
		/**
		 * Converts an Axis Aligned Bounding Box to fully cover the blocks at its corners.
		 */
		@Contract(pure=true)
		fun AABB.toBlockInclusive(): BlockInclusiveAABB {
			val new = AABB(minX, minY, minZ, maxX + 1, maxY + 1, maxZ + 1)
			
			return BlockInclusiveAABB(new)
			/*
            Cases:
            1. if both are in the same quadrant, or if one is in (-x, -z) and the other in (+x, +z), or if at least one of the axes has the same sign:
                 - whichever is closest to (+inf,+inf), add (1,1)
            2. if one is in (-x, +z) and the other is in (+x, -z):
                - this can never happen because it's always turned to the former category, so literally just add 1 to all maxes
            */
		}
	}
}

fun aabbOf(pos1: BlockPos, pos2: BlockPos): AABB {
	return AABB(pos1.x.toDouble(), pos1.y.toDouble(), pos1.z.toDouble(), pos2.x.toDouble(), pos2.y.toDouble(), pos2.z.toDouble())
}