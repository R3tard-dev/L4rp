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
        var tr = MinecraftClient.getInstance().textRenderer;
        Color accent = ClickGui.getHeaderColor(getY());

        int innerX1 = getX() + getPadding() + 1;
        int innerX2 = getX() + getWidth() - getPadding() - 1;
        int alpha = (255 << 24) | (accent.getRed() << 16) | (accent.getGreen() << 8) | accent.getBlue();

        context.fill(innerX1, getY(), innerX2, getY() + getHeight() - 1,
                (60 << 24) | (accent.getRed() << 16) | (accent.getGreen() << 8) | accent.getBlue());

        context.drawTextWithShadow(tr, (setting.isOpen() ? "\u25bc " : "\u25b6 ") + setting.getTag(),
                getX() + getTextPadding() + 2, getY() + (getHeight() - 8) / 2, alpha);

        context.fill(innerX1, getY() + getHeight() - 1, innerX2, getY() + getHeight(), 0xFF060606);
    }

    @Override
    public void mouseClicked(double mouseX, double mouseY, int button) {
        if (isHovering(mouseX, mouseY) && button == 0) {
            setting.setOpen(!setting.isOpen());
        }
    }
}