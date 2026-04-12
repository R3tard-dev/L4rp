package play451.is.larping.gui.api;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import play451.is.larping.gui.ClickGui;
import play451.is.larping.module.Module;

import java.awt.*;

public class ModuleButton extends Button {
    private final Module module;

    public ModuleButton(Module module, Frame parent, int height) {
        super(parent, height, "");
        this.module = module;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        boolean hovered = isHovering(mouseX, mouseY);
        boolean enabled = module.isEnabled();

        Color accent = ClickGui.getHeaderColor(y);
        int accentPacked = accent.getRGB();

        int bg;
        if (enabled) {
            bg = (accent.getAlpha() << 24) | ((Math.max(0, accent.getRed() - 30)) << 16)
               | ((Math.max(0, accent.getGreen() - 30)) << 8) | Math.max(0, accent.getBlue() - 30);
        } else if (hovered) {
            bg = 0xCC1C1C1C;
        } else {
            bg = 0xCC101010;
        }

        context.fill(x + padding, y, x + getWidth() - padding, y + height - 1, bg);

        if (enabled) {
            context.fill(x + padding, y, x + padding + 2, y + height - 1, accentPacked);
        }

        int textColor = enabled ? 0xFFFFFFFF : 0xFFAAAAAA;
        var tr = MinecraftClient.getInstance().textRenderer;
        context.drawTextWithShadow(tr, module.getName(), x + textPadding + (enabled ? 3 : 0), y + (height - 8) / 2, textColor);

        context.fill(x + padding, y + height - 1, x + getWidth() - padding, y + height, 0xFF080808);
    }

    @Override
    public void mouseClicked(double mouseX, double mouseY, int button) {
        if (isHovering(mouseX, mouseY) && button == 0) {
            module.toggle();
        }
    }
}