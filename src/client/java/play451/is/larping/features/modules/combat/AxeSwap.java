package play451.is.larping.features.modules.combat;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.AxeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.registry.tag.ItemTags;
import play451.is.larping.config.Config;
import play451.is.larping.features.modules.Module;
import play451.is.larping.features.modules.ModuleCategory;
import play451.is.larping.features.modules.ModuleSettingsRenderer;
import play451.is.larping.features.modules.SettingsHelper;
import play451.is.larping.mixin.PlayerInventoryAccessor;

import java.util.HashMap;
import java.util.Map;

public class AxeSwap extends Module implements ModuleSettingsRenderer {

    private boolean autoSwapBack = true;
    private int swapBackDelay = 5;
    private boolean onlyWhenHoldingSword = true;

    private int swapBackCounter = 0;
    private boolean hasSwapped = false;
    private int previousSlot = -1;
    private PlayerEntity lastTarget = null;

    private final MinecraftClient mc = MinecraftClient.getInstance();

    public AxeSwap() {
        super("AxeSwap", "Switches to axe when hitting shielding opponents", ModuleCategory.COMBAT);
    }

    @Override
    public void onEnable() {
        swapBackCounter = 0;
        hasSwapped = false;
        previousSlot = -1;
        lastTarget = null;
    }

