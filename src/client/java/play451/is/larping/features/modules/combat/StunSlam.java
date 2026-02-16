package play451.is.larping.features.modules.combat;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.registry.tag.ItemTags;
import play451.is.larping.config.Config;
import play451.is.larping.features.modules.Module;
import play451.is.larping.features.modules.ModuleCategory;
import play451.is.larping.features.modules.ModuleSettingsRenderer;
import play451.is.larping.features.modules.SettingsHelper;

import java.util.HashMap;
import java.util.Map;

public class StunSlam extends Module implements ModuleSettingsRenderer {
    
     
    private boolean requireDensity = true;  
    private boolean onlyOnShield = true;  
    private boolean autoSwitchBack = true;  
    private int swapDelay = 2;  
    private int swapBackDelay = 2;  
    
     
    private int swapDelayCounter = 0;
    private int swapBackCounter = 0;
    private boolean hasSwapped = false;
    private int previousSlot = -1;
    private PlayerEntity lastTarget = null;
    private boolean awaitingBack = false;
    
    private final MinecraftClient mc = MinecraftClient.getInstance();
    
    public StunSlam() {
        super("StunSlam", "Switches from axe to mace when hitting shielding opponents", ModuleCategory.COMBAT);
    }
    
    @Override
    public void onEnable() {
        swapDelayCounter = 0;
        swapBackCounter = 0;
        hasSwapped = false;
        previousSlot = -1;
        lastTarget = null;
        awaitingBack = false;
    }
    
