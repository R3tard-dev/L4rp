package play451.is.larping.module;

public enum Category {
    COMBAT("Combat"),
    PLAYER("Player"),
    VISUALS("Visuals"),
    MOVEMENT("Movement"),
    MISCELLANEOUS("Miscellaneous"),
    CORE("Core");

    private final String name;

    Category(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}