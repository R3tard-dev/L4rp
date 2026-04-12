package play451.is.larping.module.setting;

public class CategorySetting extends Setting<String> {
    private boolean open = false;

    public CategorySetting(String name, String description) {
        super(name, name, description, name);
    }

    public boolean isOpen()          { return open; }
    public void    setOpen(boolean v){ this.open = v; }
}