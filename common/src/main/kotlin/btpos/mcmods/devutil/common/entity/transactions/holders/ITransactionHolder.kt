package btpos.mcmods.devutil.common.entity.transactions.holders

import btpos.mcmods.devutil.common.entity.transactions.ETransactionCancelReason
import btpos.mcmods.devutil.common.entity.transactions.ITransaction
import net.minecraft.world.entity.Entity
import org.jetbrains.annotations.ApiStatus

/**
 * Holds and ticks [ITransactions][btpos.mcmods.devutil.common.entity.transactions.ITransaction].
 *
 * You'd think this should be a component, but transactions are exactly the opposite and _should not_ be persisted.
 * ...maybe it should be a component anyway.
 *
 * Implementers will need to retain a reference to their owner to accurately pass it to each method.
 *
 * @param T The owner of these transactions: the entity or player affected by them.
 */
interface ITransactionHolder<T : Any> {
    /**
     * Add a transaction to this container, to be ticked along with the owner.
     *
     * Transactions will have their [btpos.mcmods.devutil.common.entity.transactions.ITransaction.shouldApply] method invoked a single time with the current contents of this holder.
     */
    fun addTransaction(transaction: ITransaction<T>)
    
    /**
     * Ticks all transactions in this holder.
     */
    @ApiStatus.Internal
    fun tickTransactions()
    
    /**
     * Invoked when a change occurs to the owner that can invalidate transactions.
     *
     * @see btpos.mcmods.terminus.common.events.listeners.XplatTransactionHandlers XplatTransactionHandlers
     */
    fun onACancellingChange(cancelReason: ETransactionCancelReason)
}

/**
 * To be injected into classes via Mixin.
 */
internal interface ITransactionHolder_Injected<SELF : Entity> {
    fun `terminus$getTransactions`(): ITransactionHolder<SELF>
}


