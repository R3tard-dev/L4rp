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
    private int                x, y, width, height;
    private boolean            dragging;
    private double             dragOffX, dragOffY;
    private boolean            expanded     = true;
    private int                scrollOffset = 0;
    private final List<Button> buttons      = new ArrayList<>();

    public GuiFrame(Category category, int x, int y, int width, int height) {
        this.category = category;
        this.x        = x;
        this.y        = y;
        this.width    = width;
        this.height   = height;
        buildButtons();
    }

    private void buildButtons() {
        buttons.clear();
        for (Module mod : ModuleManager.getModulesForCategory(category)) {
            buttons.add(new ModuleButton(mod, this, height));
        }
    }

    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        Color accent = ClickGui.getHeaderColor(y);
        int   aR     = accent.getRed();
        int   aG     = accent.getGreen();
        int   aB     = accent.getBlue();
        int   aA     = accent.getAlpha();

        context.fill(x, y, x + width, y + height,
                (aA << 24) | (aR << 16) | (aG << 8) | aB);
        drawCenteredText(context, category.getName(), x + width / 2, y + (height - 8) / 2, 0xFFFFFFFF);

        if (!expanded) return;

        int bodyH = getVisibleButtonHeight();
        context.fill(x, y + height, x + width, y + height + bodyH,
                ClickGuiModule.getFrameBodyColor(aR, aG, aB));

        int btnY = y + height;
        int idx  = 0;
        for (Button btn : buttons) {
            if (idx < scrollOffset) { idx++; continue; }
            btn.setX(x);
            btn.setY(btnY);
            btn.render(context, mouseX, mouseY, delta);
            btnY += btn.getHeight();
            idx++;
        }
    }

    private int getVisibleButtonHeight() {
        int total = 0;
        int idx   = 0;
        for (Button btn : buttons) {
            if (idx < scrollOffset) { idx++; continue; }
            total += btn.getHeight();
            idx++;
        }
        return total;
    }

    public boolean mouseClickedWithSettingsCallback(double mouseX, double mouseY, int button, SettingsPanel panel) {
        if (isHoveringHeader(mouseX, mouseY)) {
            if (button == 0) {
                dragging = true;
                dragOffX = mouseX - x;
                dragOffY = mouseY - y;
            } else if (button == 1) {
                panel.open((int) mouseX + 4, (int) mouseY);
            }
            return true;
        }
        if (!expanded) return false;
        for (Button btn : buttons) btn.mouseClicked(mouseX, mouseY, button);
        return false;
    }

    public void mouseClicked(double mouseX, double mouseY, int button) {
        mouseClickedWithSettingsCallback(mouseX, mouseY, button, null);
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
        if (!expanded) return;
        int totalH = height + getVisibleButtonHeight();
        if (mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + totalH) {
            int max = Math.max(0, buttons.size() - 10);
            scrollOffset = Math.max(0, Math.min(max, scrollOffset - (int) Math.signum(vAmt)));
        }
    }

    public void keyPressed(int keyCode, int scanCode, int modifiers) {
        for (Button btn : buttons) btn.keyPressed(keyCode, scanCode, modifiers);
    }

    public void charTyped(char chr, int modifiers) {
        for (Button btn : buttons) btn.charTyped(chr, modifiers);
    }

    private boolean isHoveringHeader(double mx, double my) {
        return mx >= x && mx < x + width && my >= y && my < y + height;
    }

    private void drawCenteredText(DrawContext ctx, String text, int cx, int ty, int color) {
        var tr = MinecraftClient.getInstance().textRenderer;
        ctx.drawTextWithShadow(tr, text, cx - tr.getWidth(text) / 2, ty, color);
    }

    public int      getX()        { return x; }
    public int      getY()        { return y; }
    public int      getWidth()    { return width; }
    public int      getHeight()   { return height; }
    public Category getCategory() { return category; }
}