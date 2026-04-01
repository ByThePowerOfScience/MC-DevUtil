package btpos.mcmods.devutil.mixin.transactions;

import btpos.mcmods.devutil.common.entity.transactions.holders.BaseTransactionHolder;
import btpos.mcmods.devutil.common.entity.transactions.holders.ITransactionHolder;
import btpos.mcmods.devutil.common.entity.transactions.holders.ITransactionHolder_Injected;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(Player.class)
abstract class AddPlayerTransactions implements ITransactionHolder_Injected<Player> {
	@Unique
	private final ITransactionHolder<Player> terminus$transactions = new BaseTransactionHolder<>(((Player)(Object)this));
	
	@Override
	public @NotNull ITransactionHolder<Player> terminus$getTransactions() {
		return terminus$transactions;
	}
}
