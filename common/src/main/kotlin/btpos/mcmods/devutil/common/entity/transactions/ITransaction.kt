package btpos.mcmods.devutil.common.entity.transactions

import org.jetbrains.annotations.Contract

/**
 * A "transaction", in this case, is some ongoing action that can be cancelled in the middle,
 * and has some behavior that needs to be performed when it's cancelled to fully- or partially-revert it.
 *
 * For example, a player disconnecting while on an elevator should always return
 * to the start point of said elevator instead of the position they were at when they disconnected.
 */
interface ITransaction<in T : Any> {
    /**
     * True if this transaction should be removed from the list of active transactions on its owner,
     * false otherwise.
     *
     * Essentially a "status" indicator for the transaction itself.
     *
     * If true, [shouldRevert] will immediately be called with [ETransactionCancelReason.SELF_REQUESTED_TERMINATION].
     */
    val isRequestingCancel: Boolean
    
    /**
     * View the other transactions currently applied to this owner and possibly do one of the following:
     * - Fail to apply yourself to the owner (return `null`)
     * - Cancel other transactions from the owner's set after you are applied (return set of transactions; holder will call [revert] on them with [ETransactionCancelReason.OTHER_REQUESTED_TERMINATION])
     *
     * This method will only be called a single time (during [btpos.mcmods.devutil.common.entity.transactions.holders.ITransactionHolder.addTransaction]),
     * so you can safely adapt to the presence of other transactions in the container.
     *
     * @param currentTransactions The transactions currently applied to the owner.
     * @param owner The entity that contains this and the other transactions. **MUST NOT BE MODIFIED IN THIS METHOD**
     *
     * @see
     */
    @Contract(mutates="this")
    fun shouldApply(currentTransactions: List<ITransaction<@UnsafeVariance T>>, owner: T): Set<ITransaction<T>>? = emptySet()
    
    
    
    /**
     * Progress this transaction by a single tick.
     *
     * Transactions should keep track of their own progress and update [isRequestingCancel]
     * once their operation has completed or if a failure condition is met.
     *
     * Implementers _should not cache [affected]_.
     *
     * @param affected The entity or player that owns and is affected by this transaction.
     */
    @Contract(mutates = "this,param1")
    fun onTick(affected: T)
    
    /**
     * Called when the owner of this transaction has done something that could cancel a transaction.
     *
     * Implementers _should not modify [affected] here_.  Only perform reversion procedures in [revert].
     *
     * @param reason The reason this transaction may wish to be cancelled. See [ETransactionCancelReason] for more details.
     * @param affected The entity or player that owns and is affected by this transaction.
     * @return True if this transaction should be removed from the list of active transactions, false otherwise.
     *         Return false if you want to ignore this cancel reason, essentially.
     *         (This method will not be called if it is a forced termination. See [ETransactionCancelReason.isForcedCancel].)
     */
    @Contract(pure = true)
    fun shouldRevert(reason: ETransactionCancelReason, affected: T): Boolean = true
    
    /**
     * Implementers should perform any hard- or soft-reversion behavior here.
     * This method will only be called for non-forced terminations after
     * [shouldRevert] has been called and returned `true`.
     *
     * For example, cutscenes will want to fully reset the player position only if [ETransactionCancelReason.ownerCannotMoveAfterThis] is true,
     * but unset the "has cutscene played" flag alone if the owner was teleported.
     *
     * @param reason The reason this transaction may wish to be cancelled. See [ETransactionCancelReason] for more details.
     * @param affected The entity or player that owns and is affected by this transaction.
     */
    @Contract(mutates = "param2")
    fun revert(reason: ETransactionCancelReason, affected: T) {}
}