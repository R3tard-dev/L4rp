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

    public final BooleanSetting sounds      = new BooleanSetting("Sounds",      "Plays Minecraft UI sounds when interacting with the GUI.", true);
    public final BooleanSetting blur        = new BooleanSetting("Blur",        "Whether or not to blur the background behind the GUI.", false);
    public final SliderSetting  scrollSpeed = new SliderSetting("ScrollSpeed",  "The speed at which scrolling of the frames will be at.", 15, 1, 50, 1);
    public final ColorSetting   color       = new ColorSetting("Color",         "The color used throughout the GUI.", 15, 112, 112, 255);

    public ClickGuiModule() {
        super("ClickGUI", Category.CORE, "Allows you to change and interact with the client's modules and settings through a GUI.");
        INSTANCE = this;
        settings.add(sounds);
        settings.add(blur);
        settings.add(scrollSpeed);
        settings.add(color);
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
            if (client.currentScreen instanceof ClickGui) {
                client.setScreen(null);
            }
        });
    }
}