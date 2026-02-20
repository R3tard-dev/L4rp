package play451.is.larping.features.modules;

import net.minecraft.client.MinecraftClient;
import play451.is.larping.chat.ChatUtils;
import play451.is.larping.config.Config;
import java.util.HashMap;
import java.util.Map;

public abstract class Module {
    private final String name;
    private final String description;
    private final ModuleCategory category;
    private boolean enabled;
    private int keyBind = -1; 
    
    public Module(String name, String description, ModuleCategory category) {
        this.name = name;
        this.description = description;
        this.category = category;
        this.enabled = false;
        ModuleManager.getInstance().registerModule(this);
    }
    
    public String getName() { return name; }
    public String getDescription() { return description; }
    public ModuleCategory getCategory() { return category; }
    public boolean isEnabled() { return enabled; }
    
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

        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player != null && mc.player.age > 20) {
            ChatUtils.moduleToggle(this.name, enabled);
        }
        
        if (ModuleManager.getInstance().getModules().size() > 0) {
            Config.getInstance().saveModules();
        }
    }
    
    public int getKeyBind() { return keyBind; }
    public void setKeyBind(int keyBind) {
        this.keyBind = keyBind;
        Config.getInstance().saveModules();
    }
    
    public String getKeyBindName() {
        return SettingsHelper.getKeyName(keyBind);
    }
    
    public void onEnable() {}
    public void onDisable() {}
    public void onTick() {}
    
    public Map<String, Object> saveSettings() {
        Map<String, Object> settings = new HashMap<>();
        settings.put("__keyBind", keyBind);
        return settings;
    }
    
    public void loadSettings(Map<String, Object> settings) {
        if (settings.containsKey("__keyBind")) {
            keyBind = ((Number) settings.get("__keyBind")).intValue();
        }
    }
}