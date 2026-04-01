package btpos.mcmods.devutil.common.entity.transactions.types

import btpos.mcmods.devutil.common.entity.transactions.ITransaction
import net.minecraft.world.entity.Entity

/**
 * A transaction that fully controls the movement of an entity, such as a dash or cutscene.
 *
 * Removes other [IControlledMovementTransaction]s on the owner, with higher priorities overwriting lower priorities.
 */
interface IControlledMovementTransaction<T : Entity> : ITransaction<T> {
    object StandardPriorities {
        /**
         * A movement action that can occur arbitrarily, like dashing or attacking.
         *
         * These can generally be overwritten without fear.
         */
        const val COMMON_ANIMATION = 0
        
        /**
         * An infrequent thing that fully controls the player's movement for a
         * substantial period and generally disables player controls.
         *
         * Examples:
         * - Viaduct transportation
         * - That one levitator lift in the giant tree from Skyrim's Dragonborn DLC
         *
         * These can still be cancelled semi-freely, but shouldn't be overwritten by many things.
         */
        const val PSEUDO_CUTSCENE = 100
        
        /**
         * An actual cutscene, where the camera is disconnected from the player
         * and all sorts of other things are happening.
         *
         * Nothing should be overwriting these.
         */
        const val CUTSCENE = Integer.MAX_VALUE
        
    }
    // todo figure out if this should be like "movement bracket" + "priority within said bracket" or just an absolute priority system
    val movementPriority: Int
    
    override fun shouldApply(currentTransactions: List<ITransaction<T>>, owner: T): Set<ITransaction<T>>? {
        val out = mutableSetOf<ITransaction<T>>()
        
        currentTransactions.forEach {
            if (it !is IControlledMovementTransaction)
                return@forEach;
            
            if (it.movementPriority > this.movementPriority)
                return null;
            else if (it.movementPriority == this.movementPriority) {
                when (shouldApplySamePriority(it)) {
                    OnSamePriority.NO_APPLY_SELF -> return null;
                    OnSamePriority.REMOVE_OTHER -> out += it;
                    OnSamePriority.COEXIST -> {}
                }
            }
            
            out += it
        }
        
        return out;
    }
    
    @JvmInline value class OnSamePriority private constructor(private val boolean: Boolean?) {
        companion object {
            /**
             * This transaction should fail to apply and leave the other one alone.
             */
            val NO_APPLY_SELF = OnSamePriority(true)
            
            /**
             * The other transaction should be removed from the owner and this one should be applied.
             */
            val REMOVE_OTHER = OnSamePriority(false)
            
            /**
             * This one is applied but the other is left alone.
             */
            val COEXIST = OnSamePriority(null)
        }
    }
    
    /**
     * Optional behavior when a current transaction is found with equal priority to this.
     * Called by [shouldApply].
     *
     * @param transaction The transaction found that has equal priority.
     * @return The behavior to perform.
     * @see OnSamePriority.NO_APPLY_SELF
     * @see OnSamePriority.REMOVE_OTHER
     * @see OnSamePriority.COEXIST
     */
    fun shouldApplySamePriority(transaction: IControlledMovementTransaction<T>): OnSamePriority = OnSamePriority.NO_APPLY_SELF
}