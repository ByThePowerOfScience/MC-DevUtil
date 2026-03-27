package btpos.mcmods.devutil.common.util.math

import net.minecraft.core.Direction
import net.minecraft.world.phys.Vec3

/**
 * Marker class asserting that the contained [Vec3] instance is a unit vector. (i.e. has a length of 1.0)
 *
 * Useful for passing around an object without having to recheck its length or re-normalize it each time before using it.
 */
@JvmInline value class UnitVec3 private constructor(val vec: Vec3) {
    companion object {
        /**
         * If [vector] is a unit vector, returns a [UnitVec3] wrapping it, else null.
         */
        operator fun invoke(vector: Vec3): UnitVec3? {
            if (vector.lengthSqr() != 1.0)
                return null;
            return UnitVec3(vec=vector)
        }
        
        /**
         * Normalizes [vector] and returns it wrapped in an assertion.
         */
        fun normalizing(vector: Vec3) = unchecked(vector.normalize())
        
        /**
         * Assert that you've already checked that this vector is a unit vector.
         *
         * You should have already checked that this vector was normalized before calling.
         * Calling this method with a non-unit vector will caused unexpected behavior.
         */
        fun unchecked(vector: Vec3) = UnitVec3(vec=vector)
    }
}

val Direction.unitVec3_wrapped: UnitVec3
    get() = UnitVec3.unchecked(unitVec3)