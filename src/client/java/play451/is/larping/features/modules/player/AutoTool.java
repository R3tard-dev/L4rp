package play451.is.larping.features.modules.player;

import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import play451.is.larping.features.modules.Module;
import play451.is.larping.features.modules.ModuleCategory;
import play451.is.larping.ducks.IPlayerInventory;

public class AutoTool extends Module {

    private final MinecraftClient mc = MinecraftClient.getInstance();
    private int lastSlot = -1;
    private boolean wasBreaking = false;

    public AutoTool() {
        super("AutoTool", "Automatically switches to the best tool", ModuleCategory.PLAYER);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;

        IPlayerInventory inv = (IPlayerInventory) mc.player.getInventory();

        if (mc.options.attackKey.isPressed() && mc.crosshairTarget != null && mc.crosshairTarget.getType() == HitResult.Type.BLOCK) {
            BlockHitResult blockHit = (BlockHitResult) mc.crosshairTarget;
            BlockState state = mc.world.getBlockState(blockHit.getBlockPos());

            if (!state.isAir()) {
                int bestSlot = findBestSlot(state);
                
                if (bestSlot != -1 && bestSlot != inv.larping$getSelectedSlot()) {
                    if (!wasBreaking) {
                        lastSlot = inv.larping$getSelectedSlot();
                        wasBreaking = true;
                    }
                    inv.larping$setSelectedSlot(bestSlot);
                }
            }
        } else if (wasBreaking) {
            wasBreaking = false;
            lastSlot = -1;
        }
    }

    private int findBestSlot(BlockState state) {
        int bestSlot = -1;
        double maxSpeed = 1.0;

        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.isEmpty()) continue;

            float speed = stack.getMiningSpeedMultiplier(state);

            if (speed > maxSpeed) {
                maxSpeed = speed;
                bestSlot = i;
            }
        }

        return bestSlot;
    }
    
    @Override
    public void onDisable() {
        wasBreaking = false;
        lastSlot = -1;
    }
}