package btpos.mcmods.devutil.common.entity.transactions.holders

import btpos.mcmods.devutil.common.entity.transactions.ETransactionCancelReason
import btpos.mcmods.devutil.common.entity.transactions.ITransaction

/**
 * Basic implementation of [ITransactionHolder].
 *
 * @param owner A reference to the owner of this transaction.
 */
class BaseTransactionHolder<T : Any>(private val owner: T) : ITransactionHolder<T> {
    private val transactions: MutableList<ITransaction<T>> = ArrayList(0)
    
    override fun addTransaction(transaction: ITransaction<T>) {
        val toRemove = transaction.shouldApply(transactions, owner)
            ?: return;
        
        transactions.removeIf {
            if (it in toRemove) {
                it.revert(ETransactionCancelReason.OTHER_REQUESTED_TERMINATION, owner)
                return@removeIf true;
            }
            
            return@removeIf false;
        }
        
        transactions.add(transaction)
    }
    
    override fun tickTransactions() {
        if (transactions.isEmpty())
            return;
        
        transactions.removeIf {
            it.onTick(owner)
            
            if (it.isRequestingCancel) {
                it.revert(ETransactionCancelReason.SELF_REQUESTED_TERMINATION, owner)
                return@removeIf true;
            }
            
            return@removeIf false;
        }
    }
    
    override fun onACancellingChange(cancelReason: ETransactionCancelReason) {
        transactions.removeIf {
            if (cancelReason.isForcedCancel || it.shouldRevert(cancelReason, owner)) {
                it.revert(cancelReason, owner)
                return@removeIf true;
            }
            return@removeIf false;
        }
    }
}