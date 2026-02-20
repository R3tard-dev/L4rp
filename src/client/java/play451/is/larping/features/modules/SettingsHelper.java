package play451.is.larping.features.modules;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

public class SettingsHelper {
    
    TextRenderer textRenderer;
    
    
    private static final int ACCENT        = 0xFF1E90FF;
    private static final int TEXT_PRIMARY   = 0xFFF0F0F0;
    private static final int TEXT_SECONDARY = 0xFF999999;
    private static final int TEXT_DIM       = 0xFF666666;
    private static final int BUTTON_BG      = 0xFF0A0A0A;
    private static final int BUTTON_HOVER   = 0xFF101010;
    private static final int ENABLED_GLOW   = 0xFF10B981;
    private static final int BORDER_SUBTLE  = 0xFF151515;
    private static final int BIND_LISTENING = 0xFF2A0808;
    private static final int BIND_SET       = 0xFF0D2035;
    
    public SettingsHelper(TextRenderer textRenderer) {
        this.textRenderer = textRenderer;
    }
    
    
    
    public void renderLabel(DrawContext context, String label, String value, int x, int y) {
        context.drawText(textRenderer, label, x, y, TEXT_PRIMARY, false);

        if (value != null) {
            int sliderStartX = x + 110;
            int valueWidth = textRenderer.getWidth(value);
            int spacing = 6;

            int valueX = sliderStartX - valueWidth - spacing;
            context.drawText(textRenderer, value, valueX, y, TEXT_SECONDARY, false);
        }
    }
    
    
    
    public void renderSlider(DrawContext context, int x, int y, int width, double value, double min, double max) {
        int trackH = 4;
        int trackY = y - trackH / 2;
        
        
        context.fill(x, trackY, x + width, trackY + trackH, 0xFF1A1A1A);
        
        context.fill(x, trackY, x + 1, trackY + trackH, BORDER_SUBTLE);
        
        
        double percent = Math.max(0, Math.min(1, (value - min) / (max - min)));
        int fillWidth = (int) (width * percent);
        if (fillWidth > 0) {
            context.fill(x, trackY, x + fillWidth, trackY + trackH, ACCENT);
        }
        
        
        int handleX = x + fillWidth - 2;
        int handleW = 4;
        int handleH = 12;
        int handleY = y - handleH / 2;
        
        handleX = Math.max(x, Math.min(x + width - handleW, handleX));
        context.fill(handleX, handleY, handleX + handleW, handleY + handleH, TEXT_PRIMARY);
        
        context.fill(handleX + 1, handleY, handleX + 2, handleY + handleH, ACCENT);
    }
    
    
    
    public void renderToggle(DrawContext context, int mouseX, int mouseY, int x, int y, boolean enabled) {
        int w = 36;
        int h = 16;
        
        boolean isHovered = mouseX >= x && mouseX <= x + w &&
                            mouseY >= y && mouseY <= y + h;
        
        
        int bgColor = enabled ? ENABLED_GLOW : (isHovered ? BUTTON_HOVER : BUTTON_BG);
        context.fill(x, y, x + w, y + h, bgColor);
        
        
        context.fill(x, y, x + 2, y + h, enabled ? 0xFF0D8060 : BORDER_SUBTLE);
        
        
        int knobW = 12;
        int knobH = h - 4;
        int knobX = enabled ? x + w - knobW - 2 : x + 2;
        int knobY = y + 2;
        context.fill(knobX, knobY, knobX + knobW, knobY + knobH, TEXT_PRIMARY);
    }
    
    
    
    public void renderModeSelector(DrawContext context, int mouseX, int mouseY, int x, int y,
                                   String[] options, String selected, int buttonWidth) {
        int btnH = 18;
        int currentX = x;
        for (String option : options) {
            boolean isSelected = option.equals(selected);
            boolean isHovered  = !isSelected &&
                                  mouseX >= currentX && mouseX <= currentX + buttonWidth &&
                                  mouseY >= y - 2 && mouseY <= y - 2 + btnH;
            
            
            int bgColor = isSelected ? ACCENT : (isHovered ? BUTTON_HOVER : BUTTON_BG);
            context.fill(currentX, y - 2, currentX + buttonWidth, y - 2 + btnH, bgColor);
            
            
            if (isSelected) {
                context.fill(currentX, y - 2 + btnH - 2, currentX + buttonWidth, y - 2 + btnH, 0xFF1060AA);
            }
            
            
            int textOffset = (buttonWidth - textRenderer.getWidth(option)) / 2;
            context.drawText(textRenderer, option, currentX + textOffset, y + 3, TEXT_PRIMARY, false);
            
            
            currentX += buttonWidth + 3;
        }
    }
    
    
    
