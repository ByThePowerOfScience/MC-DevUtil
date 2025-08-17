package btpos.mcmods.testproject.blocks

import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.world.level.BlockGetter
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockState

class PlatformRedstoneBlock(props: Properties) : Block(props), btpos.mcmods.devutil.multiplatform.api.IPlatformConnectRedstone {
	override fun canConnectRedstone(state: BlockState, level: BlockGetter, pos: BlockPos, direction: Direction?): Boolean {
		return true
	}
}