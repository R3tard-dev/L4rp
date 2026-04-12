package play451.is.larping.module.setting;

import lombok.Getter;

@Getter
public class SliderSetting extends Setting<Double> {
    private final double min;
    private final double max;
    private final double increment;

    public SliderSetting(String name, String description, double defaultValue, double min, double max, double increment) {
        super(name, name, description, defaultValue);
        this.min = min;
        this.max = max;
        this.increment = increment;
    }

    public SliderSetting(String name, String description, double defaultValue, double min, double max, double increment, Visibility visibility) {
        super(name, name, description, visibility);
        this.value = defaultValue;
        this.min = min;
        this.max = max;
        this.increment = increment;
    }

    @Override
    public void setValue(Double value) {
        double snapped = Math.round(value / increment) * increment;
        this.value = Math.max(min, Math.min(max, snapped));
    }

    public String getDisplayValue() {
        if (increment >= 1.0) return String.valueOf((int) Math.round(value));
        return String.format("%.2f", value);
    }
}