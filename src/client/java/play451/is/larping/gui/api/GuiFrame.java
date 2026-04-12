package play451.is.larping.gui.api;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import play451.is.larping.gui.ClickGui;
import play451.is.larping.module.Category;
import play451.is.larping.module.Module;
import play451.is.larping.module.ModuleManager;
import play451.is.larping.module.impl.core.ClickGuiModule;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class GuiFrame {
    private final Category     category;
    private int                x, y, width;
    private boolean            dragging;
    private double             dragOffX, dragOffY;
    private final List<Button> buttons = new ArrayList<>();

    private static final int LABEL_H = 10;

    public GuiFrame(Category category, int x, int y, int width) {
        this.category = category;
        this.x        = x;
        this.y        = y;
        this.width    = width;
        buildButtons();
    }

    private void buildButtons() {
        buttons.clear();
        for (Module mod : ModuleManager.getModulesForCategory(category)) {
            buttons.add(new ModuleButton(mod, this, 0));
        }
    }

    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        var   tr     = MinecraftClient.getInstance().textRenderer;
        Color accent = ClickGui.getHeaderColor(y);
        int   aR     = accent.getRed();
        int   aG     = accent.getGreen();
        int   aB     = accent.getBlue();

        context.drawTextWithShadow(tr, category.getName(),
                x + width / 2 - tr.getWidth(category.getName()) / 2,
                y + 1, (180 << 24) | (aR << 16) | (aG << 8) | aB);

        context.fill(x, y + LABEL_H, x + width, y + LABEL_H + 1,
                (60 << 24) | (aR << 16) | (aG << 8) | aB);

        int btnY = y + LABEL_H + 1;
        for (Button btn : buttons) {
            btn.setX(x);
            btn.setY(btnY);
            btn.render(context, mouseX, mouseY, delta);
            btnY += btn.getHeight();
        }
    }

    public int getTotalHeight() {
        int h = LABEL_H + 1;
        for (Button btn : buttons) h += btn.getHeight();
        return h;
    }

    public void mouseClicked(double mouseX, double mouseY, int button) {
        if (isHoveringLabel(mouseX, mouseY)) {
            if (button == 0) {
                dragging = true;
                dragOffX = mouseX - x;
                dragOffY = mouseY - y;
            }
            return;
        }
        for (Button btn : buttons) btn.mouseClicked(mouseX, mouseY, button);
    }

    public void mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0) dragging = false;
        for (Button btn : buttons) btn.mouseReleased(mouseX, mouseY, button);
    }

    public void mouseDragged(double mouseX, double mouseY, int button, double dX, double dY) {
        if (dragging && button == 0) {
            x = (int)(mouseX - dragOffX);
            y = (int)(mouseY - dragOffY);
        }
        for (Button btn : buttons) btn.mouseDragged(mouseX, mouseY, button, dX, dY);
    }

    public void mouseScrolled(double mouseX, double mouseY, double hAmt, double vAmt) {
        for (Button btn : buttons) btn.mouseScrolled(mouseX, mouseY, hAmt, vAmt);
    }

    public void keyPressed(int keyCode, int scanCode, int modifiers) {
        for (Button btn : buttons) btn.keyPressed(keyCode, scanCode, modifiers);
    }

    public void charTyped(char chr, int modifiers) {
        for (Button btn : buttons) btn.charTyped(chr, modifiers);
    }

    private boolean isHoveringLabel(double mx, double my) {
        return mx >= x && mx < x + width && my >= y && my < y + LABEL_H;
    }

    public int      getX()        { return x; }
    public int      getY()        { return y; }
    public int      getWidth()    { return width; }
    public int      getHeight()   { return LABEL_H; }
    public Category getCategory() { return category; }
}