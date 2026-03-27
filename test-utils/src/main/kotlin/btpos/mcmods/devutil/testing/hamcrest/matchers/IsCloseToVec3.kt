package btpos.mcmods.devutil.testing.hamcrest.matchers

import btpos.mcmods.devutil.testing.hamcrest.utils.appendMismatchList
import net.minecraft.world.phys.Vec3
import org.hamcrest.Description
import org.hamcrest.FeatureMatcher
import org.hamcrest.TypeSafeMatcher
import org.hamcrest.number.IsCloseTo

class IsCloseToVec3(private val expected: Vec3, private val error: Double) : TypeSafeMatcher<Vec3>(Vec3::class.java) {
    private class CoordinateMatcher(name: String, expected: Vec3, pError: Double, val coord: Vec3.() -> Double) : FeatureMatcher<Vec3, Double>(IsCloseTo(expected.coord(), pError), "$name coordinate of vector", "$name value") {
        override fun featureValueOf(actual: Vec3): Double {
            return actual.coord()
        }
    }
    
    private val matchers = listOf(
        CoordinateMatcher("x", expected, error) { x },
        CoordinateMatcher("y", expected, error) { y },
        CoordinateMatcher("z", expected, error) { z }
    )
    
    override fun describeMismatchSafely(item: Vec3, mismatchDescription: Description) {
        mismatchDescription.appendMismatchList(matchers, item)
    }
    
    override fun matchesSafely(actual: Vec3): Boolean {
        return matchers.all { it.matches(actual) }
    }
    
    override fun describeTo(p0: Description) {
        p0.appendText("a vector where each coordinate is within ").appendValue(error).appendText(" of ").appendValue(expected)
    }
}