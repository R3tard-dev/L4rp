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

    public final BooleanSetting sounds       = new BooleanSetting("Sounds",       "Plays Minecraft UI sounds when interacting with the GUI.", true);
    public final BooleanSetting blur         = new BooleanSetting("Blur",         "Whether or not to blur the background behind the GUI.", false);
    public final SliderSetting  scrollSpeed  = new SliderSetting("ScrollSpeed",   "The speed at which scrolling of the frames will be at.", 15, 1, 50, 1);
    public final ColorSetting   color        = new ColorSetting("Color",          "The accent color used throughout the GUI.", 15, 112, 112, 255);
    public final ColorSetting   overlayColor = new ColorSetting("OverlayColor",   "Background overlay color of the screen.", 0, 0, 0, 136);
    public final SliderSetting  frameOpacity = new SliderSetting("FrameOpacity",  "Opacity of the module list body background.", 220, 0, 255, 1);

    public ClickGuiModule() {
        super("ClickGUI", Category.CORE, "Allows you to change and interact with the client's modules and settings through a GUI.");
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

    public static int getModuleBg(boolean enabled, boolean hovered, java.awt.Color accent) {
        if (enabled) {
            return (180 << 24) | (accent.getRed() << 16) | (accent.getGreen() << 8) | accent.getBlue();
        }
        return hovered ? 0xCC1E1E1E : 0xCC0E0E0E;
    }

    public static int getFrameBodyColor(int aR, int aG, int aB) {
        int opacity = INSTANCE != null ? (int) Math.round(INSTANCE.frameOpacity.getValue()) : 220;
        int r = Math.max(0, aR - 10);
        int g = Math.max(0, aG - 10);
        int b = Math.max(0, aB - 10);
        return (opacity << 24) | (r << 16) | (g << 8) | b;
    }
}