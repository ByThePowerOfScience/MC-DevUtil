package btpos.mcmods.devutil.common.util

import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.projectile.ProjectileUtil
import net.minecraft.world.phys.EntityHitResult
import java.util.*
import java.util.function.Predicate

object EntityUtils {
    /**
     * Gets the entity this entity is looking at, if any.
     *
     * @see net.minecraft.client.renderer.debug.DebugRenderer.getTargetedEntity
     */
    fun Entity.getTargetedEntity(pDistance: Double): Entity? {
        val vec3 = this.getEyePosition()
        val vec31 = this.getViewVector(1.0f).scale(pDistance)
        val vec32 = vec3.add(vec31)
        val aabb = this.getBoundingBox().expandTowards(vec31).inflate(1.0)
        val i = pDistance * pDistance
        val predicate = Predicate { it: Entity -> !it.isSpectator() && it.isPickable() }
        
        val entityhitresult = ProjectileUtil.getEntityHitResult(this, vec3, vec32, aabb, predicate, i)
        
        if (entityhitresult == null) {
            return null
        } else {
            if (vec3.distanceToSqr(entityhitresult.getLocation()) > i)
                return null 
            else 
                return entityhitresult.entity
        }
    }
}