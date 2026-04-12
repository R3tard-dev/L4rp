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

    public final BooleanSetting sounds       = new BooleanSetting("Sounds",      "Plays UI sounds when interacting with the GUI.", true);
    public final BooleanSetting blur         = new BooleanSetting("Blur",        "Blur the background behind the GUI.", false);
    public final SliderSetting  scrollSpeed  = new SliderSetting("ScrollSpeed",  "Speed of frame scrolling.", 15, 1, 50, 1);
    public final ColorSetting   color        = new ColorSetting("Color",         "Accent color used throughout the GUI.", 15, 112, 112, 255);
    public final ColorSetting   overlayColor = new ColorSetting("OverlayColor",  "Background overlay color.", 0, 0, 0, 136);
    public final SliderSetting  frameOpacity = new SliderSetting("FrameOpacity", "Opacity of the module list background.", 220, 0, 255, 1);

    public ClickGuiModule() {
        super("ClickGUI", Category.CORE, "GUI for managing modules.");
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
        if (client.player == null) { toggle(); return; }
        client.execute(() -> client.setScreen(new ClickGui()));
    }

    @Override
    public void onDisable() {
        MinecraftClient client = MinecraftClient.getInstance();
        client.execute(() -> {
            if (client.currentScreen instanceof ClickGui) client.setScreen(null);
        });
    }

    public static int getFrameBodyColor(int aR, int aG, int aB) {
        int op = INSTANCE != null ? (int) Math.round(INSTANCE.frameOpacity.getValue()) : 220;
        return (op << 24) | (Math.max(0, aR - 10) << 16)
                          | (Math.max(0, aG - 10) << 8)
                          |  Math.max(0, aB - 10);
    }
}