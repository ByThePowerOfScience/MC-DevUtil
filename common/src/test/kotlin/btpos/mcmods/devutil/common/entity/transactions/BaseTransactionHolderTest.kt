package btpos.mcmods.devutil.common.entity.transactions

import btpos.mcmods.devutil.common.entity.transactions.holders.BaseTransactionHolder
import btpos.mcmods.devutil.testing.mockito.spy
import net.minecraft.world.phys.Vec3
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoMoreInteractions
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.inOrder
import org.mockito.kotlin.spy
import kotlin.test.assertContains

/**
 * You may see the number of "[tickTransactions][BaseTransactionHolder.tickTransactions] then [verify]" for checking
 * if a transaction is in the holder and go "what the hell are you doing???"
 *
 * However, I'm specifically _avoiding_ exposing [BaseTransactionHolder.transactions] because **that's not the contract**.
 * The contract of [BaseTransactionHolder.addTransaction] is that from then on,
 * [BaseTransactionHolder.tickTransactions] will call [ITransaction.onTick].
 * That's the contract.
 *
 * The backend implementation shouldn't be exposed unnecessarily because _that's not part of the deal we make_.
 *
 * And yes, I came to this conclusion only after going "this is stupid" _myself_, but I think it's worth it.
 * Theoretically, any transaction holder should be able to use these exact same tests.
 */
@ExtendWith(MockitoExtension::class)
class BaseTransactionHolderTest {
    private val owner = Owner()
    private val transactionHolder = BaseTransactionHolder<Owner>(owner)
    
    data class Owner(var position: Vec3 = Vec3.ZERO)
    open class Transaction : ITransaction<Owner> {
        override val isRequestingCancel: Boolean
            get() = false
        
        override fun onTick(affected: Owner) {}
    }
    
    /**
     * Basic contract:
     * - [BaseTransactionHolder.addTransaction] calls [ITransaction.shouldApply]
     * - after that, [BaseTransactionHolder.tickTransactions] calls [ITransaction.onTick]
     */
    @Test
    fun `addTransaction - transactions added and tickable`() {
        val tsx = spy<Transaction>()
        
        transactionHolder.addTransaction(tsx)
        verify(tsx).shouldApply(any(), any())
        
        transactionHolder.tickTransactions()
        verify(tsx).onTick(owner)
    }
    
    @Test
    fun `addTransaction - all transactions given in list`() {
        val first = Transaction()
        val second = Transaction()
        
        transactionHolder.addTransaction(first)
        transactionHolder.addTransaction(second)
        
        
        class Tsx : Transaction() {
            override fun shouldApply(currentTransactions: List<ITransaction<Owner>>, owner: Owner): Set<ITransaction<Owner>>? {
                assertContains(currentTransactions, first)
                return null;
            }
        }
        
        transactionHolder.addTransaction(Tsx())
    }
    
    @Test
    fun `tickTransactions - cancel on isRequestingCancel after tick`() {
        val tsx = spy<Transaction> {
            on { isRequestingCancel } doReturn true
        }
        
        transactionHolder.addTransaction(tsx)
        
        transactionHolder.tickTransactions()
        tsx.inOrder {
            verify().onTick(owner)
            verify().isRequestingCancel
            verify().revert(ETransactionCancelReason.SELF_REQUESTED_TERMINATION, owner)
            verifyNoMoreInteractions()
        }
    }
    
    @Test
    fun `addTransaction - doesnt add if shouldApply returns null`() {
        val tsx = spy(object : Transaction() {
            override fun shouldApply(currentTransactions: List<ITransaction<Owner>>, owner: Owner) = null
        })
        
        transactionHolder.addTransaction(tsx)
        verify(tsx).shouldApply(any(), any())
        
        transactionHolder.tickTransactions()
        
        transactionHolder.onACancellingChange(ETransactionCancelReason.SERVER_STOP)
        verifyNoMoreInteractions(tsx)
    }
    
    /**
     * [BaseTransactionHolder.addTransaction] calls [ITransaction.shouldApply], and calling `remove` on its `currentTransactions` parameter
     * calls [ITransaction.revert] on the item with [ETransactionCancelReason.OTHER_REQUESTED_TERMINATION].
     */
    @Test
    fun `addTransaction - overwriting other transactions`() {
        val toBeOverwritten = spy(Transaction(), name="toBeOverwritten")
        
        class OverwritingTsx : Transaction() {
            override fun shouldApply(currentTransactions: List<ITransaction<Owner>>, owner: Owner): Set<ITransaction<Owner>> {
                assertContains(currentTransactions, toBeOverwritten, "contains transaction to be overwritten")
                return currentTransactions.toSet()
            }
        }
        
        
        val overwrites = spy(OverwritingTsx(), name="overwrites")
        
        transactionHolder.addTransaction(toBeOverwritten)
        verify(toBeOverwritten).shouldApply(any(), any())
        
        transactionHolder.addTransaction(overwrites)
        verify(overwrites).shouldApply(any(), any())
        verify(toBeOverwritten).revert(ETransactionCancelReason.OTHER_REQUESTED_TERMINATION, owner)
        
        transactionHolder.tickTransactions()
        verify(overwrites).onTick(owner)
        verifyNoMoreInteractions(toBeOverwritten)
        
        transactionHolder.onACancellingChange(ETransactionCancelReason.OWNER_TELEPORTED)
        verify(overwrites).shouldRevert(ETransactionCancelReason.OWNER_TELEPORTED, owner)
        verifyNoMoreInteractions(toBeOverwritten)
    }
    
    @Test
    fun `onACancellingChange - no cancel if shouldRevert returns false`() {
        val tsx = spy<Transaction> {
            onGeneric { shouldRevert(ETransactionCancelReason.OWNER_TELEPORTED, owner) } doReturn false
            onGeneric { shouldRevert(ETransactionCancelReason.OWNER_CHANGED_DIMENSION, owner) } doReturn true
        }
        
        transactionHolder.addTransaction(tsx)
        transactionHolder.tickTransactions()
        
        tsx.inOrder {
            transactionHolder.onACancellingChange(ETransactionCancelReason.OWNER_TELEPORTED)
            verify().shouldRevert(ETransactionCancelReason.OWNER_TELEPORTED, owner)
            verifyNoMoreInteractions()
            
            transactionHolder.onACancellingChange(ETransactionCancelReason.OWNER_CHANGED_DIMENSION)
            verify().shouldRevert(ETransactionCancelReason.OWNER_CHANGED_DIMENSION, owner)
            verify().revert(ETransactionCancelReason.OWNER_CHANGED_DIMENSION, owner)
            verifyNoMoreInteractions()
            
            // ensure the transaction was removed:
            transactionHolder.tickTransactions()
            transactionHolder.onACancellingChange(ETransactionCancelReason.SELF_REQUESTED_TERMINATION)
            verifyNoMoreInteractions()
        }
    }
    
    @Test
    fun `tickTransactions - transactions can actually modify owner`() {
        val newpos = Vec3(0.0, 1.0, 0.0)
        
        val tsx = object : Transaction() {
            override fun onTick(affected: Owner) {
                affected.position = newpos
            }
        }
        
        transactionHolder.addTransaction(tsx)
        transactionHolder.tickTransactions()
        
        assertEquals(owner.position, newpos)
    }
}