package play451.is.larping.features.modules.movement;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import play451.is.larping.config.Config;
import play451.is.larping.features.modules.Module;
import play451.is.larping.features.modules.ModuleCategory;
import play451.is.larping.features.modules.ModuleSettingsRenderer;
import play451.is.larping.features.modules.SettingsHelper;

import java.util.HashMap;
import java.util.Map;

public class AutoWalk extends Module implements ModuleSettingsRenderer {
    
     
    private boolean ignoreJump = false;  
    private boolean ignoreSneak = false;  
    private boolean autoSprint = false;  
    private boolean sprintJump = false;  
    private int jumpDelay = 10;  
    
     
    private int jumpCounter = 0;
    
    private final MinecraftClient mc = MinecraftClient.getInstance();
    
    public AutoWalk() {
        super("AutoWalk", "Automatically walks forward", ModuleCategory.MOVEMENT);
    }
    
    @Override
    public void onEnable() {
        if (mc.player == null) {
            this.toggle();
            return;
        }
        jumpCounter = 0;
    }
    
    @Override
    public void onDisable() {
        if (mc.player != null && mc.options != null) {
             
            mc.options.forwardKey.setPressed(false);
             
            if (autoSprint) {
                mc.options.sprintKey.setPressed(false);
            }
             
            if (sprintJump) {
                mc.options.jumpKey.setPressed(false);
            }
        }
        jumpCounter = 0;
    }
    
    @Override
    public void onTick() {
        if (mc.player == null || mc.world == null) {
            this.toggle();
            return;
        }
        
         
        boolean shouldWalk = true;
        
        if (!ignoreJump && mc.options.jumpKey.isPressed() && !sprintJump) {
            shouldWalk = false;
        }
        
        if (!ignoreSneak && mc.options.sneakKey.isPressed()) {
            shouldWalk = false;
        }
        
         
        mc.options.forwardKey.setPressed(shouldWalk);
        
         
        if (autoSprint && shouldWalk) {
            mc.options.sprintKey.setPressed(true);
        } else if (autoSprint) {
            mc.options.sprintKey.setPressed(false);
        }
        
         
        if (sprintJump && autoSprint && shouldWalk && mc.player.isOnGround()) {
            jumpCounter++;
            if (jumpCounter >= jumpDelay) {
                mc.options.jumpKey.setPressed(true);
                jumpCounter = 0;
            }
        } else if (sprintJump) {
             
            if (!mc.player.isOnGround()) {
                mc.options.jumpKey.setPressed(false);
            }
        }
    }
    
     
    @Override
    public Map<String, Object> saveSettings() {
        Map<String, Object> settings = new HashMap<>();
        settings.put("ignoreJump", ignoreJump);
        settings.put("ignoreSneak", ignoreSneak);
        settings.put("autoSprint", autoSprint);
        settings.put("sprintJump", sprintJump);
        settings.put("jumpDelay", jumpDelay);
        return settings;
    }
    
    @Override
    public void loadSettings(Map<String, Object> settings) {
        if (settings.containsKey("ignoreJump")) {
            ignoreJump = (Boolean) settings.get("ignoreJump");
        }
        if (settings.containsKey("ignoreSneak")) {
            ignoreSneak = (Boolean) settings.get("ignoreSneak");
        }
        if (settings.containsKey("autoSprint")) {
            autoSprint = (Boolean) settings.get("autoSprint");
        }
        if (settings.containsKey("sprintJump")) {
            sprintJump = (Boolean) settings.get("sprintJump");
        }
        if (settings.containsKey("jumpDelay")) {
            jumpDelay = ((Number) settings.get("jumpDelay")).intValue();
        }
    }
    
     
    public void setIgnoreJump(boolean ignoreJump) {
        this.ignoreJump = ignoreJump;
        Config.getInstance().saveModules();
    }
    
    public boolean isIgnoreJump() {
        return ignoreJump;
    }
    
    public void setIgnoreSneak(boolean ignoreSneak) {
        this.ignoreSneak = ignoreSneak;
        Config.getInstance().saveModules();
    }
    