    @Override
    public void onDisable() {
         
        if (hasSwapped && previousSlot != -1 && mc.player != null) {
            mc.player.getInventory().setSelectedSlot(previousSlot);
            mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(previousSlot));
        }
        swapDelayCounter = 0;
        swapBackCounter = 0;
        hasSwapped = false;
        previousSlot = -1;
        lastTarget = null;
        awaitingBack = false;
    }
    
    @Override
    public void onTick() {
        if (mc.player == null || mc.world == null) return;
        
         
        if (awaitingBack) {
            swapBackCounter--;
            if (swapBackCounter <= 0) {
                swapBackToAxe();
                awaitingBack = false;
            }
            return;
        }
        
         
        PlayerEntity target = null;
        if (mc.crosshairTarget != null && mc.crosshairTarget.getType() == net.minecraft.util.hit.HitResult.Type.ENTITY) {
            var entityHit = (net.minecraft.util.hit.EntityHitResult) mc.crosshairTarget;
            if (entityHit.getEntity() instanceof PlayerEntity) {
                target = (PlayerEntity) entityHit.getEntity();
            }
        }
        
         
        if (target != null && !hasSwapped) {
             
            if (onlyOnShield && !isPlayerBlocking(target)) {
                swapDelayCounter = 0;
                return;
            }
            
             
            ItemStack heldItem = mc.player.getMainHandStack();
            if (!isAxe(heldItem)) {
                swapDelayCounter = 0;
                return;
            }
            
             
            if (mc.options.attackKey.isPressed()) {
                swapDelayCounter++;
                
                if (swapDelayCounter >= swapDelay) {
                    swapToMace(target);
                    swapDelayCounter = 0;
                }
            }
        } else {
            swapDelayCounter = 0;
        }
    }
    
    private void swapToMace(PlayerEntity target) {
         
        Integer maceSlot = findMaceInHotbar();
        if (maceSlot == null) return;
        
         
        previousSlot = mc.player.getInventory().getSelectedSlot();
        lastTarget = target;
        
         
        mc.player.getInventory().setSelectedSlot(maceSlot);
        mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(maceSlot));
        
        hasSwapped = true;
        
         
        if (autoSwitchBack) {
            awaitingBack = true;
            swapBackCounter = swapBackDelay;
        }
    }
    
    private void swapBackToAxe() {
        if (previousSlot != -1 && mc.player != null) {
             
            ItemStack previousStack = mc.player.getInventory().getStack(previousSlot);
            if (isAxe(previousStack)) {
                mc.player.getInventory().setSelectedSlot(previousSlot);
                mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(previousSlot));
            } else {
                 
                Integer axeSlot = findAxeInHotbar();
                if (axeSlot != null) {
                    mc.player.getInventory().setSelectedSlot(axeSlot);
                    mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(axeSlot));
                }
            }
        }
        
        hasSwapped = false;
        previousSlot = -1;
        lastTarget = null;
        swapBackCounter = 0;
    }
    
    private boolean isPlayerBlocking(PlayerEntity player) {
        return player.isBlocking() && player.getActiveItem().isOf(Items.SHIELD);
    }
    
    private boolean isAxe(ItemStack item) {
        if (item.isEmpty()) return false;
        return item.isIn(ItemTags.AXES);
    }
    
    private Integer findMaceInHotbar() {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (!stack.isOf(Items.MACE)) continue;
            
             
            if (requireDensity) {
                if (hasDensityEnchantment(stack)) {
                    return i;
                }
            } else {
                return i;
            }
        }
        return null;
    }
    
    private Integer findAxeInHotbar() {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (isAxe(stack)) {
                return i;
            }
        }
        return null;
    }
    
    private boolean hasDensityEnchantment(ItemStack stack) {
         
         
        try {
             
            String nbtString = stack.toString().toLowerCase();
            return nbtString.contains("density");
        } catch (Exception e) {
             
            return false;
        }
    }
    
     
    @Override
    public Map<String, Object> saveSettings() {
        Map<String, Object> settings = new HashMap<>();
        settings.put("requireDensity", requireDensity);
        settings.put("onlyOnShield", onlyOnShield);
        settings.put("autoSwitchBack", autoSwitchBack);
        settings.put("swapDelay", swapDelay);
        settings.put("swapBackDelay", swapBackDelay);
        return settings;
    }
    
    @Override
    public void loadSettings(Map<String, Object> settings) {
        if (settings.containsKey("requireDensity")) {
            requireDensity = (Boolean) settings.get("requireDensity");
        }
        if (settings.containsKey("onlyOnShield")) {
            onlyOnShield = (Boolean) settings.get("onlyOnShield");
        }
        if (settings.containsKey("autoSwitchBack")) {
            autoSwitchBack = (Boolean) settings.get("autoSwitchBack");
        }
        if (settings.containsKey("swapDelay")) {
            swapDelay = ((Number) settings.get("swapDelay")).intValue();
        }
        if (settings.containsKey("swapBackDelay")) {
            swapBackDelay = ((Number) settings.get("swapBackDelay")).intValue();
        }
    }
    
     
    public void setRequireDensity(boolean requireDensity) {
        this.requireDensity = requireDensity;
        Config.getInstance().saveModules();
    }
    
    public boolean isRequireDensity() {
        return requireDensity;
    }
    
    public void setOnlyOnShield(boolean onlyOnShield) {
        this.onlyOnShield = onlyOnShield;
        Config.getInstance().saveModules();
    }
    
    public boolean isOnlyOnShield() {
        return onlyOnShield;
    }
    
    public void setAutoSwitchBack(boolean autoSwitchBack) {
        this.autoSwitchBack = autoSwitchBack;
        Config.getInstance().saveModules();
    }
    
    public boolean isAutoSwitchBack() {
        return autoSwitchBack;
    }
    
    public void setSwapDelay(int swapDelay) {
        this.swapDelay = Math.max(0, Math.min(20, swapDelay));
        Config.getInstance().saveModules();
    }
    
    public int getSwapDelay() {
        return swapDelay;
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
        
         
        helper.renderLabel(context, "Require Density:", null, startX, settingY);
        helper.renderToggle(context, mouseX, mouseY, startX + 150, settingY - 5, requireDensity);
        settingY += 35;
        
         
        helper.renderLabel(context, "Only On Shield:", null, startX, settingY);
        helper.renderToggle(context, mouseX, mouseY, startX + 150, settingY - 5, onlyOnShield);
        settingY += 35;
        
         
        helper.renderLabel(context, "Auto Switch Back:", null, startX, settingY);
        helper.renderToggle(context, mouseX, mouseY, startX + 150, settingY - 5, autoSwitchBack);
        settingY += 35;
        
         
        helper.renderLabel(context, "Swap Delay:", String.format("%d ticks", swapDelay), startX, settingY);
        helper.renderSlider(context, startX + 200, settingY, 150, swapDelay, 0.0, 20.0);
        settingY += 35;
        
         
        if (autoSwitchBack) {
            helper.renderLabel(context, "Swap Back Delay:", String.format("%d ticks", swapBackDelay), startX + 20, settingY);
            helper.renderSlider(context, startX + 200, settingY, 150, swapBackDelay, 0.0, 40.0);
            settingY += 35;
        }
        
         
        helper.renderInfo(context, "Swaps axe → mace when hitting shielding", startX, settingY);
        helper.renderInfo(context, "players for devastating stun combos", startX, settingY + 10);
        
        return settingY + 20;
    }
    
    @Override
    public boolean handleSettingsClick(double mouseX, double mouseY, int startX, int startY, int width, SettingsHelper helper) {
        int settingY = startY;
        
         
        if (helper.isToggleHovered(mouseX, mouseY, startX + 150, settingY - 5)) {
            setRequireDensity(!requireDensity);
            return true;
        }
        settingY += 35;
        
         
        if (helper.isToggleHovered(mouseX, mouseY, startX + 150, settingY - 5)) {
            setOnlyOnShield(!onlyOnShield);
            return true;
        }
        settingY += 35;
        
         
        if (helper.isToggleHovered(mouseX, mouseY, startX + 150, settingY - 5)) {
            setAutoSwitchBack(!autoSwitchBack);
            return true;
        }
        settingY += 35;
        
         
        if (helper.isSliderHovered(mouseX, mouseY, startX + 200, settingY, 150)) {
            int newValue = (int) helper.calculateSliderValue(mouseX, startX + 200, 150, 0.0, 20.0);
            setSwapDelay(newValue);
            return true;
        }
        settingY += 35;
        
         
        if (autoSwitchBack) {
            if (helper.isSliderHovered(mouseX, mouseY, startX + 200, settingY, 150)) {
                int newValue = (int) helper.calculateSliderValue(mouseX, startX + 200, 150, 0.0, 40.0);
                setSwapBackDelay(newValue);
                return true;
            }
        }
        
        return false;
    }
}