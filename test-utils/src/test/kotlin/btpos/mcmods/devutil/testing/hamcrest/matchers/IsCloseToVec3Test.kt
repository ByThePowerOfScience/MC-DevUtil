package btpos.mcmods.devutil.testing.hamcrest.matchers

import net.minecraft.world.phys.Vec3
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers
import org.hamcrest.StringDescription
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import kotlin.test.assertContains

class IsCloseToVec3Test {
    @Test
    fun `true when exact match`() {
        assertTrue(closeTo(Vec3.ZERO, 1.0).matches(Vec3(0.0, 0.0, 0.0)))
    }
    
    @Test
    fun `true when exact match with 0 margin`() {
        assertTrue(closeTo(Vec3.ZERO, 0.0).matches(Vec3(0.0, 0.0, 0.0)))
    }
    
    @Test
    fun `true when diff within delta`() {
        assertTrue(closeTo(Vec3.ZERO, 1.0).matches(Vec3(0.999, 0.999, 0.999)))
    }
    
    @Test
    fun `true when diff exactly delta`() {
        assertTrue(closeTo(Vec3.ZERO, 1.0).matches(Vec3(1.0, 1.0, 1.0)))
    }
    
    @Test
    fun `false when x diff over delta`() {
        assertFalse(closeTo(Vec3.ZERO, 1.0).matches(Vec3(4.0,0.0,0.0)))
    }
    
    @Test
    fun `only gives x desc when x diff over delta`() {
        val desc = StringDescription()
        closeTo(Vec3.ZERO, 1.0).describeMismatch(Vec3(4.0,1.0,0.0), desc)
        val s = desc.toString()
        assertContains(s, "x value")
        assertThat(s, Matchers.not(Matchers.containsString("y value")))
        assertThat(s, Matchers.not(Matchers.containsString("z value")))
    }
    
    @Test
    fun `false when y diff over delta`() {
        assertFalse(closeTo(Vec3.ZERO, 1.0).matches(Vec3(0.0,4.0,0.0)))
    }
    
    @Test
    fun `only gives y desc when y diff over delta`() {
        val desc = StringDescription()
        closeTo(Vec3.ZERO, 1.0).describeMismatch(Vec3(1.0,4.0,0.0), desc)
        val s = desc.toString()
        assertThat(s, Matchers.not(Matchers.containsString("x value")))
        assertContains(s, "y value")
        assertThat(s, Matchers.not(Matchers.containsString("z value")))
    }
    
    @Test
    fun `false when z diff over delta`() {
        assertFalse(closeTo(Vec3.ZERO, 1.0).matches(Vec3(0.0,0.0,4.0)))
    }
    
    @Test
    fun `only gives z desc when z diff over delta`() {
        val desc = StringDescription()
        closeTo(Vec3.ZERO, 1.0).describeMismatch(Vec3(1.0,0.0,4.0), desc)
        val s = desc.toString()
        assertThat(s, Matchers.not(Matchers.containsString("x value")))
        assertThat(s, Matchers.not(Matchers.containsString("y value")))
        assertContains(s, "z value")
    }
    
    @Test
    fun `desc gives xy when both xy over delta`() {
        val desc = StringDescription()
        assertFalse(closeTo(Vec3.ZERO, 1.0).matches(Vec3(4.0,4.0, 0.0)))
        closeTo(Vec3.ZERO, 1.0).describeMismatch(Vec3(4.0,4.0, 0.0), desc)
        val s = desc.toString()
        assertContains(s, "x value")
        assertContains(s, "y value")
        assertThat(s, Matchers.not(Matchers.containsString("z value")))
    }
    
    @Test
    fun `desc gives yz when both yz over delta`() {
        val desc = StringDescription()
        assertFalse(closeTo(Vec3.ZERO, 1.0).matches(Vec3(0.0, 4.0, 4.0)))
        closeTo(Vec3.ZERO, 1.0).describeMismatch(Vec3(0.0, 4.0, 4.0), desc)
        val s = desc.toString()
        assertThat(s, Matchers.not(Matchers.containsString("x value")))
        assertContains(s, "y value")
        assertContains(s, "z value")
    }
    
    @Test
    fun `desc gives xz when both xz over delta`() {
        val desc = StringDescription()
        assertFalse(closeTo(Vec3.ZERO, 1.0).matches(Vec3(4.0, 0.0, 4.0)))
        closeTo(Vec3.ZERO, 1.0).describeMismatch(Vec3(4.0, 0.0, 4.0), desc)
        val s = desc.toString()
        assertContains(s, "x value")
        assertThat(s, Matchers.not(Matchers.containsString("y value")))
        assertContains(s, "z value")
    }
    
    @Test
    fun `false when all diff over delta`() {
        
        val desc = StringDescription()
        assertFalse(closeTo(Vec3.ZERO, 1.0).matches(Vec3(4.0, 5.0, 6.0)))
        closeTo(Vec3.ZERO, 1.0).describeMismatch(Vec3(4.0, 5.0, 6.0), desc)
        val s = desc.toString()
        assertContains(s, "x value")
        assertContains(s, "y value")
        assertContains(s, "z value")
    }
}