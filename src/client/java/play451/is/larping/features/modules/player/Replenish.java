package play451.is.larping.features.modules.player;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.SlotActionType;
import play451.is.larping.features.modules.Module;
import play451.is.larping.features.modules.ModuleCategory;

public class Replenish extends Module {

    private final MinecraftClient mc = MinecraftClient.getInstance();
    private int tickDelay = 0;

    public Replenish() {
        super("Replenish", "Refills hotbar stacks from inventory", ModuleCategory.PLAYER);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.interactionManager == null || mc.currentScreen != null) return;

        if (tickDelay > 0) {
            tickDelay--;
            return;
        }

        PlayerInventory inv = mc.player.getInventory();

        for (int i = 0; i < 9; i++) {
            ItemStack hotbarStack = inv.getStack(i);

            if (!hotbarStack.isEmpty() && hotbarStack.isStackable() && hotbarStack.getCount() < hotbarStack.getMaxCount()) {
                int refillSlot = findRefillSlot(hotbarStack);

                if (refillSlot != -1) {
                    fillStack(refillSlot, i);
                    tickDelay = 2; 
                    return;
                }
            }
        }
    }

    private int findRefillSlot(ItemStack targetStack) {
        PlayerInventory inv = mc.player.getInventory();

        for (int i = 9; i < 36; i++) {
            ItemStack invStack = inv.getStack(i);

            if (!invStack.isEmpty() && invStack.getItem() == targetStack.getItem() && ItemStack.areItemsEqual(invStack, targetStack)) {
                return i;
            }
        }
        return -1;
    }

    private void fillStack(int invSlot, int hotbarSlot) {
        int syncSlot = invSlot < 9 ? invSlot + 36 : invSlot;
        int syncHotbar = hotbarSlot;

        mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, syncSlot, 0, SlotActionType.QUICK_MOVE, mc.player);
    }
}