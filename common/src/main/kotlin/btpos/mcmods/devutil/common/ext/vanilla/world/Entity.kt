@file:OptIn(ExperimentalContracts::class)

package btpos.mcmods.devutil.common.ext.vanilla.world

import net.minecraft.world.entity.Entity
import net.minecraft.world.phys.Vec3
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

/**
 * Applies the transformer function to the entity's deltaMovement, and sets the entity's deltaMovement to the result.
 *
 * @see Entity.getDeltaMovement
 * @see Entity.setDeltaMovement
 */
inline fun Entity.modifyDeltaMovement(transformer: (Vec3) -> Vec3) {
	contract {
		callsInPlace(transformer, InvocationKind.EXACTLY_ONCE)
	}
	
	deltaMovement = transformer(deltaMovement)
}