    public boolean isIgnoreSneak() {
        return ignoreSneak;
    }
    
    public void setAutoSprint(boolean autoSprint) {
        this.autoSprint = autoSprint;
         
        if (!autoSprint && sprintJump) {
            sprintJump = false;
        }
        Config.getInstance().saveModules();
    }
    
    public boolean isAutoSprint() {
        return autoSprint;
    }
    
    public void setSprintJump(boolean sprintJump) {
         
        if (sprintJump && !autoSprint) {
            autoSprint = true;
        }
        this.sprintJump = sprintJump;
        Config.getInstance().saveModules();
    }
    
    public boolean isSprintJump() {
        return sprintJump;
    }
    
    public void setJumpDelay(int jumpDelay) {
        this.jumpDelay = Math.max(1, Math.min(40, jumpDelay));
        Config.getInstance().saveModules();
    }
    
    public int getJumpDelay() {
        return jumpDelay;
    }
    
     
    @Override
    public int renderSettings(DrawContext context, int mouseX, int mouseY, int startX, int startY, int width, SettingsHelper helper) {
        int settingY = startY;
        
         
        helper.renderLabel(context, "Ignore Jump:", null, startX, settingY);
        helper.renderToggle(context, mouseX, mouseY, startX + 150, settingY - 5, ignoreJump);
        settingY += 35;
        
         
        helper.renderLabel(context, "Ignore Sneak:", null, startX, settingY);
        helper.renderToggle(context, mouseX, mouseY, startX + 150, settingY - 5, ignoreSneak);
        settingY += 35;
        
         
        helper.renderLabel(context, "Auto Sprint:", null, startX, settingY);
        helper.renderToggle(context, mouseX, mouseY, startX + 150, settingY - 5, autoSprint);
        settingY += 35;
        
         
        if (autoSprint) {
            helper.renderLabel(context, "Sprint Jump:", null, startX + 20, settingY);
            helper.renderToggle(context, mouseX, mouseY, startX + 150, settingY - 5, sprintJump);
            settingY += 35;
            
             
            if (sprintJump) {
                helper.renderLabel(context, "Jump Delay:", String.format("%d ticks", jumpDelay), startX + 40, settingY);
                helper.renderSlider(context, startX + 200, settingY, 150, jumpDelay, 1.0, 40.0);
                settingY += 35;
            }
        }
        
         
        helper.renderInfo(context, "Stops walking when jump/sneak pressed", startX, settingY);
        helper.renderInfo(context, "(unless ignored)", startX, settingY + 10);
        
        return settingY + 20;
    }
    
    @Override
    public boolean handleSettingsClick(double mouseX, double mouseY, int startX, int startY, int width, SettingsHelper helper) {
        int settingY = startY;
        
         
        if (helper.isToggleHovered(mouseX, mouseY, startX + 150, settingY - 5)) {
            setIgnoreJump(!ignoreJump);
            return true;
        }
        settingY += 35;
        
         
        if (helper.isToggleHovered(mouseX, mouseY, startX + 150, settingY - 5)) {
            setIgnoreSneak(!ignoreSneak);
            return true;
        }
        settingY += 35;
        
         
        if (helper.isToggleHovered(mouseX, mouseY, startX + 150, settingY - 5)) {
            setAutoSprint(!autoSprint);
            return true;
        }
        settingY += 35;
        
         
        if (autoSprint) {
            if (helper.isToggleHovered(mouseX, mouseY, startX + 150, settingY - 5)) {
                setSprintJump(!sprintJump);
                return true;
            }
            settingY += 35;
            
             
            if (sprintJump) {
                if (helper.isSliderHovered(mouseX, mouseY, startX + 200, settingY, 150)) {
                    int newValue = (int) helper.calculateSliderValue(mouseX, startX + 200, 150, 1.0, 40.0);
                    setJumpDelay(newValue);
                    return true;
                }
            }
        }
        
        return false;
    }
}