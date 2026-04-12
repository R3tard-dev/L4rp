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
    public final ColorSetting   color       = new ColorSetting("Color",         "The accent color used throughout the GUI.", 15, 112, 112, 255);
    public final ColorSetting   moduleColor = new ColorSetting("ModuleColor",   "Background color of module rows.", 12, 12, 12, 220);
    public final ColorSetting   enabledColor= new ColorSetting("EnabledColor",  "Background color of enabled module rows.", 10, 80, 80, 200);
    public final ColorSetting   overlayColor= new ColorSetting("OverlayColor",  "Background overlay color of the screen.", 0, 0, 0, 136);
    public final SliderSetting  borderSize  = new SliderSetting("BorderSize",   "Thickness of the frame border in pixels.", 1, 0, 4, 1);
    public final SliderSetting  borderOpacity = new SliderSetting("BorderOpacity", "Opacity of the frame border (0-255).", 180, 0, 255, 1);
    public final SliderSetting  frameOpacity  = new SliderSetting("FrameOpacity",  "Opacity of the frame module list background.", 220, 0, 255, 1);

    public ClickGuiModule() {
        super("ClickGUI", Category.CORE, "Allows you to change and interact with the client's modules and settings through a GUI.");
        INSTANCE = this;
        settings.add(sounds);
        settings.add(blur);
        settings.add(scrollSpeed);
        settings.add(color);
        settings.add(moduleColor);
        settings.add(enabledColor);
        settings.add(overlayColor);
        settings.add(borderSize);
        settings.add(borderOpacity);
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
            if (client.currentScreen instanceof ClickGui) {
                client.setScreen(null);
            }
        });
    }

    public static int getAccentPacked() {
        if (INSTANCE == null) return 0xFF0F7070;
        return INSTANCE.color.getPacked();
    }

    public static int getModuleBg(boolean enabled, boolean hovered) {
        if (INSTANCE == null) return enabled ? 0xCC0F5050 : hovered ? 0xCC1E1E1E : 0xCC0E0E0E;
        if (enabled) {
            int p = INSTANCE.enabledColor.getPacked();
            return (INSTANCE.enabledColor.getA() << 24) | (p & 0x00FFFFFF);
        }
        if (hovered) return 0xCC1E1E1E;
        int p = INSTANCE.moduleColor.getPacked();
        return (INSTANCE.moduleColor.getA() << 24) | (p & 0x00FFFFFF);
    }

    public static int getBorderSize() {
        if (INSTANCE == null) return 1;
        return (int) Math.round(INSTANCE.borderSize.getValue());
    }

    public static int getBorderColor(int accentR, int accentG, int accentB) {
        if (INSTANCE == null) return 0xB40A4040;
        int opacity = (int) Math.round(INSTANCE.borderOpacity.getValue());
        return (opacity << 24) | (accentR << 16) | (accentG << 8) | accentB;
    }

    public static int getFrameBodyColor(int accentR, int accentG, int accentB) {
        if (INSTANCE == null) return 0xDC0A0A0A;
        int opacity = (int) Math.round(INSTANCE.frameOpacity.getValue());
        int r = Math.max(0, accentR - 10);
        int g = Math.max(0, accentG - 10);
        int b = Math.max(0, accentB - 10);
        return (opacity << 24) | (r << 16) | (g << 8) | b;
    }
}