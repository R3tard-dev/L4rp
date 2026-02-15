package play451.is.larping.features.modules.combat;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.SlotActionType;
import play451.is.larping.config.Config;
import play451.is.larping.features.modules.Module;
import play451.is.larping.features.modules.ModuleCategory;

import java.util.HashMap;
import java.util.Map;

public class AutoTotem extends Module {
    
     
    private int delay = 3;  
    private boolean moveFromHotbar = true;  
    private boolean openInv = false;  
    private int invOpenDelay = 2;  
    private int invCloseDelay = 8;  
    
     
    private boolean needsTotem = false;
    private int delayTicks = 0;
    private boolean hadTotemInOffhand = false;
    
     
    private boolean shouldOpenInv = false;
    private int invOpenTicks = 0;
    private int invCloseTicks = 0;
    private boolean invAutoOpened = false;
    
    private final MinecraftClient mc = MinecraftClient.getInstance();
    
    public AutoTotem() {
        super("AutoTotem", "auto totem", ModuleCategory.COMBAT);
    }
    
    @Override
    public void onEnable() {
        if (mc.player != null) {
            hadTotemInOffhand = hasTotemInOffhand();
            needsTotem = false;
            delayTicks = 0;
            resetInvState();
        }
    }
    
    @Override
    public void onDisable() {
        resetInvState();
    }
    
    private void resetInvState() {
        shouldOpenInv = false;
        invOpenTicks = 0;
        invCloseTicks = 0;
        invAutoOpened = false;
    }
    
    @Override
    public void onTick() {
        if (mc.player == null) return;
        
        handleAutoInventory();
        
         
        boolean hasTotem = hasTotemInOffhand();
        if (hadTotemInOffhand && !hasTotem) {
            needsTotem = true;
            if (openInv && mc.currentScreen == null) {
                shouldOpenInv = true;
                invOpenTicks = invOpenDelay;
            }
        }
        hadTotemInOffhand = hasTotem;
        
         
        if (hasTotem && needsTotem) {
            needsTotem = false;
            delayTicks = 0;
        }
        
         
        if (delayTicks > 0) {
            delayTicks--;
            if (delayTicks == 0) {
                moveTotemToOffhand();
            }
        }
        
         
        if (mc.currentScreen instanceof InventoryScreen && needsTotem) {
            delayTicks = delay;
        }
    }
    
    private void handleAutoInventory() {
         
        if (shouldOpenInv && invOpenTicks > 0) {
            invOpenTicks--;
            if (invOpenTicks == 0 && mc.currentScreen == null) {
                mc.setScreen(new InventoryScreen(mc.player));
                invAutoOpened = true;
                invCloseTicks = invCloseDelay;
                shouldOpenInv = false;
            }
        }
        
         
        if (invAutoOpened && invCloseTicks > 0) {
            invCloseTicks--;
            if (invCloseTicks == 0 && mc.currentScreen instanceof InventoryScreen) {
                mc.setScreen(null);
                invAutoOpened = false;
            }
        }
        
         
        if (invAutoOpened && !(mc.currentScreen instanceof InventoryScreen)) {
            invAutoOpened = false;
            invCloseTicks = 0;
        }
    }
    
    private void moveTotemToOffhand() {
        int totemSlot = findTotemSlot();
        if (totemSlot == -1) return;
        
        try {
             
            int containerSlot = totemSlot < 9 ? totemSlot + 36 : totemSlot;
            
            ItemStack offhandStack = mc.player.getOffHandStack();
            
            if (offhandStack.isEmpty()) {
                 
                mc.interactionManager.clickSlot(0, containerSlot, 40, SlotActionType.SWAP, mc.player);
            } else {
                 
                mc.interactionManager.clickSlot(0, containerSlot, 0, SlotActionType.PICKUP, mc.player);
                mc.interactionManager.clickSlot(0, 45, 0, SlotActionType.PICKUP, mc.player);
                mc.interactionManager.clickSlot(0, containerSlot, 0, SlotActionType.PICKUP, mc.player);
            }
            
            needsTotem = false;
            
        } catch (Exception ignored) {
             
        }
    }
    
    private int findTotemSlot() {
         
        for (int i = 9; i < 36; i++) {
            if (mc.player.getInventory().getStack(i).getItem() == Items.TOTEM_OF_UNDYING) {
                return i;
            }
        }
        
         
        if (moveFromHotbar) {
            for (int i = 0; i < 9; i++) {
                if (mc.player.getInventory().getStack(i).getItem() == Items.TOTEM_OF_UNDYING) {
                    return i;
                }
            }
        }
        
        return -1;
    }
    
    private boolean hasTotemInOffhand() {
        if (mc.player == null) return false;
        ItemStack stack = mc.player.getOffHandStack();
        return !stack.isEmpty() && stack.getItem() == Items.TOTEM_OF_UNDYING;
    }
    
     
    @Override
    public Map<String, Object> saveSettings() {
        Map<String, Object> settings = new HashMap<>();
        settings.put("delay", delay);
        settings.put("moveFromHotbar", moveFromHotbar);
        settings.put("openInv", openInv);
        settings.put("invOpenDelay", invOpenDelay);
        settings.put("invCloseDelay", invCloseDelay);
        return settings;
    }
    
    @Override
    public void loadSettings(Map<String, Object> settings) {
        if (settings.containsKey("delay")) {
            delay = ((Number) settings.get("delay")).intValue();
        }
        if (settings.containsKey("moveFromHotbar")) {
            moveFromHotbar = (Boolean) settings.get("moveFromHotbar");
        }
        if (settings.containsKey("openInv")) {
            openInv = (Boolean) settings.get("openInv");
        }
        if (settings.containsKey("invOpenDelay")) {
            invOpenDelay = ((Number) settings.get("invOpenDelay")).intValue();
        }
        if (settings.containsKey("invCloseDelay")) {
            invCloseDelay = ((Number) settings.get("invCloseDelay")).intValue();
        }
    }
    
     
    public void setDelay(int delay) {
        this.delay = Math.max(1, Math.min(20, delay));
        Config.getInstance().saveModules();
    }
    
    public int getDelay() {
        return delay;
    }
    
    public void setMoveFromHotbar(boolean moveFromHotbar) {
        this.moveFromHotbar = moveFromHotbar;
        Config.getInstance().saveModules();
    }
    
    public boolean isMoveFromHotbar() {
        return moveFromHotbar;
    }
    
    public void setOpenInv(boolean openInv) {
        this.openInv = openInv;
        Config.getInstance().saveModules();
    }
    
    public boolean isOpenInv() {
        return openInv;
    }
    
    public void setInvOpenDelay(int invOpenDelay) {
        this.invOpenDelay = Math.max(1, Math.min(10, invOpenDelay));
        Config.getInstance().saveModules();
    }
    
    public int getInvOpenDelay() {
        return invOpenDelay;
    }
    
    public void setInvCloseDelay(int invCloseDelay) {
        this.invCloseDelay = Math.max(5, Math.min(20, invCloseDelay));
        Config.getInstance().saveModules();
    }
    
    public int getInvCloseDelay() {
        return invCloseDelay;
    }
}