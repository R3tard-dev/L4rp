package play451.is.larping.features.gui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public class ClickGui extends Screen {
    private static ClickGui INSTANCE;
    
    private int x = 100;
    private int y = 100;
    private int width = 400;
    private int height = 300;
    
    private boolean dragging = false;
    private int dragOffsetX = 0;
    private int dragOffsetY = 0;

    public ClickGui() {
        super(Text.literal("Click GUI"));
    }

    public static ClickGui getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ClickGui();
        }
        return INSTANCE;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        
        // Draw background
        context.fill(x, y, x + width, y + height, 0x90000000);
        
        // Draw title bar
        context.fill(x, y, x + width, y + 20, 0xFF2A2A2A);
        context.drawTextWithShadow(MinecraftClient.getInstance().textRenderer, "L4rp Menu", x + 5, y + 6, 0xFFFFFF);
        
        // Draw border
        context.fill(x, y, x + width, y + 1, 0xFF000000); // Top
        context.fill(x, y + height - 1, x + width, y + height, 0xFF000000); // Bottom
        context.fill(x, y, x + 1, y + height, 0xFF000000); // Left
        context.fill(x + width - 1, y, x + width, y + height, 0xFF000000); // Right

        // Example content
        context.drawText(MinecraftClient.getInstance().textRenderer, "Example Module", x + 10, y + 30, 0xFFFFFF, false);
        context.drawText(MinecraftClient.getInstance().textRenderer, "Press RSHIFT to close", x + 10, y + 50, 0xAAAAAA, false);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) { // Left click
            // Check if clicking title bar
            if (mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + 20) {
                dragging = true;
                dragOffsetX = (int) (mouseX - x);
                dragOffsetY = (int) (mouseY - y);
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0) {
            dragging = false;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (dragging) {
            x = (int) (mouseX - dragOffsetX);
            y = (int) (mouseY - dragOffsetY);
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean shouldPause() {
        return false; // Don't pause the game when GUI is open
    }
}