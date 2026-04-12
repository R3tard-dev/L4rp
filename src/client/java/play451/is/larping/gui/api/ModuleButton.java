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
    private final List<Button> settingButtons = new ArrayList<>();

    private static final int MODULE_H = 16;

    public ModuleButton(Module module, Frame parent, int rowHeight) {
        super(parent, MODULE_H, module.getDescription());
        this.module = module;
        buildSettingButtons();
    }

    private void buildSettingButtons() {
        settingButtons.clear();
        for (Setting<?> s : module.getSettings()) {
            if (!s.getVisibility().isVisible()) continue;
            if (s instanceof CategorySetting cs)   settingButtons.add(new CategoryButton(cs, getParent(), MODULE_H - 2));
            else if (s instanceof BooleanSetting bs) settingButtons.add(new BooleanButton(bs, getParent(), MODULE_H - 2));
            else if (s instanceof SliderSetting ss)  settingButtons.add(new SliderButton(ss, getParent(), MODULE_H - 2));
            else if (s instanceof ColorSetting cs2)  settingButtons.add(new ColorButton(cs2, getParent(), MODULE_H - 2));
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        var tr     = MinecraftClient.getInstance().textRenderer;
        Color accent = ClickGui.getHeaderColor(getY());
        boolean hovered = isHovering(mouseX, mouseY);
        boolean enabled = module.isEnabled();

        int innerX1 = getX() + getPadding();
        int innerX2 = getX() + getWidth() - getPadding();

        int bg;
        if (enabled) {
            bg = (180 << 24) | ((Math.max(0, accent.getRed() - 25)) << 16)
               | ((Math.max(0, accent.getGreen() - 25)) << 8) | Math.max(0, accent.getBlue() - 25);
        } else {
            bg = hovered ? 0xCC1C1C1C : 0xCC0E0E0E;
        }

        context.fill(innerX1, getY(), innerX2, getY() + MODULE_H - 1, bg);

        if (enabled) {
            context.fill(innerX1, getY(), innerX1 + 2, getY() + MODULE_H - 1,
                    (255 << 24) | (accent.getRed() << 16) | (accent.getGreen() << 8) | accent.getBlue());
        }

        boolean hasSets = !module.getSettings().isEmpty();
        String label = module.getName() + (hasSets ? (open ? " \u25be" : " \u25b8") : "");
        context.drawTextWithShadow(tr, label, getX() + getTextPadding() + (enabled ? 3 : 0),
                getY() + (MODULE_H - 8) / 2, enabled ? 0xFFFFFFFF : 0xFFAAAAAA);

        context.fill(innerX1, getY() + MODULE_H - 1, innerX2, getY() + MODULE_H, 0xFF060606);

        if (open) {
            buildSettingButtons();
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
            else if (button == 1 && !module.getSettings().isEmpty()) open = !open;
        }
        if (open) for (Button btn : settingButtons) btn.mouseClicked(mouseX, mouseY, button);
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
        return mouseX >= getX() + getPadding() && mouseX < getX() + getWidth() - getPadding()
            && mouseY >= getY() && mouseY < getY() + MODULE_H;
    }

    @Override
    public int getHeight() {
        if (!open) return MODULE_H;
        int total = MODULE_H;
        buildSettingButtons();
        for (Button btn : settingButtons) total += btn.getHeight();
        return total;
    }
}