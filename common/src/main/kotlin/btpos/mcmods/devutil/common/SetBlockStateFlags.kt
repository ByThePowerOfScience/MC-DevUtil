package btpos.mcmods.devutil.common

/**
 * Flags for [net.minecraft.world.level.Level.setBlock] as of 1.20.1
 */
object SetBlockStateFlags {
    /**
     * Cause a block update on the block that's being changed.
     */
    const val BLOCK_UPDATE = 1
    
    /**
     * Sync the change to clients.
     */
    const val SEND_TO_CLIENTS = 2
    
    /**
     * Prevent the block from bring re-rendered.
     */
    const val NO_RERENDER = 4
    
    /**
     * Forces any rerenders to happen on the main thread.
     */
    const val RERENDER_ON_MAIN = 8
    
    /**
     * The change will not cause block updates on neighbors.
     */
    const val NO_NEIGHBOR_REACTIONS = 16
    
    /**
     * Prevent any neighbor reactions from spawning drops.
     */
    const val PREVENT_NEIGHBOR_REACTIONS_DROPS = 32
    
    /**
     * Signify that this block is being moved.
     */
    const val IS_BEING_MOVED = 64
}