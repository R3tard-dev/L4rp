package play451.is.larping.gui.impl;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import play451.is.larping.gui.ClickGui;
import play451.is.larping.gui.api.Button;
import play451.is.larping.gui.api.Frame;
import play451.is.larping.module.setting.ColorSetting;

import java.awt.*;

public class ColorButton extends Button {
    private final ColorSetting setting;
    private boolean open = false;
    private int dragChannel = -1;

    private static final String[] LABELS = {"R", "G", "B", "A"};
    private static final int[] LABEL_COLORS = {0xFFCC4444, 0xFF44CC44, 0xFF4488CC, 0xFFAAAAAA};
    private static final int CHANNEL_H = 9;

    public ColorButton(ColorSetting setting, Frame parent, int height) {
        super(setting, parent, height, setting.getDescription());
        this.setting = setting;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        var tr = MinecraftClient.getInstance().textRenderer;
        Color accent = ClickGui.getHeaderColor(getY());
        int baseH = super.getHeight();

        int innerX1 = getX() + getPadding() + 1;
        int innerX2 = getX() + getWidth() - getPadding() - 1;

        context.fill(innerX1, getY(), innerX2, getY() + baseH - 1, 0xAA0D0D0D);
        context.fill(innerX1, getY(), innerX1 + 1, getY() + baseH - 1,
                (255 << 24) | (accent.getRed() << 16) | (accent.getGreen() << 8) | accent.getBlue());

        context.drawTextWithShadow(tr, setting.getTag(),
                getX() + getTextPadding() + 2, getY() + (baseH - 8) / 2, 0xFFCCCCCC);

        int previewColor = setting.getPacked();
        context.fill(innerX2 - 10, getY() + 2, innerX2 - 2, getY() + baseH - 3, previewColor | 0xFF000000);
        context.fill(innerX2 - 10, getY() + 2, innerX2 - 2, getY() + baseH - 3,
                (setting.getA() << 24) | (setting.getR() << 16) | (setting.getG() << 8) | setting.getB());

        context.fill(innerX1, getY() + baseH - 1, innerX2, getY() + baseH, 0xFF060606);

        if (open) {
            int cy = getY() + baseH;
            int[] channels = {setting.getR(), setting.getG(), setting.getB(), setting.getA()};
            int sliderX1 = innerX1 + 10;
            int sliderW  = innerX2 - sliderX1 - 22;

            for (int i = 0; i < 4; i++) {
                context.fill(innerX1, cy, innerX2, cy + CHANNEL_H - 1, 0xAA111111);
                context.drawTextWithShadow(tr, LABELS[i], innerX1 + 2, cy + 1, LABEL_COLORS[i]);

                context.fill(sliderX1, cy + 2, sliderX1 + sliderW, cy + CHANNEL_H - 2, 0xFF1A1A1A);
                int fillW = (int)((channels[i] / 255.0) * sliderW);
                context.fill(sliderX1, cy + 2, sliderX1 + fillW, cy + CHANNEL_H - 2, LABEL_COLORS[i]);

                String valStr = String.valueOf(channels[i]);
                context.drawTextWithShadow(tr, valStr, innerX2 - 2 - tr.getWidth(valStr), cy + 1, 0xFFCCCCCC);

                context.fill(innerX1, cy + CHANNEL_H - 1, innerX2, cy + CHANNEL_H, 0xFF060606);
                cy += CHANNEL_H;
            }
        }
    }

    @Override
    public void mouseClicked(double mouseX, double mouseY, int button) {
        int baseH   = super.getHeight();
        int innerX1 = getX() + getPadding() + 1;
        int innerX2 = getX() + getWidth() - getPadding() - 1;

        if (isHovering(mouseX, mouseY) && button == 0) {
            open = !open;
            return;
        }

        if (open) {
            int sliderX1 = innerX1 + 10;
            int sliderW  = innerX2 - sliderX1 - 22;
            int cy = getY() + baseH;
            for (int i = 0; i < 4; i++) {
                if (mouseX >= sliderX1 && mouseX < sliderX1 + sliderW
                 && mouseY >= cy + 2 && mouseY < cy + CHANNEL_H - 2) {
                    dragChannel = i;
                    applyChannel(mouseX, sliderX1, sliderW);
                    return;
                }
                cy += CHANNEL_H;
            }
        }
    }

    @Override
    public void mouseReleased(double mouseX, double mouseY, int button) {
        dragChannel = -1;
    }

    @Override
    public void mouseDragged(double mouseX, double mouseY, int button, double dX, double dY) {
        if (dragChannel < 0 || !open) return;
        int innerX1 = getX() + getPadding() + 1;
        int sliderX1 = innerX1 + 10;
        int innerX2  = getX() + getWidth() - getPadding() - 1;
        int sliderW  = innerX2 - sliderX1 - 22;
        applyChannel(mouseX, sliderX1, sliderW);
    }

    private void applyChannel(double mouseX, int sliderX, int sliderW) {
        int val = (int) Math.max(0, Math.min(255, ((mouseX - sliderX) / sliderW) * 255));
        switch (dragChannel) {
            case 0 -> setting.setR(val);
            case 1 -> setting.setG(val);
            case 2 -> setting.setB(val);
            case 3 -> setting.setA(val);
        }
    }

    @Override
    public int getHeight() {
        return super.getHeight() + (open ? 4 * CHANNEL_H : 0);
    }
}