package btpos.mcmods.devutil.mixin.api;

import btpos.mcmods.devutil.multiplatform.api.IPlatformConnectRedstone;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.RedStoneWireBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Applied on Fabric only
 */
@SuppressWarnings("UnusedMixin") // Applied through IMixinConfigPlugin#getMixins
@Mixin(RedStoneWireBlock.class)
public abstract class MConnectRedstone {
	@WrapOperation(
			method="getConnectingSide(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/Direction;Z)Lnet/minecraft/world/level/block/state/properties/RedstoneSide;",
			at=@At(
					target="Lnet/minecraft/world/level/block/RedStoneWireBlock;shouldConnectTo(Lnet/minecraft/world/level/block/state/BlockState;)Z",
					value="INVOKE",
					ordinal=0
			)
	)
	private boolean noDirection_above(BlockState state, Operation<Boolean> original, @Local(argsOnly = true) BlockPos pos, @Local(argsOnly = true) BlockGetter level) {
		if (state.getBlock() instanceof IPlatformConnectRedstone platform) {
			return platform.canConnectRedstone(state, level, pos.above(), Direction.UP);
		}
		return original.call(state);
	}
	
	@WrapOperation(
			method="getConnectingSide(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/Direction;Z)Lnet/minecraft/world/level/block/state/properties/RedstoneSide;",
			at=@At(
					target="Lnet/minecraft/world/level/block/RedStoneWireBlock;shouldConnectTo(Lnet/minecraft/world/level/block/state/BlockState;)Z",
					value="INVOKE",
					ordinal=1
			)
	)
	private boolean noDirection_below(BlockState state, Operation<Boolean> original, @Local(argsOnly = true) BlockPos pos, @Local(argsOnly = true) BlockGetter level) {
		if (state.getBlock() instanceof IPlatformConnectRedstone platform) {
			return platform.canConnectRedstone(state, level, pos.below(), Direction.DOWN);
		}
		return original.call(state);
	}
	
	@WrapOperation(
			method="getConnectingSide(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/Direction;Z)Lnet/minecraft/world/level/block/state/properties/RedstoneSide;",
			at=@At(
					target="Lnet/minecraft/world/level/block/RedStoneWireBlock;shouldConnectTo(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/Direction;)Z",
					value="INVOKE"
			)
	)
	private boolean withDirection(BlockState state, Direction direction, Operation<Boolean> original, @Local(argsOnly = true) BlockPos pos, @Local(argsOnly = true) BlockGetter level) {
		if (state.getBlock() instanceof IPlatformConnectRedstone platform) {
			return platform.canConnectRedstone(state, level, pos.relative(direction), direction);
		}
		
		return original.call(state, direction);
	}
}
