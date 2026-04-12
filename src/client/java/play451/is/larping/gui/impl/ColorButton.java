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

    private static final String[] LABELS      = {"R", "G", "B", "A"};
    private static final int[]    LABEL_COLS  = {0xFFCC4444, 0xFF44CC44, 0xFF4488CC, 0xFFAAAAAA};
    private static final int      CHANNEL_H   = 14;

    public ColorButton(ColorSetting setting, Frame parent, int height) {
        super(setting, parent, height, setting.getDescription());
        this.setting = setting;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        var   tr     = MinecraftClient.getInstance().textRenderer;
        Color accent = ClickGui.getHeaderColor(getY());
        int   ap     = (255 << 24) | (accent.getRed() << 16) | (accent.getGreen() << 8) | accent.getBlue();
        int   baseH  = super.getHeight();
        int   x1     = getX() + getPadding() + 1;
        int   x2     = getX() + getWidth() - getPadding() - 1;

        context.fill(x1, getY(), x2, getY() + baseH - 1, 0xAA0C0C0C);
        context.fill(x1, getY(), x1 + 1, getY() + baseH - 1, ap);

        context.drawTextWithShadow(tr, setting.getTag(),
                getX() + getTextPadding() + 2, getY() + (baseH - 8) / 2, 0xFFCCCCCC);

        int px = x2 - 12;
        int py = getY() + 2;
        context.fill(px, py, x2 - 2, getY() + baseH - 3, 0xFF333333);
        context.fill(px, py, x2 - 2, getY() + baseH - 3,
                (setting.getA() << 24) | (setting.getR() << 16) | (setting.getG() << 8) | setting.getB());

        String arrow = open ? "\u25be" : "\u25b8";
        context.drawTextWithShadow(tr, arrow, px - 4 - tr.getWidth(arrow),
                getY() + (baseH - 8) / 2, 0xFF888888);

        context.fill(x1, getY() + baseH - 1, x2, getY() + baseH, 0xFF050505);

        if (open) {
            int    cy      = getY() + baseH;
            int    sliderX = x1 + 10 + 3;
            int    sliderW = x2 - sliderX - 22;
            int[]  chs     = {setting.getR(), setting.getG(), setting.getB(), setting.getA()};

            for (int i = 0; i < 4; i++) {
                context.fill(x1, cy, x2, cy + CHANNEL_H - 1, 0xAA111111);
                context.fill(x1, cy, x1 + 1, cy + CHANNEL_H - 1, LABEL_COLS[i]);
                context.drawTextWithShadow(tr, LABELS[i], x1 + 3, cy + (CHANNEL_H - 8) / 2, LABEL_COLS[i]);

                context.fill(sliderX, cy + 3, sliderX + sliderW, cy + CHANNEL_H - 3, 0xFF1A1A1A);
                int fillW  = (int)((chs[i] / 255.0) * sliderW);
                context.fill(sliderX, cy + 3, sliderX + fillW, cy + CHANNEL_H - 3, LABEL_COLS[i]);
                int thumbX = sliderX + fillW;
                context.fill(thumbX - 1, cy + 2, thumbX + 1, cy + CHANNEL_H - 2, 0xFFFFFFFF);

                String vs = String.valueOf(chs[i]);
                context.drawTextWithShadow(tr, vs, x2 - 2 - tr.getWidth(vs),
                        cy + (CHANNEL_H - 8) / 2, 0xFFCCCCCC);

                context.fill(x1, cy + CHANNEL_H - 1, x2, cy + CHANNEL_H, 0xFF050505);
                cy += CHANNEL_H;
            }
        }
    }

    @Override
    public void mouseClicked(double mouseX, double mouseY, int button) {
        int x1     = getX() + getPadding() + 1;
        int x2     = getX() + getWidth() - getPadding() - 1;
        int baseH  = super.getHeight();

        if (isHovering(mouseX, mouseY) && button == 0) {
            open = !open;
            return;
        }
        if (!open) return;

        int sliderX = x1 + 10 + 3;
        int sliderW = x2 - sliderX - 22;
        int cy      = getY() + baseH;
        for (int i = 0; i < 4; i++) {
            if (mouseX >= sliderX && mouseX < sliderX + sliderW
             && mouseY >= cy + 3  && mouseY < cy + CHANNEL_H - 3) {
                dragChannel = i;
                applyChannel(mouseX, sliderX, sliderW);
                return;
            }
            cy += CHANNEL_H;
        }
    }

    @Override
    public void mouseReleased(double mouseX, double mouseY, int button) {
        dragChannel = -1;
    }

    @Override
    public void mouseDragged(double mouseX, double mouseY, int button, double dX, double dY) {
        if (dragChannel < 0 || !open) return;
        int x1      = getX() + getPadding() + 1;
        int x2      = getX() + getWidth() - getPadding() - 1;
        int sliderX = x1 + 10 + 3;
        int sliderW = x2 - sliderX - 22;
        applyChannel(mouseX, sliderX, sliderW);
    }

    private void applyChannel(double mx, int sx, int sw) {
        int val = (int) Math.max(0, Math.min(255, ((mx - sx) / sw) * 255));
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