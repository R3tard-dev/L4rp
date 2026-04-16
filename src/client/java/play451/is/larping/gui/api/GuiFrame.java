package play451.is.larping.gui.api;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import play451.is.larping.module.Category;
import play451.is.larping.module.Module;
import play451.is.larping.module.ModuleManager;

import java.util.ArrayList;
import java.util.List;

public class GuiFrame {
    private final Category category;
    private int x, y, width;
    private boolean dragging;
    private double dragOffX, dragOffY;
    private final List<Button> buttons = new ArrayList<>();

    private static final int LABEL_H = 10;

    public GuiFrame(Category category, int x, int y, int width) {
        this.category = category;
        this.x = x;
        this.y = y;
        this.width = width;
        buildButtons();
    }

    private void buildButtons() {
        buttons.clear();
        for (Module mod : ModuleManager.getModulesForCategory(category)) {
            buttons.add(new ModuleButton(mod, this, 0));
        }
    }

    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        var tr = MinecraftClient.getInstance().textRenderer;

        context.drawCenteredTextWithShadow(tr, category.getName(), x + width / 2, y + 2, 0xFFFFFFFF);

        int btnY = y + LABEL_H;
        for (Button btn : buttons) {
            btn.setX(x);
            btn.setY(btnY);
            btn.render(context, mouseX, mouseY, delta);
            btnY += btn.getHeight();
        }
    }

    public int getTotalHeight() {
        int h = LABEL_H;
        for (Button btn : buttons) h += btn.getHeight();
        return h;
    }

    public void mouseClicked(double mouseX, double mouseY, int button) {
        if (mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + LABEL_H) {
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

    public int getX() { return x; }
    public int getY() { return y; }
    public int getWidth() { return width; }
}