package play451.is.larping.features.modules.combat;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.util.Hand;
import play451.is.larping.config.Config;
import play451.is.larping.features.modules.Module;
import play451.is.larping.features.modules.ModuleCategory;
import play451.is.larping.features.modules.ModuleSettingsRenderer;
import play451.is.larping.features.modules.SettingsHelper;

import java.util.HashMap;
import java.util.Map;

public class KeyPearl extends Module implements ModuleSettingsRenderer {
    
     
    private boolean switchBack = true;  
    private int throwDelay = 0;  
    
     
    private boolean thrown = false;
    private int delayCounter = 0;
    private int previousSlot = -1;
    
    private final MinecraftClient mc = MinecraftClient.getInstance();
    
    public KeyPearl() {
        super("KeyPearl", "Throws an ender pearl and disables", ModuleCategory.COMBAT);
    }
    
    @Override
    public void onEnable() {
        thrown = false;
        delayCounter = 0;
        previousSlot = -1;
        
        if (mc.player == null || mc.world == null) {
            this.toggle();
            return;
        }
        
         
        Integer pearlSlot = findHotbarItem(Items.ENDER_PEARL);
        if (pearlSlot == null) {
             
            this.toggle();
            return;
        }
        
         
        if (switchBack) {
            previousSlot = mc.player.getInventory().getSelectedSlot();
        }
        
         
        if (mc.player.getInventory().getSelectedSlot() != pearlSlot) {
            mc.player.getInventory().setSelectedSlot(pearlSlot);
            mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(pearlSlot));
        }
    }
    
    @Override
    public void onDisable() {
         
        if (switchBack && previousSlot != -1 && mc.player != null) {
            mc.player.getInventory().setSelectedSlot(previousSlot);
            mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(previousSlot));
        }
        
        thrown = false;
        delayCounter = 0;
        previousSlot = -1;
    }
    
    @Override
    public void onTick() {
        if (mc.player == null || mc.world == null) {
            this.toggle();
            return;
        }
        
         
        if (thrown) {
            this.toggle();
            return;
        }
        
         
        if (delayCounter < throwDelay) {
            delayCounter++;
            return;
        }
        
         
        if (!mc.player.getMainHandStack().isOf(Items.ENDER_PEARL)) {
             
            Integer pearlSlot = findHotbarItem(Items.ENDER_PEARL);
            if (pearlSlot == null) {
                 
                this.toggle();
                return;
            }
            mc.player.getInventory().setSelectedSlot(pearlSlot);
            mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(pearlSlot));
            return;
        }
        
         
        mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
        mc.player.swingHand(Hand.MAIN_HAND);
        
        thrown = true;
        
         
    }
    
    private Integer findHotbarItem(Item item) {
        for (int i = 0; i < 9; i++) {
            if (mc.player.getInventory().getStack(i).getItem() == item) {
                return i;
            }
        }
        return null;
    }
    
     
    @Override
    public Map<String, Object> saveSettings() {
        Map<String, Object> settings = new HashMap<>();
        settings.put("switchBack", switchBack);
        settings.put("throwDelay", throwDelay);
        return settings;
    }
    
    @Override
    public void loadSettings(Map<String, Object> settings) {
        if (settings.containsKey("switchBack")) {
            switchBack = (Boolean) settings.get("switchBack");
        }
        if (settings.containsKey("throwDelay")) {
            throwDelay = ((Number) settings.get("throwDelay")).intValue();
        }
    }
    
     
    public void setSwitchBack(boolean switchBack) {
        this.switchBack = switchBack;
        Config.getInstance().saveModules();
    }
    
    public boolean isSwitchBack() {
        return switchBack;
    }
    
    public void setThrowDelay(int throwDelay) {
        this.throwDelay = Math.max(0, Math.min(20, throwDelay));
        Config.getInstance().saveModules();
    }
    
    public int getThrowDelay() {
        return throwDelay;
    }
    
     
    @Override
    public int renderSettings(DrawContext context, int mouseX, int mouseY, int startX, int startY, int width, SettingsHelper helper) {
        int settingY = startY;
        
        helper.renderLabel(context, "Switch Back:", null, startX, settingY);
        helper.renderToggle(context, mouseX, mouseY, startX + 150, settingY - 5, switchBack);
        settingY += 35;
        
        helper.renderLabel(context, "Throw Delay:", String.format("%d ticks", throwDelay), startX, settingY);
        helper.renderSlider(context, startX + 200, settingY, 150, throwDelay, 0.0, 20.0);
        settingY += 35;
        
        helper.renderInfo(context, "Automatically throws ender pearl when", startX, settingY);
        helper.renderInfo(context, "activated, then disables itself.", startX, settingY + 10);
        
        return settingY + 20;
    }
    
    @Override
    public boolean handleSettingsClick(double mouseX, double mouseY, int startX, int startY, int width, SettingsHelper helper) {
        int settingY = startY;
        
         
        if (helper.isToggleHovered(mouseX, mouseY, startX + 150, settingY - 5)) {
            setSwitchBack(!switchBack);
            return true;
        }
        settingY += 35;
        
         
        if (helper.isSliderHovered(mouseX, mouseY, startX + 200, settingY, 150)) {
            int newValue = (int) helper.calculateSliderValue(mouseX, startX + 200, 150, 0.0, 20.0);
            setThrowDelay(newValue);
            return true;
        }
        
        return false;
    }
}