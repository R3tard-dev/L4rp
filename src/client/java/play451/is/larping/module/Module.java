package play451.is.larping.module;

import play451.is.larping.module.setting.Setting;
import java.util.ArrayList;
import java.util.List;

public abstract class Module {
    private final String name;
    private final Category category;
    private final String description;
    private boolean enabled;
    protected final List<Setting<?>> settings = new ArrayList<>();

    public Module(String name, Category category, String description) {
        this.name = name;
        this.category = category;
        this.description = description;
        this.enabled = false;
    }

    public Module(String name, Category category) {
        this(name, category, "");
    }

    public String getName()        { return name; }
    public Category getCategory()  { return category; }
    public String getDescription() { return description; }
    public boolean isEnabled()     { return enabled; }
    public List<Setting<?>> getSettings() { return settings; }

    public void toggle() {
        enabled = !enabled;
        if (enabled) onEnable();
        else onDisable();
    }

    public void onEnable()  {}
    public void onDisable() {}
}