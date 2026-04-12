package play451.is.larping.module.setting;

public class BooleanSetting extends Setting<Boolean> {

    public BooleanSetting(String name, String description, boolean defaultValue) {
        super(name, name, description, defaultValue);
    }

    public void toggle() { value = !value; }
}