package play451.is.larping.mixin;

import net.minecraft.entity.player.PlayerInventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import play451.is.larping.ducks.IPlayerInventory;

@Mixin(PlayerInventory.class)
public class PlayerInventoryMixin implements IPlayerInventory {
    @Shadow
    public int selectedSlot;

    @Override
    public void larping$setSelectedSlot(int slot) {
        this.selectedSlot = slot;
    }

    @Override
    public int larping$getSelectedSlot() {
        return this.selectedSlot;
    }
}