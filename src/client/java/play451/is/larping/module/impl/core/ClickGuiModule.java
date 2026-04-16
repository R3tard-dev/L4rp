package play451.is.larping.module.impl.core;

import net.minecraft.client.MinecraftClient;
import play451.is.larping.gui.ClickGui;
import play451.is.larping.module.Category;
import play451.is.larping.module.Module;
import play451.is.larping.module.setting.BooleanSetting;
import play451.is.larping.module.setting.ColorSetting;
import play451.is.larping.module.setting.SliderSetting;

public class ClickGuiModule extends Module {

    public static ClickGuiModule INSTANCE;

    public final BooleanSetting sounds = new BooleanSetting("Sounds", "", true);
    public final BooleanSetting blur = new BooleanSetting("Blur", "", true);
    public final SliderSetting scrollSpeed = new SliderSetting("ScrollSpeed", "", 15, 1, 50, 1);
    public final ColorSetting color = new ColorSetting("Color", "", 15, 112, 112, 255);
    public final ColorSetting overlayColor = new ColorSetting("OverlayColor", "", 0, 0, 0, 170);
    public final SliderSetting frameOpacity = new SliderSetting("FrameOpacity", "", 140, 0, 255, 1);

    public ClickGuiModule() {
        super("ClickGUI", Category.CORE, "");
        INSTANCE = this;
        settings.add(sounds);
        settings.add(blur);
        settings.add(scrollSpeed);
        settings.add(color);
        settings.add(overlayColor);
        settings.add(frameOpacity);
    }

    @Override
    public void onEnable() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) {
            toggle();
            return;
        }
        client.execute(() -> client.setScreen(new ClickGui()));
    }

    @Override
    public void onDisable() {
        MinecraftClient client = MinecraftClient.getInstance();
        client.execute(() -> {
            if (client.currentScreen instanceof ClickGui) client.setScreen(null);
        });
    }
}