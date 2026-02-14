package play451.is.larping.features.modules;

public enum ModuleCategory {
    COMBAT("Combat"),
    MOVEMENT("Movement"),
    PLAYER("Player"),
    RENDER("Render"),
    WORLD("World"),
    MISC("Misc"),
    CLIENT("Client");
    
    private final String name;
    
    ModuleCategory(String name) {
        this.name = name;
    }
    
    public String getName() {
        return name;
    }
}