package btpos.mcmods.devutil.common.structure.blocks

import btpos.mcmods.devutil.common.ext.vanilla.world.blockEntity
import net.minecraft.core.BlockPos
import net.minecraft.world.level.BlockGetter
import net.minecraft.world.level.block.EntityBlock
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.world.level.block.state.BlockState

interface BlockWithEntity<ENT_TYPE : BlockEntity> : EntityBlock {
	fun getEntityType(): BlockEntityType<ENT_TYPE>
	
	override fun newBlockEntity(pos: BlockPos, state: BlockState): BlockEntity? {
		return getEntityType().create(pos, state)
	}
	
	fun BlockGetter.getOurEntity(pos: BlockPos): ENT_TYPE? {
		val ourEntity = this.blockEntity(pos, getEntityType())
		
		return ourEntity
	}
}