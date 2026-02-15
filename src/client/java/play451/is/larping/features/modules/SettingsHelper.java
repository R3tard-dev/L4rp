package play451.is.larping.features.modules;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

 
public class SettingsHelper {
    
    private final TextRenderer textRenderer;
    
     
    private static final int ACCENT = 0xFF1E90FF;
    private static final int TEXT_PRIMARY = 0xFFF0F0F0;
    private static final int TEXT_SECONDARY = 0xFF999999;
    private static final int TEXT_DIM = 0xFF666666;
    private static final int BUTTON_BG = 0xFF1A1A1A;
    private static final int BUTTON_HOVER = 0xFF252525;
    private static final int ENABLED_GLOW = 0xFF10B981;
    
    public SettingsHelper(TextRenderer textRenderer) {
        this.textRenderer = textRenderer;
    }
    
     
    public void renderLabel(DrawContext context, String label, String value, int x, int y) {
        context.drawText(textRenderer, label, x, y, TEXT_PRIMARY, false);
        if (value != null) {
            context.drawText(textRenderer, value, x + 130, y, TEXT_SECONDARY, false);
        }
    }
    
     
    public void renderSlider(DrawContext context, int x, int y, int width, double value, double min, double max) {
         
        drawRoundedRect(context, x, y - 2, width, 4, 0xFF2A2A2A);
        
         
        double percent = (value - min) / (max - min);
        int fillWidth = (int) (width * percent);
        drawRoundedRect(context, x, y - 2, fillWidth, 4, ACCENT);
        
         
        int handleX = x + fillWidth - 4;
        drawRoundedRect(context, handleX, y - 6, 8, 12, TEXT_PRIMARY);
    }
    
     
    public void renderToggle(DrawContext context, int mouseX, int mouseY, int x, int y, boolean enabled) {
        int width = 42;
        int height = 20;
        
        boolean isHovered = mouseX >= x && mouseX <= x + width &&
                           mouseY >= y && mouseY <= y + height;
        
         
        int bgColor = enabled ? ENABLED_GLOW : BUTTON_BG;
        if (isHovered && !enabled) bgColor = BUTTON_HOVER;
        drawRoundedRect(context, x, y, width, height, bgColor);
        
         
        int circleX = enabled ? x + width - 18 : x + 2;
        drawRoundedRect(context, circleX, y + 2, 16, 16, TEXT_PRIMARY);
    }
    
     
    public void renderModeSelector(DrawContext context, int mouseX, int mouseY, int x, int y, 
                                   String[] options, String selected, int buttonWidth) {
        int currentX = x;
        for (String option : options) {
            boolean isSelected = option.equals(selected);
            boolean isHovered = mouseX >= currentX && mouseX <= currentX + buttonWidth &&
                               mouseY >= y - 5 && mouseY <= y + 15;
            
            int bgColor = isSelected ? ACCENT : (isHovered ? BUTTON_HOVER : BUTTON_BG);
            drawRoundedRect(context, currentX, y - 5, buttonWidth, 20, bgColor);
            
            int textOffset = (buttonWidth - textRenderer.getWidth(option)) / 2;
            context.drawText(textRenderer, option, currentX + textOffset, y, TEXT_PRIMARY, false);
            
            currentX += buttonWidth + 5;
        }
    }
    
     
    public void renderInfo(DrawContext context, String text, int x, int y) {
        context.drawText(textRenderer, text, x, y, TEXT_DIM, false);
    }
    
     
    public boolean isSliderHovered(double mouseX, double mouseY, int sliderX, int sliderY, int width) {
        return mouseX >= sliderX && mouseX <= sliderX + width &&
               mouseY >= sliderY - 10 && mouseY <= sliderY + 10;
    }
    
     
    public boolean isToggleHovered(double mouseX, double mouseY, int x, int y) {
        return mouseX >= x && mouseX <= x + 42 &&
               mouseY >= y && mouseY <= y + 20;
    }
    
     
    public boolean isModeButtonHovered(double mouseX, double mouseY, int x, int y, int width) {
        return mouseX >= x && mouseX <= x + width &&
               mouseY >= y - 5 && mouseY <= y + 15;
    }
    
     
    public double calculateSliderValue(double mouseX, int sliderX, int width, double min, double max) {
        double percent = Math.max(0, Math.min(1, (mouseX - sliderX) / width));
        return min + (percent * (max - min));
    }
    
     
    public void drawRoundedRect(DrawContext context, int x, int y, int width, int height, int color) {
        int radius = 6;
        
         
        context.fill(x + radius, y, x + width - radius, y + height, color);
        context.fill(x, y + radius, x + width, y + height - radius, color);
        
         
        drawSmoothCorner(context, x + radius, y + radius, radius, color, 0);
        drawSmoothCorner(context, x + width - radius, y + radius, radius, color, 1);
        drawSmoothCorner(context, x + radius, y + height - radius, radius, color, 2);
        drawSmoothCorner(context, x + width - radius, y + height - radius, radius, color, 3);
    }
    
    private void drawSmoothCorner(DrawContext context, int centerX, int centerY, int radius, int color, int corner) {
        int alpha = (color >> 24) & 0xFF;
        int rgb = color & 0x00FFFFFF;
        
        for (int i = -radius; i <= radius; i++) {
            for (int j = -radius; j <= radius; j++) {
                double distance = Math.sqrt(i * i + j * j);
                
                if (distance <= radius) {
                    double edgeDistance = radius - distance;
                    int pixelAlpha = alpha;
                    
                    if (edgeDistance < 1.0) {
                        pixelAlpha = (int) (alpha * edgeDistance);
                    }
                    
                    int smoothColor = (pixelAlpha << 24) | rgb;
                    
                    int x = centerX;
                    int y = centerY;
                    
                    boolean draw = false;
                    switch (corner) {
                        case 0: if (i <= 0 && j <= 0) { x += i; y += j; draw = true; } break;
                        case 1: if (i >= 0 && j <= 0) { x += i; y += j; draw = true; } break;
                        case 2: if (i <= 0 && j >= 0) { x += i; y += j; draw = true; } break;
                        case 3: if (i >= 0 && j >= 0) { x += i; y += j; draw = true; } break;
                    }
                    
                    if (draw) {
                        context.fill(x, y, x + 1, y + 1, smoothColor);
                    }
                }
            }
        }
    }
    
     
    public int getAccentColor() { return ACCENT; }
    public int getTextPrimaryColor() { return TEXT_PRIMARY; }
    public int getTextSecondaryColor() { return TEXT_SECONDARY; }
    public int getTextDimColor() { return TEXT_DIM; }
    public int getButtonBgColor() { return BUTTON_BG; }
    public int getButtonHoverColor() { return BUTTON_HOVER; }
    public int getEnabledGlowColor() { return ENABLED_GLOW; }
}