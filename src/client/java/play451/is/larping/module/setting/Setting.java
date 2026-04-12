package play451.is.larping.module.setting;

public abstract class Setting<T> {
    private final String name;
    private final String tag;
    private final String description;
    private final Visibility visibility;
    protected T value;

    public Setting(String name, String tag, String description, T defaultValue) {
        this.name        = name;
        this.tag         = tag;
        this.description = description;
        this.value       = defaultValue;
        this.visibility  = new Visibility();
    }

    public String     getName()       { return name; }
    public String     getTag()        { return tag; }
    public String     getDescription(){ return description; }
    public Visibility getVisibility() { return visibility; }
    public T          getValue()      { return value; }
    public void       setValue(T v)   { this.value = v; }

    public static class Visibility {
        private boolean visible = true;

        public boolean isVisible()           { return visible; }
        public void    setVisible(boolean v) { this.visible = v; }
        public void    update()              {}
    }
}