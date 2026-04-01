package btpos.mcmods.devutil.common.entity.transactions

import btpos.mcmods.devutil.common.entity.transactions.holders.BaseTransactionHolder
import btpos.mcmods.devutil.common.entity.transactions.types.IControlledMovementTransaction
import btpos.mcmods.devutil.testing.mockito.spy
import net.minecraft.world.entity.Entity
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.never
import org.mockito.kotlin.verify

@ExtendWith(MockitoExtension::class)
class IControlledMovementTransactionTest {
    
    open class TsxPriority(override val movementPriority: Int) : IControlledMovementTransaction<Entity> {
        override val isRequestingCancel: Boolean
            get() = false
        
        override fun onTick(affected: Entity) {}
    }
    
    private val holder by lazy(LazyThreadSafetyMode.NONE) { BaseTransactionHolder(owner) }
    
    
    @Mock
    private lateinit var owner: Entity
    
    @Test
    fun `removes lower priority ones when added`() {
        val lowerPriority = spy(TsxPriority(0), name="lowerPriority")
        val higherPriority = spy(TsxPriority(1), name="higherPriority")
        
        holder.addTransaction(lowerPriority)
        
        holder.addTransaction(higherPriority)
        verify(lowerPriority).revert(ETransactionCancelReason.OTHER_REQUESTED_TERMINATION, owner)
        
        holder.tickTransactions()
        verify(lowerPriority, never()).onTick(any())
        verify(higherPriority).onTick(owner)
    }
    
    @Test
    fun `balks when higher priority ones already exist`() {
        val lowerPriority = spy(TsxPriority(0), name="lowerPriority")
        val higherPriority = spy(TsxPriority(1), name="higherPriority")
        
        holder.addTransaction(higherPriority)
        
        holder.addTransaction(lowerPriority)
        
        verify(higherPriority, never()).revert(ETransactionCancelReason.OTHER_REQUESTED_TERMINATION, owner)
        
        holder.tickTransactions()
        verify(lowerPriority, never()).onTick(any())
        verify(higherPriority).onTick(owner)
    }
    
    @Test
    fun `balks at same-priority ones too`() {
        val addedFirst = spy(TsxPriority(0), name="addedFirst")
        val addedSecond = spy(TsxPriority(0), name="addedSecond")
        
        holder.addTransaction(addedFirst)
        holder.addTransaction(addedSecond)
        
        verify(addedFirst, never()).revert(ETransactionCancelReason.OTHER_REQUESTED_TERMINATION, owner)
        
        holder.tickTransactions()
        verify(addedFirst).onTick(owner)
        verify(addedSecond, never()).onTick(any())
    }
}