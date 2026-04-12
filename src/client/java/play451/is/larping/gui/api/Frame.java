package play451.is.larping.gui.api;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import play451.is.larping.module.Category;
import play451.is.larping.module.Module;
import play451.is.larping.module.ModuleManager;
import play451.is.larping.module.impl.core.ClickGuiModule;
import play451.is.larping.gui.ClickGui;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class Frame {
    private final Category category;
    private int x, y, width, height;
    private boolean dragging;
    private double dragOffX, dragOffY;
    private boolean expanded = true;
    private int scrollOffset = 0;
    private final List<Button> buttons = new ArrayList<>();

    public Frame(Category category, int x, int y, int width, int height) {
        this.category = category;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        buildButtons();
    }

    private void buildButtons() {
        buttons.clear();
        for (Module mod : ModuleManager.getModulesForCategory(category)) {
            buttons.add(new ModuleButton(mod, this, height));
        }
    }

    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        Color headerColor = ClickGui.getHeaderColor(y);
        Color headerDark  = new Color(
            Math.max(0, headerColor.getRed()   - 20),
            Math.max(0, headerColor.getGreen() - 20),
            Math.max(0, headerColor.getBlue()  - 20),
            headerColor.getAlpha()
        );

        fill(context, x, y, x + width, y + height, headerDark.getRGB());
        fill(context, x, y + height - 1, x + width, y + height, headerColor.getRGB());

        drawCenteredText(context, category.getName(), x + width / 2, y + (height - 8) / 2, 0xFFFFFFFF);

        if (!expanded) return;

        int buttonY = y + height;
        int idx = 0;
        for (Button btn : buttons) {
            if (idx < scrollOffset) { idx++; continue; }
            btn.setX(x);
            btn.setY(buttonY);
            btn.render(context, mouseX, mouseY, delta);
            buttonY += btn.getHeight();
            idx++;
        }

        fill(context, x, y, x + 1, y + height + getTotalButtonHeight(), pack(headerColor, 180));
        fill(context, x + width - 1, y, x + width, y + height + getTotalButtonHeight(), pack(headerColor, 180));
        fill(context, x, y + height + getTotalButtonHeight(), x + width, y + height + getTotalButtonHeight() + 1, pack(headerColor, 180));
    }

    private int getTotalButtonHeight() {
        int total = 0;
        int idx = 0;
        for (Button btn : buttons) {
            if (idx < scrollOffset) { idx++; continue; }
            total += btn.getHeight();
            idx++;
        }
        return total;
    }

    public void mouseClicked(double mouseX, double mouseY, int button) {
        if (isHoveringHeader(mouseX, mouseY)) {
            if (button == 0) {
                dragging  = true;
                dragOffX  = mouseX - x;
                dragOffY  = mouseY - y;
            } else if (button == 1) {
                expanded = !expanded;
            }
        }

        if (!expanded) return;
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
        if (!expanded) return;
        int totalH = height + getTotalButtonHeight();
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

    private void fill(DrawContext ctx, int x1, int y1, int x2, int y2, int color) {
        ctx.fill(x1, y1, x2, y2, color);
    }

    private int pack(Color c, int alpha) {
        return (alpha << 24) | (c.getRed() << 16) | (c.getGreen() << 8) | c.getBlue();
    }

    private void drawCenteredText(DrawContext ctx, String text, int cx, int ty, int color) {
        var tr = MinecraftClient.getInstance().textRenderer;
        ctx.drawTextWithShadow(tr, text, cx - tr.getWidth(text) / 2, ty, color);
    }

    public int getX()      { return x; }
    public int getY()      { return y; }
    public int getWidth()  { return width; }
    public int getHeight() { return height; }
    public Category getCategory() { return category; }
}