package play451.is.larping.gui.impl;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import play451.is.larping.gui.ClickGui;
import play451.is.larping.gui.api.Button;
import play451.is.larping.gui.api.Frame;
import play451.is.larping.module.setting.CategorySetting;

import java.awt.*;

public class CategoryButton extends Button {
    private final CategorySetting setting;

    public CategoryButton(CategorySetting setting, Frame parent, int height) {
        super(setting, parent, height, setting.getDescription());
        this.setting = setting;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        var   tr  = MinecraftClient.getInstance().textRenderer;
        Color accent = ClickGui.getHeaderColor(getY());
        int   ap  = (255 << 24) | (accent.getRed() << 16) | (accent.getGreen() << 8) | accent.getBlue();
        int   h   = getHeight();
        int   x1  = getX() + getPadding() + 1;
        int   x2  = getX() + getWidth() - getPadding() - 1;

        context.fill(x1, getY(), x2, getY() + h - 1, withAlpha(accent, 45));
        context.fill(x1, getY(), x1 + 1, getY() + h - 1, ap);

        String arrow = setting.isOpen() ? "\u25bc " : "\u25b6 ";
        context.drawTextWithShadow(tr, arrow + setting.getTag(),
                getX() + getTextPadding() + 2, getY() + (h - 8) / 2, ap);

        context.fill(x1, getY() + h - 1, x2, getY() + h, 0xFF050505);
    }

    @Override
    public void mouseClicked(double mouseX, double mouseY, int button) {
        if (isHovering(mouseX, mouseY) && button == 0) {
            setting.setOpen(!setting.isOpen());
        }
    }

    private int withAlpha(Color c, int a) {
        return (a << 24) | (c.getRed() << 16) | (c.getGreen() << 8) | c.getBlue();
    }
}