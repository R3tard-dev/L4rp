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

    private final List<GuiFrame> frames        = new ArrayList<>();
    private final SettingsPanel  settingsPanel  = new SettingsPanel(4, 16);

    private static final int GEAR_X = 4;
    private static final int GEAR_Y = 4;
    private static final int GEAR_W = 50;
    private static final int GEAR_H = 10;

    public ClickGui() {
        super(Text.literal("clickgui"));
    }

    @Override
    protected void init() {
        frames.clear();
        int x = 6;
        for (Category cat : Category.values()) {
            frames.add(new GuiFrame(cat, x, 3, 100));
            x += 104;
        }
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        if (ClickGuiModule.INSTANCE != null && ClickGuiModule.INSTANCE.blur.getValue()) {
            applyBlur(context);
        }
        int ov = ClickGuiModule.INSTANCE != null
                ? ClickGuiModule.INSTANCE.overlayColor.getPacked()
                : 0x88000000;
        context.fill(0, 0, this.width, this.height, ov);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        for (GuiFrame frame : frames) frame.render(context, mouseX, mouseY, delta);

        Color  accent  = getHeaderColor(0);
        int    ap      = 0xFF000000 | (accent.getRed() << 16) | (accent.getGreen() << 8) | accent.getBlue();
        boolean gearHov = mouseX >= GEAR_X && mouseX < GEAR_X + GEAR_W
                       && mouseY >= GEAR_Y && mouseY < GEAR_Y + GEAR_H;
        context.fill(GEAR_X, GEAR_Y, GEAR_X + GEAR_W, GEAR_Y + GEAR_H,
                gearHov ? withAlpha(accent, 180) : withAlpha(accent, 100));
        context.drawCenteredTextWithShadow(textRenderer, "\u2699 Settings",
                GEAR_X + GEAR_W / 2, GEAR_Y + 1, 0xFFFFFFFF);

        settingsPanel.render(context, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0
         && mouseX >= GEAR_X && mouseX < GEAR_X + GEAR_W
         && mouseY >= GEAR_Y && mouseY < GEAR_Y + GEAR_H) {
            settingsPanel.toggle();
            return true;
        }

        if (settingsPanel.isVisible()
         && settingsPanel.mouseClicked(mouseX, mouseY, button)) return true;

        for (GuiFrame frame : frames) frame.mouseClicked(mouseX, mouseY, button);
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dX, double dY) {
        if (settingsPanel.mouseDragged(mouseX, mouseY, button, dX, dY)) return true;
        for (GuiFrame frame : frames) frame.mouseDragged(mouseX, mouseY, button, dX, dY);
        return super.mouseDragged(mouseX, mouseY, button, dX, dY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        settingsPanel.mouseReleased(mouseX, mouseY, button);
        for (GuiFrame frame : frames) frame.mouseReleased(mouseX, mouseY, button);
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double hAmt, double vAmt) {
        int speed = ClickGuiModule.INSTANCE != null
                ? (int) Math.round(ClickGuiModule.INSTANCE.scrollSpeed.getValue()) : 15;
        for (GuiFrame frame : frames) frame.mouseScrolled(mouseX, mouseY, hAmt, vAmt * speed);
        return super.mouseScrolled(mouseX, mouseY, hAmt, vAmt);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        for (GuiFrame frame : frames) frame.keyPressed(keyCode, scanCode, modifiers);
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        for (GuiFrame frame : frames) frame.charTyped(chr, modifiers);
        return super.charTyped(chr, modifiers);
    }

    @Override
    public void close() {
        super.close();
        if (ClickGuiModule.INSTANCE != null && ClickGuiModule.INSTANCE.isEnabled()) {
            ClickGuiModule.INSTANCE.toggle();
        }
    }

    @Override
    public boolean shouldPause() { return false; }

    public static Color getHeaderColor(int ignored) {
        if (ClickGuiModule.INSTANCE == null) return new Color(15, 112, 112, 255);
        int packed = ClickGuiModule.INSTANCE.color.getPacked();
        int a = (packed >> 24) & 0xFF;
        int r = (packed >> 16) & 0xFF;
        int g = (packed >> 8)  & 0xFF;
        int b =  packed        & 0xFF;
        return new Color(r, g, b, Math.max(1, a));
    }

    private static int withAlpha(Color c, int a) {
        return (a << 24) | (c.getRed() << 16) | (c.getGreen() << 8) | c.getBlue();
    }
}