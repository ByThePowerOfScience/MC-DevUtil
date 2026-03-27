package btpos.mcmods.devutil.testing.hamcrest.matchers

import net.minecraft.world.phys.Vec3

/**
 * Checks that each coordinate of the [expected] and "actual" vectors are within [error] of one another.
 */
fun closeTo(expected: Vec3, error: Double) = IsCloseToVec3(expected, error)