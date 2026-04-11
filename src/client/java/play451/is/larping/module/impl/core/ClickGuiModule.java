package play451.is.larping.module.impl.core;

import net.minecraft.client.MinecraftClient;
import play451.is.larping.gui.ClickGui;
import play451.is.larping.module.Category;
import play451.is.larping.module.Module;
import play451.is.larping.module.setting.ColorSetting;

public class ClickGuiModule extends Module {

    public static ClickGuiModule INSTANCE;

    public final ColorSetting headerColor  = new ColorSetting("Category Header", 15, 112, 112, 255);
    public final ColorSetting moduleColor  = new ColorSetting("Module BG",       10,  10,  10, 238);
    public final ColorSetting enabledColor = new ColorSetting("Active Module",   15,  80,  80, 238);
    public final ColorSetting borderColor  = new ColorSetting("Border",          10,  64,  64, 255);
    public final ColorSetting overlayColor = new ColorSetting("BG Overlay",       0,   0,   0,  85);

    public ClickGuiModule() {
        super("ClickGUI", Category.CORE);
        INSTANCE = this;
        settings.add(headerColor);
        settings.add(moduleColor);
        settings.add(enabledColor);
        settings.add(borderColor);
        settings.add(overlayColor);
    }

    @Override
    public void onEnable() {
        MinecraftClient client = MinecraftClient.getInstance();
        client.execute(() -> client.setScreen(new ClickGui()));
    }

    @Override
    public void onDisable() {}
}