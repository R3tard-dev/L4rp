package play451.is.larping.features.modules.movement;

import net.minecraft.client.MinecraftClient;
import play451.is.larping.config.Config;
import play451.is.larping.features.modules.Module;
import play451.is.larping.features.modules.ModuleCategory;

import java.util.HashMap;
import java.util.Map;

public class AutoWalk extends Module {
    
     
    private boolean ignoreJump = false;  
    private boolean ignoreSneak = false;  
    
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
    }
    
    @Override
    public void onDisable() {
        if (mc.player != null && mc.options != null) {
             
            mc.options.forwardKey.setPressed(false);
        }
    }
    
    @Override
    public void onTick() {
        if (mc.player == null || mc.world == null) {
            this.toggle();
            return;
        }
        
         
        boolean shouldWalk = true;
        
        if (!ignoreJump && mc.options.jumpKey.isPressed()) {
            shouldWalk = false;
        }
        
        if (!ignoreSneak && mc.options.sneakKey.isPressed()) {
            shouldWalk = false;
        }
        
         
        mc.options.forwardKey.setPressed(shouldWalk);
    }
    
     
    @Override
    public Map<String, Object> saveSettings() {
        Map<String, Object> settings = new HashMap<>();
        settings.put("ignoreJump", ignoreJump);
        settings.put("ignoreSneak", ignoreSneak);
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
}