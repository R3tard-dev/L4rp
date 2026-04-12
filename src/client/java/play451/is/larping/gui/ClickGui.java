package play451.is.larping.gui;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import play451.is.larping.gui.api.Frame;
import play451.is.larping.module.Category;
import play451.is.larping.module.impl.core.ClickGuiModule;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class ClickGui extends Screen {

    private final List<Frame> frames = new ArrayList<>();

    public ClickGui() {
        super(Text.literal("clickgui"));
    }

    @Override
    protected void init() {
        frames.clear();
        int x = 6;
        for (Category cat : Category.values()) {
            frames.add(new Frame(cat, x, 3, 100, 13));
            x += 104;
        }
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        if (ClickGuiModule.INSTANCE != null && ClickGuiModule.INSTANCE.blur.getValue()) {
            applyBlur();
        }
        context.fill(0, 0, this.width, this.height, 0x88000000);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        for (Frame frame : frames) frame.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dX, double dY) {
        for (Frame frame : frames) frame.mouseDragged(mouseX, mouseY, button, dX, dY);
        return super.mouseDragged(mouseX, mouseY, button, dX, dY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        for (Frame frame : frames) frame.mouseClicked(mouseX, mouseY, button);
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        for (Frame frame : frames) frame.mouseReleased(mouseX, mouseY, button);
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double hAmt, double vAmt) {
        int speed = ClickGuiModule.INSTANCE != null ? (int) Math.round(ClickGuiModule.INSTANCE.scrollSpeed.getValue()) : 15;
        for (Frame frame : frames) frame.mouseScrolled(mouseX, mouseY, hAmt, vAmt * speed);
        return super.mouseScrolled(mouseX, mouseY, hAmt, vAmt);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        for (Frame frame : frames) frame.keyPressed(keyCode, scanCode, modifiers);
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        for (Frame frame : frames) frame.charTyped(chr, modifiers);
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
    public boolean shouldPause() {
        return false;
    }

    public static Color getHeaderColor(int index) {
        if (ClickGuiModule.INSTANCE == null) return new Color(15, 112, 112, 255);
        int packed = ClickGuiModule.INSTANCE.color.getPacked();
        int a = (packed >> 24) & 0xFF;
        int r = (packed >> 16) & 0xFF;
        int g = (packed >> 8)  & 0xFF;
        int b =  packed        & 0xFF;
        return new Color(r, g, b, Math.max(1, a));
    }
}