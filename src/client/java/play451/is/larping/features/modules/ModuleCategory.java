package play451.is.larping.features.modules;

public enum ModuleCategory {
    COMBAT("Combat", "⚔"),
    MOVEMENT("Movement", "➤"),
    PLAYER("Player", "👤"),
    RENDER("Render", "👁"),
    WORLD("World", "🌍"),
    MISC("Misc", "⚙"),
    CLIENT("Client", "💻");
    
    private final String name;
    private final String icon;
    
    ModuleCategory(String name, String icon) {
        this.name = name;
        this.icon = icon;
    }
    
    public String getName() {
        return name;
    }
    
    public String getIcon() {
        return icon;
    }
}