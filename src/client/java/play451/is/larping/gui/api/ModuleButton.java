package play451.is.larping.gui.api;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import play451.is.larping.gui.ClickGui;
import play451.is.larping.gui.impl.BooleanButton;
import play451.is.larping.gui.impl.CategoryButton;
import play451.is.larping.gui.impl.ColorButton;
import play451.is.larping.gui.impl.SliderButton;
import play451.is.larping.module.Module;
import play451.is.larping.module.setting.*;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class ModuleButton extends Button {
    private final Module module;
    private boolean open = false;
    private boolean dirty = true;
    private final List<Button> settingButtons = new ArrayList<>();

    public static final int MODULE_H = 14;
    public static final int SETTING_H = 12;

    public ModuleButton(Module module, GuiFrame parent, int rowHeight) {
        super(parent, MODULE_H, module.getDescription());
        this.module = module;
    }

    private void rebuildIfDirty() {
        if (!dirty) return;
        dirty = false;
        settingButtons.clear();
        for (Setting<?> s : module.getSettings()) {
            s.getVisibility().update();
            if (!s.getVisibility().isVisible()) continue;
            if (s instanceof CategorySetting cs) settingButtons.add(new CategoryButton(cs, getParent(), SETTING_H));
            else if (s instanceof BooleanSetting bs) settingButtons.add(new BooleanButton(bs, getParent(), SETTING_H));
            else if (s instanceof SliderSetting ss) settingButtons.add(new SliderButton(ss, getParent(), SETTING_H));
            else if (s instanceof ColorSetting cs2) settingButtons.add(new ColorButton(cs2, getParent(), SETTING_H));
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        rebuildIfDirty();

        var tr = MinecraftClient.getInstance().textRenderer;
        Color accent = ClickGui.getHeaderColor(getY());

        boolean hov = isHovering(mouseX, mouseY);
        boolean on = module.isEnabled();

        int x1 = getX() + getPadding();
        int x2 = getX() + getWidth() - getPadding();

        int bg;
        if (on) {
            int r = accent.getRed();
            int g = accent.getGreen();
            int b = accent.getBlue();
            bg = (90 << 24) | (r << 16) | (g << 8) | b;
        } else {
            bg = hov ? 0x661E1E1E : 0x55111111;
        }

        context.fill(x1, getY(), x2, getY() + MODULE_H, bg);

        boolean hasSets = !module.getSettings().isEmpty();
        String label = module.getName() + (hasSets ? (open ? " -" : " +") : "");

        context.drawTextWithShadow(tr, label, getX() + 4, getY() + 3, on ? 0xFFFFFFFF : 0xFFAAAAAA);

        if (open) {
            int sy = getY() + MODULE_H;
            for (Button btn : settingButtons) {
                btn.setX(getX());
                btn.setY(sy);
                btn.render(context, mouseX, mouseY, delta);
                sy += btn.getHeight();
            }
        }
    }

    @Override
    public void mouseClicked(double mouseX, double mouseY, int button) {
        if (isHovering(mouseX, mouseY)) {
            if (button == 0) module.toggle();
            else if (button == 1 && !module.getSettings().isEmpty()) {
                open = !open;
                dirty = true;
            }
            return;
        }
        if (open) {
            for (Button btn : settingButtons) btn.mouseClicked(mouseX, mouseY, button);
            dirty = true;
        }
    }

    @Override
    public void mouseReleased(double mouseX, double mouseY, int button) {
        if (open) for (Button btn : settingButtons) btn.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public void mouseDragged(double mouseX, double mouseY, int button, double dX, double dY) {
        if (open) for (Button btn : settingButtons) btn.mouseDragged(mouseX, mouseY, button, dX, dY);
    }

    @Override
    public void keyPressed(int keyCode, int scanCode, int modifiers) {
        if (open) for (Button btn : settingButtons) btn.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void charTyped(char chr, int modifiers) {
        if (open) for (Button btn : settingButtons) btn.charTyped(chr, modifiers);
    }

    @Override
    public boolean isHovering(double mouseX, double mouseY) {
        return mouseX >= getX() + getPadding()
                && mouseX < getX() + getWidth() - getPadding()
                && mouseY >= getY()
                && mouseY < getY() + MODULE_H;
    }

    @Override
    public int getHeight() {
        int total = MODULE_H;
        if (open) {
            rebuildIfDirty();
            for (Button btn : settingButtons) total += btn.getHeight();
        }
        return total;
    }
}