    @Override
    public void onDisable() {
        if (hasSwapped && previousSlot != -1 && mc.player != null) {
            ((PlayerInventoryAccessor) mc.player.getInventory()).setSelectedSlot(previousSlot);
            mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(previousSlot));
        }
        swapBackCounter = 0;
        hasSwapped = false;
        previousSlot = -1;
        lastTarget = null;
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.world == null) return;
        if (!hasSwapped || !autoSwapBack) return;

        PlayerEntity target = null;
        if (mc.crosshairTarget != null && mc.crosshairTarget.getType() == net.minecraft.util.hit.HitResult.Type.ENTITY) {
            var entityHit = (net.minecraft.util.hit.EntityHitResult) mc.crosshairTarget;
            if (entityHit.getEntity() instanceof PlayerEntity p) {
                target = p;
            }
        }

        boolean shouldSwapBack = (target == null || target != lastTarget || !target.isBlocking());

        if (shouldSwapBack) {
            swapBackCounter++;
            if (swapBackCounter >= swapBackDelay) {
                swapBackToSword();
            }
        } else {
            swapBackCounter = 0;
        }
    }

    public void doSwapToAxe(PlayerEntity target) {
        if (mc.player == null) return;
        if (hasSwapped) return;

        if (onlyWhenHoldingSword) {
            if (!mc.player.getMainHandStack().isIn(ItemTags.SWORDS)) return;
        }

        Integer axeSlot = findAxeInHotbar();
        if (axeSlot == null) return;

        previousSlot = ((PlayerInventoryAccessor) mc.player.getInventory()).getSelectedSlot();
        lastTarget = target;

        ((PlayerInventoryAccessor) mc.player.getInventory()).setSelectedSlot(axeSlot);
        mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(axeSlot));

        hasSwapped = true;
        swapBackCounter = 0;
    }

    private void swapBackToSword() {
        if (previousSlot != -1 && mc.player != null) {
            ItemStack previousStack = mc.player.getInventory().getStack(previousSlot);
            if (previousStack.isIn(ItemTags.SWORDS)) {
                ((PlayerInventoryAccessor) mc.player.getInventory()).setSelectedSlot(previousSlot);
                mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(previousSlot));
            } else {
                Integer swordSlot = findSwordInHotbar();
                if (swordSlot != null) {
                    ((PlayerInventoryAccessor) mc.player.getInventory()).setSelectedSlot(swordSlot);
                    mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(swordSlot));
                }
            }
        }

        hasSwapped = false;
        previousSlot = -1;
        lastTarget = null;
        swapBackCounter = 0;
    }

    private Integer findAxeInHotbar() {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.getItem() instanceof AxeItem) return i;
        }
        return null;
    }

    private Integer findSwordInHotbar() {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.isIn(ItemTags.SWORDS)) return i;
        }
        return null;
    }

    @Override
    public Map<String, Object> saveSettings() {
        Map<String, Object> settings = new HashMap<>();
        settings.put("autoSwapBack", autoSwapBack);
        settings.put("onlyWhenHoldingSword", onlyWhenHoldingSword);
        settings.put("swapBackDelay", swapBackDelay);
        return settings;
    }

    @Override
    public void loadSettings(Map<String, Object> settings) {
        if (settings.containsKey("autoSwapBack")) autoSwapBack = (Boolean) settings.get("autoSwapBack");
        if (settings.containsKey("onlyWhenHoldingSword")) onlyWhenHoldingSword = (Boolean) settings.get("onlyWhenHoldingSword");
        if (settings.containsKey("swapBackDelay")) swapBackDelay = ((Number) settings.get("swapBackDelay")).intValue();
    }

    public void setAutoSwapBack(boolean autoSwapBack) {
        this.autoSwapBack = autoSwapBack;
        Config.getInstance().saveModules();
    }

    public boolean isAutoSwapBack() {
        return autoSwapBack;
    }

    public void setOnlyWhenHoldingSword(boolean onlyWhenHoldingSword) {
        this.onlyWhenHoldingSword = onlyWhenHoldingSword;
        Config.getInstance().saveModules();
    }

    public boolean isOnlyWhenHoldingSword() {
        return onlyWhenHoldingSword;
    }

    public void setSwapBackDelay(int swapBackDelay) {
        this.swapBackDelay = Math.max(0, Math.min(40, swapBackDelay));
        Config.getInstance().saveModules();
    }

    public int getSwapBackDelay() {
        return swapBackDelay;
    }

    @Override
    public int renderSettings(DrawContext context, int mouseX, int mouseY, int startX, int startY, int width, SettingsHelper helper) {
        int settingY = startY;

        helper.renderLabel(context, "Auto Swap Back:", null, startX, settingY);
        helper.renderToggle(context, mouseX, mouseY, startX + 150, settingY - 5, autoSwapBack);
        settingY += 35;

        helper.renderLabel(context, "Only w/ Sword:", null, startX, settingY);
        helper.renderToggle(context, mouseX, mouseY, startX + 150, settingY - 5, onlyWhenHoldingSword);
        settingY += 35;

        if (autoSwapBack) {
            helper.renderLabel(context, "Swap Back Delay:", String.format("%d ticks", swapBackDelay), startX + 20, settingY);
            helper.renderSlider(context, startX + 200, settingY, 150, swapBackDelay, 0.0, 40.0);
            settingY += 35;
        }

        helper.renderInfo(context, "Swaps to axe when hitting blocking players", startX, settingY);
        helper.renderInfo(context, "to disable their shield", startX, settingY + 10);

        return settingY + 20;
    }

    @Override
    public boolean handleSettingsClick(double mouseX, double mouseY, int startX, int startY, int width, SettingsHelper helper) {
        int settingY = startY;

        if (helper.isToggleHovered(mouseX, mouseY, startX + 150, settingY - 5)) {
            setAutoSwapBack(!autoSwapBack);
            return true;
        }
        settingY += 35;

        if (helper.isToggleHovered(mouseX, mouseY, startX + 150, settingY - 5)) {
            setOnlyWhenHoldingSword(!onlyWhenHoldingSword);
            return true;
        }
        settingY += 35;

        if (autoSwapBack) {
            if (helper.isSliderHovered(mouseX, mouseY, startX + 200, settingY, 150)) {
                int newValue = (int) helper.calculateSliderValue(mouseX, startX + 200, 150, 0.0, 40.0);
                setSwapBackDelay(newValue);
                return true;
            }
        }

        return false;
    }
}