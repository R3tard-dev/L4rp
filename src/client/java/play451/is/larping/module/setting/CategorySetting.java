package play451.is.larping.module.setting;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CategorySetting extends Setting<String> {
    private boolean open = false;

    public CategorySetting(String name, String description) {
        super(name, name, description, name);
    }

    public CategorySetting(String name, String description, Visibility visibility) {
        super(name, name, description, visibility);
        this.value = name;
    }

    public static class Visibility extends Setting.Visibility {
        private final CategorySetting category;

        public Visibility(CategorySetting category) {
            super(category);
            this.category = category;
        }

        @Override
        public void update() {
            if (category.getVisibility() != null) {
                category.getVisibility().update();
                if (!category.getVisibility().isVisible()) {
                    setVisible(false);
                    return;
                }
            }
            setVisible(category.isOpen());
        }
    }
}