package btpos.mcmods.devutil.neoforge.impl.events

import btpos.mcmods.devutil.common.impl.events.XplatTransactionHandlers
import net.minecraft.world.entity.player.Player
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.neoforge.event.entity.EntityTeleportEvent
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent
import net.neoforged.neoforge.event.entity.player.PlayerEvent
import net.neoforged.neoforge.event.server.ServerStoppingEvent
import net.neoforged.neoforge.event.tick.EntityTickEvent

internal object NeoForgeTransactionHandlers : XplatTransactionHandlers() {
    @SubscribeEvent
    fun onTick(evt: EntityTickEvent.Post) {
        (evt.entity as? Player)?.let { super.onPlayerTick(it) }
    }
    
    @SubscribeEvent
    fun onLogOut(evt: PlayerEvent.PlayerLoggedOutEvent) {
        super.onPlayerLogOut(evt.entity)
    }
    
    @SubscribeEvent
    fun onChangeDimension(evt: PlayerEvent.PlayerChangedDimensionEvent) {
        super.onPlayerChangeDimension(evt.entity)
    }
    
    @SubscribeEvent
    fun onServerStop(evt: ServerStoppingEvent) {
        super.onServerStop(evt.server)
    }
    
    @SubscribeEvent
    fun onPlayerDie(evt: LivingDeathEvent) {
        (evt.entity as? Player)?.let { onPlayerDeath(it) }
    }
    
    @SubscribeEvent
    fun onPlayerTeleport(evt: EntityTeleportEvent) {
        (evt.entity as? Player)?.let { onPlayerTeleport(evt) }
    }
}