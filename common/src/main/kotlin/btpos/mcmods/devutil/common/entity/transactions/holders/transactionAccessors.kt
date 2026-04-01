package btpos.mcmods.devutil.common.entity.transactions.holders

import net.minecraft.world.entity.player.Player

/**
 *
 * @see btpos.mcmods.devutil.mixin.transactions.AddPlayerTransactions
 */
@Suppress("UNCHECKED_CAST")
val Player.transactions: ITransactionHolder<Player>
    get() = (this as ITransactionHolder_Injected<Player>).`terminus$getTransactions`()