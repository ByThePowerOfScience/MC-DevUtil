package btpos.mcmods.devutil.common.impl.events

import btpos.mcmods.devutil.common.entity.transactions.ETransactionCancelReason
import btpos.mcmods.devutil.common.entity.transactions.holders.transactions
import net.minecraft.server.MinecraftServer
import net.minecraft.world.entity.player.Player

/**
 * Event listeners that implement the various behaviors for [ITransaction][btpos.mcmods.devutil.common.entity.transactions.ITransaction]
 */
internal abstract class XplatTransactionHandlers {
    protected fun onPlayerTick(player: Player) {
        player.transactions.tickTransactions()
    }
    
    // Cancelling transactions:
    
    private fun Player.cancelAllTransactions(reason: ETransactionCancelReason) {
        transactions.onACancellingChange(reason)
    }
    
    /**
     * Called when a player leaves the server
     */
    protected fun onPlayerLogOut(player: Player) {
        player.cancelAllTransactions(ETransactionCancelReason.PLAYER_LOGGED_OUT)
    }
    
    /**
     * Called when the server stops. (how do we handle crashes?)
     */
    protected fun onServerStop(server: MinecraftServer) {
        server.playerList.players.forEach { p ->
            p.cancelAllTransactions(ETransactionCancelReason.SERVER_STOP)
        }
    }
    
    protected fun onPlayerDeath(player: Player) {
        player.cancelAllTransactions(ETransactionCancelReason.OWNER_KILLED)
    }
    
    protected fun onPlayerChangeDimension(player: Player) {
        player.cancelAllTransactions(ETransactionCancelReason.OWNER_CHANGED_DIMENSION)
    }
    
    protected fun onPlayerTeleported(player: Player) {
        player.cancelAllTransactions(ETransactionCancelReason.OWNER_TELEPORTED)
    }
}