    public void renderBind(DrawContext context, int mouseX, int mouseY, int x, int y,
                           int keyCode, boolean isListening) {
        int btnW = 100;
        int btnH = 18;
        
        boolean isHovered = !isListening && mouseX >= x && mouseX <= x + btnW &&
                            mouseY >= y && mouseY <= y + btnH;
        
        String label;
        int bgColor;
        int accentBar;
        
        if (isListening) {
            bgColor   = BIND_LISTENING;
            accentBar = 0xFFCC2222;
            label     = "Press a key...";
        } else if (keyCode != -1) {
            bgColor   = isHovered ? 0xFF1A3A5A : BIND_SET;
            accentBar = ACCENT;
            label     = getKeyName(keyCode);
        } else {
            bgColor   = isHovered ? BUTTON_HOVER : BUTTON_BG;
            accentBar = BORDER_SUBTLE;
            label     = "None";
        }
        
        
        context.fill(x, y, x + btnW, y + btnH, bgColor);
        
        context.fill(x, y, x + 2, y + btnH, accentBar);
        
        
        int textW = textRenderer.getWidth(label);
        context.drawText(textRenderer, label, x + (btnW - textW) / 2, y + 5, TEXT_PRIMARY, false);
    }
    
    
    
    public void renderInfo(DrawContext context, String text, int x, int y) {
        context.drawText(textRenderer, text, x, y, TEXT_DIM, false);
    }
    
    
    
    public boolean isSliderHovered(double mouseX, double mouseY, int sliderX, int sliderY, int width) {
        return mouseX >= sliderX && mouseX <= sliderX + width &&
               mouseY >= sliderY - 10 && mouseY <= sliderY + 10;
    }
    
    public boolean isToggleHovered(double mouseX, double mouseY, int x, int y) {
        return mouseX >= x && mouseX <= x + 36 &&
               mouseY >= y && mouseY <= y + 16;
    }
    
    public boolean isModeButtonHovered(double mouseX, double mouseY, int x, int y, int width) {
        return mouseX >= x && mouseX <= x + width &&
               mouseY >= y - 2 && mouseY <= y + 16;
    }
    
    public boolean isBindHovered(double mouseX, double mouseY, int x, int y) {
        return mouseX >= x && mouseX <= x + 100 &&
               mouseY >= y && mouseY <= y + 18;
    }
    
    
    
    public double calculateSliderValue(double mouseX, int sliderX, int width, double min, double max) {
        double percent = Math.max(0, Math.min(1, (mouseX - sliderX) / width));
        return min + (percent * (max - min));
    }
    
    
    
    public static String getKeyName(int keyCode) {
        return switch (keyCode) {
            case -1  -> "None";
            case 32  -> "Space";
            case 256 -> "Escape";
            case 257 -> "Enter";
            case 258 -> "Tab";
            case 259 -> "Backspace";
            case 261 -> "Delete";
            case 262 -> "Right";
            case 263 -> "Left";
            case 264 -> "Down";
            case 265 -> "Up";
            case 290 -> "F1";
            case 291 -> "F2";
            case 292 -> "F3";
            case 293 -> "F4";
            case 294 -> "F5";
            case 295 -> "F6";
            case 296 -> "F7";
            case 297 -> "F8";
            case 298 -> "F9";
            case 299 -> "F10";
            case 300 -> "F11";
            case 301 -> "F12";
            case 340 -> "LShift";
            case 341 -> "LCtrl";
            case 342 -> "LAlt";
            case 344 -> "RShift";
            case 345 -> "RCtrl";
            case 346 -> "RAlt";
            default  -> {
                if (keyCode >= 65 && keyCode <= 90)   yield String.valueOf((char) keyCode);
                if (keyCode >= 48 && keyCode <= 57)   yield String.valueOf((char) keyCode);
                if (keyCode >= 320 && keyCode <= 329) yield "Num" + (keyCode - 320);
                yield "Key" + keyCode;
            }
        };
    }
    
    
    
    public int getAccentColor()       { return ACCENT; }
    public int getTextPrimaryColor()  { return TEXT_PRIMARY; }
    public int getTextSecondaryColor(){ return TEXT_SECONDARY; }
    public int getTextDimColor()      { return TEXT_DIM; }
    public int getButtonBgColor()     { return BUTTON_BG; }
    public int getButtonHoverColor()  { return BUTTON_HOVER; }
    public int getEnabledGlowColor()  { return ENABLED_GLOW; }
    
    
    
    public void drawRoundedRect(DrawContext context, int x, int y, int width, int height, int color) {
        context.fill(x, y, x + width, y + height, color);
    }
}