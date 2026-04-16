package play451.is.larping.gui;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import play451.is.larping.gui.api.GuiFrame;
import play451.is.larping.gui.api.SettingsPanel;
import play451.is.larping.module.Category;
import play451.is.larping.module.impl.core.ClickGuiModule;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class ClickGui extends Screen {

    private final List<GuiFrame> frames = new ArrayList<>();
    private final SettingsPanel settingsPanel = new SettingsPanel(4, 16);

    public ClickGui() {
        super(Text.literal("clickgui"));
    }

    @Override
    protected void init() {
        frames.clear();
        int x = 6;
        for (Category cat : Category.values()) {
            frames.add(new GuiFrame(cat, x, 4, 90));
            x += 94;
        }
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        if (ClickGuiModule.INSTANCE != null && ClickGuiModule.INSTANCE.blur.getValue()) {
            applyBlur(context);
        }
        int ov = ClickGuiModule.INSTANCE != null
                ? ClickGuiModule.INSTANCE.overlayColor.getPacked()
                : 0xAA000000;
        context.fill(0, 0, this.width, this.height, ov);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        for (GuiFrame frame : frames) frame.render(context, mouseX, mouseY, delta);
        settingsPanel.render(context, mouseX, mouseY);
    }

    public static Color getHeaderColor(int ignored) {
        if (ClickGuiModule.INSTANCE == null) return new Color(15, 112, 112, 255);
        int packed = ClickGuiModule.INSTANCE.color.getPacked();
        int a = (packed >> 24) & 0xFF;
        int r = (packed >> 16) & 0xFF;
        int g = (packed >> 8) & 0xFF;
        int b = packed & 0xFF;
        return new Color(r, g, b, Math.max(1, a));
    }
}