package play451.is.larping.features.modules;

import org.lwjgl.glfw.GLFW;
import play451.is.larping.config.Config;

import java.util.HashMap;
import java.util.Map;

public abstract class Module {
    
    private final String name;
    private final String description;
    private final ModuleCategory category;
    private boolean enabled = false;
    
    
    private int keyBind = -1;
    
    public Module(String name, String description, ModuleCategory category) {
        this.name = name;
        this.description = description;
        this.category = category;
    }
    
    
    public final void tick() {
        
        if (keyBind != -1) {
            
            
        }
        onTick();
    }
    
    public void onEnable() {}
    public void onDisable() {}
    public void onTick() {}
    
    public void toggle() {
        this.enabled = !this.enabled;
        if (this.enabled) {
            onEnable();
        } else {
            onDisable();
        }
    }
    
    public boolean isEnabled() {
        return enabled;
    }
    
    public void setEnabled(boolean enabled) {
        if (this.enabled != enabled) {
            toggle();
        }
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
    
    
    public int getKeyBind() {
        return keyBind;
    }
    
    public void setKeyBind(int keyBind) {
        this.keyBind = keyBind;
        Config.getInstance().saveModules();
    }
    
    public String getKeyBindName() {
        return play451.is.larping.features.modules.SettingsHelper.getKeyName(keyBind);
    }
    
    
    public Map<String, Object> saveSettings() {
        return new HashMap<>();
    }
    
    public void loadSettings(Map<String, Object> settings) {}
    
    
    public final Map<String, Object> saveAll() {
        Map<String, Object> all = new HashMap<>(saveSettings());
        all.put("__keyBind", keyBind);
        all.put("__enabled", enabled);
        return all;
    }
    
    public final void loadAll(Map<String, Object> settings) {
        if (settings.containsKey("__keyBind")) {
            keyBind = ((Number) settings.get("__keyBind")).intValue();
        }
        if (settings.containsKey("__enabled")) {
            boolean savedEnabled = (Boolean) settings.get("__enabled");
            if (savedEnabled != this.enabled) {
                toggle();
            }
        }
        loadSettings(settings);
    }
}