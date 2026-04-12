package play451.is.larping.module.setting;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@AllArgsConstructor
public class Setting<T> {
    private final String name;
    private final String tag;
    private final String description;
    private final Visibility visibility;
    protected T value;

    public Setting(String name, String tag, String description, T defaultValue) {
        this.name = name;
        this.tag = tag;
        this.description = description;
        this.value = defaultValue;
        this.visibility = new Visibility();
    }

    public T getValue() { return value; }
    public void setValue(T value) { this.value = value; }

    @Getter
    @Setter
    @RequiredArgsConstructor
    public static class Visibility {
        private final Setting<?> setting;
        private boolean visible = true;

        public Visibility() {
            this.setting = null;
        }

        public void update() {}

        public boolean isVisible() { return visible; }
    }
}