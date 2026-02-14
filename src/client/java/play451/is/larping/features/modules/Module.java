package play451.is.larping.features.modules;

import play451.is.larping.config.Config;

import java.util.HashMap;
import java.util.Map;

public abstract class Module {
    private final String name;
    private final String description;
    private final ModuleCategory category;
    private boolean enabled;
    
    public Module(String name, String description, ModuleCategory category) {
        this.name = name;
        this.description = description;
        this.category = category;
        this.enabled = false;
        
        // Auto-register with ModuleManager
        ModuleManager.getInstance().registerModule(this);
    }
    
    public String getName() {
        return name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public ModuleCategory getCategory() {
        return category;
    }
    
    public boolean isEnabled() {
        return enabled;
    }
    
    public void toggle() {
        setEnabled(!enabled);
    }
    
    public void setEnabled(boolean enabled) {
        if (this.enabled == enabled) return;
        
        this.enabled = enabled;
        if (enabled) {
            onEnable();
        } else {
            onDisable();
        }
        
        // Save config when module state changes
        Config.getInstance().saveModules();
    }
    
    // Override these in your modules
    public void onEnable() {}
    public void onDisable() {}
    public void onTick() {}
    
    // Save/load settings - override in modules with settings
    public Map<String, Object> saveSettings() {
        return new HashMap<>();
    }
    
    public void loadSettings(Map<String, Object> settings) {
        // Override in modules with settings
    }
}