package btpos.mcmods.devutil.common.entity.transactions

/**
 * A pseudo-enum representing a reason a transaction may want to be cancelled.
 * Instances should be immutable globals created at program start.
 *
 * This is an open class to be extensible for any addons adding new cancellation reasons.
 * Anything needed for Terminus' transactions will have to be accounted for in any subclasses,
 * so there's no risk of breaking base-mod logic with new implementations like there would be with pattern-matching.
 *
 * "Reason" examples include the transaction owner:
 * - Leaving the server
 * - Dying
 * - Changing dimensions
 * - Dismounting
 * - Being teleported
 *
 * @see ITransaction.shouldRevert
 */
open class ETransactionCancelReason(
    /**
     * If true, the transaction will _always_ be removed from this owner.
     *
     * Only appears in [ITransaction.revert].
     */
    val isForcedCancel: Boolean = false,
    /**
     * If true, the owner of this transaction cannot be moved by outside forces.
     *
     * Forced-movement sequences will want to fully revert the owner's position in these cases,
     * as nothing will be able to move the owner after this reason is given.
     *
     * Examples include:
     * - Dying
     * - Leaving the server
     */
    val ownerCannotMoveAfterThis: Boolean = isForcedCancel,
    val hasOwnerChangedDimension: Boolean = false,
    val hasOwnerTeleported: Boolean = hasOwnerChangedDimension
) {
    companion object {
        // Only these should be pattern-matched against.
        val SELF_REQUESTED_TERMINATION = ETransactionCancelReason(isForcedCancel=true)
        
        /**
         * Another transaction was applied to the owner and requested your removal in its [ITransaction.shouldApply] implementation.
         */
        val OTHER_REQUESTED_TERMINATION = ETransactionCancelReason(isForcedCancel=true)
        
        // These should only be matched by their attributes, not directly.
        val PLAYER_LOGGED_OUT = ETransactionCancelReason(isForcedCancel = true, ownerCannotMoveAfterThis = true)
        val OWNER_KILLED = ETransactionCancelReason(ownerCannotMoveAfterThis = true)
        val SERVER_STOP = ETransactionCancelReason(isForcedCancel = true)
        val OWNER_CHANGED_DIMENSION = ETransactionCancelReason(hasOwnerChangedDimension = true)
        val OWNER_TELEPORTED = ETransactionCancelReason(hasOwnerTeleported = true)
    }
}