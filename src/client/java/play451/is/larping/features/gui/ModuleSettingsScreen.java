package play451.is.larping.features.gui;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import play451.is.larping.config.Config;
import play451.is.larping.features.modules.Module;
import play451.is.larping.features.modules.combat.TriggerBot;

public class ModuleSettingsScreen extends Screen {
    private final Screen parent;
    private final Module module;
    
    private int x;
    private int y;
    private int width = 360;
    private int height = 280;
    
     
    private boolean dragging = false;
    private int dragOffsetX = 0;
    private int dragOffsetY = 0;
    
     
    private static final int BG_DARK = 0xF0050505;
    private static final int SIDEBAR_BG = 0xF0080808;
    private static final int ACCENT = 0xFF1E90FF;  
    private static final int ACCENT_DARK = 0xFF1873CC;
    private static final int TEXT_PRIMARY = 0xFFF0F0F0;
    private static final int TEXT_SECONDARY = 0xFF999999;
    private static final int TEXT_DIM = 0xFF666666;
    private static final int BUTTON_BG = 0xFF1A1A1A;
    private static final int BUTTON_HOVER = 0xFF252525;
    private static final int ENABLED_GLOW = 0xFF10B981;
    private static final int BORDER_SUBTLE = 0xFF151515;
    
    public ModuleSettingsScreen(Screen parent, Module module) {
        super(Text.literal("Module Settings"));
        this.parent = parent;
        this.module = module;
    }
    
    @Override
    protected void init() {
        super.init();
        this.x = (this.width - 360) / 2;
        this.y = (this.height - 280) / 2;
    }
    
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
         
        context.fillGradient(0, 0, this.width, this.height, 0xB0000000, 0xB0000000);
        
         
        drawGlow(context, x, y, width, height);
        
         
        drawRoundedRect(context, x, y, width, height, BG_DARK);
        
         
        context.fill(x, y, x + width, y + 30, 0xFF000000);
        context.drawTextWithShadow(this.textRenderer, module.getName() + " Settings", 
            x + 10, y + 10, TEXT_PRIMARY);
        
         
        int closeX = x + width - 25;
        int closeY = y + 8;
        boolean closeHovered = mouseX >= closeX && mouseX <= closeX + 15 &&
                              mouseY >= closeY && mouseY <= closeY + 15;
        context.drawText(this.textRenderer, "X", closeX + 4, closeY + 3, 
            closeHovered ? 0xFFFF5555 : TEXT_PRIMARY, false);
        
         
        int contentY = y + 45;
        
         
        if (module instanceof TriggerBot) {
            renderTriggerBotSettings(context, mouseX, mouseY, contentY);
        } else {
             
            context.drawText(this.textRenderer, "No settings available for this module", 
                x + width / 2 - 80, contentY + 50, TEXT_SECONDARY, false);
        }
        
         
        context.fill(x, y + height - 40, x + width, y + height - 39, BORDER_SUBTLE);
        
         
        int backX = x + 10;
        int backY = y + height - 35;
        int backWidth = 60;
        int backHeight = 25;
        boolean backHovered = mouseX >= backX && mouseX <= backX + backWidth &&
                             mouseY >= backY && mouseY <= backY + backHeight;
        
        drawRoundedRect(context, backX, backY, backWidth, backHeight, 
            backHovered ? BUTTON_HOVER : BUTTON_BG);
        context.drawText(this.textRenderer, "Back", backX + 18, backY + 8, TEXT_PRIMARY, false);
    }
    
    private void renderTriggerBotSettings(DrawContext context, int mouseX, int mouseY, int startY) {
        TriggerBot trigger = (TriggerBot) module;
        int settingY = startY;
        
         
        context.drawText(this.textRenderer, "Mode:", x + 20, settingY, TEXT_PRIMARY, false);
        String modeValue = trigger.getMode();
        
         
        int modeX = x + 150;
        for (String mode : new String[]{"1.8", "1.9"}) {
            boolean isSelected = mode.equals(modeValue);
            boolean isHovered = mouseX >= modeX && mouseX <= modeX + 40 &&
                               mouseY >= settingY - 5 && mouseY <= settingY + 15;
            
            int bgColor = isSelected ? ACCENT : (isHovered ? BUTTON_HOVER : BUTTON_BG);
            drawRoundedRect(context, modeX, settingY - 5, 40, 20, bgColor);
            context.drawText(this.textRenderer, mode, modeX + 10, settingY, TEXT_PRIMARY, false);
            modeX += 45;
        }
        settingY += 35;
        
         
        if (modeValue.equals("1.8")) {
            context.drawText(this.textRenderer, "CPS:", x + 20, settingY, TEXT_PRIMARY, false);
            context.drawText(this.textRenderer, String.format("%.1f", trigger.getCPS()), 
                x + 150, settingY, TEXT_SECONDARY, false);
            
             
            int sliderX = x + 200;
            int sliderY = settingY;
            int sliderWidth = 150;
            renderSlider(context, mouseX, mouseY, sliderX, sliderY, sliderWidth, 
                trigger.getCPS(), 1.0, 20.0);
            settingY += 35;
            
             
            context.drawText(this.textRenderer, "Blockhit:", x + 20, settingY, TEXT_PRIMARY, false);
            boolean blockhit = trigger.isBlockhit();
            renderToggleButton(context, mouseX, mouseY, x + 150, settingY - 5, blockhit);
            settingY += 35;
        }
        
         
        if (modeValue.equals("1.9")) {
            context.drawText(this.textRenderer, "Cooldown %:", x + 20, settingY, TEXT_PRIMARY, false);
            context.drawText(this.textRenderer, String.format("%.0f%%", trigger.getCooldownProgress()), 
                x + 150, settingY, TEXT_SECONDARY, false);
            
             
            int sliderX = x + 220;
            int sliderY = settingY;
            int sliderWidth = 130;
            renderSlider(context, mouseX, mouseY, sliderX, sliderY, sliderWidth, 
                trigger.getCooldownProgress(), 0.0, 100.0);
            settingY += 35;
        }
        
         
        context.drawText(this.textRenderer, "Hit Range:", x + 20, settingY, TEXT_PRIMARY, false);
        context.drawText(this.textRenderer, String.format("%.1f", trigger.getHitRange()), 
            x + 150, settingY, TEXT_SECONDARY, false);
        
        int sliderX = x + 200;
        int sliderY = settingY;
        int sliderWidth = 150;
        renderSlider(context, mouseX, mouseY, sliderX, sliderY, sliderWidth, 
            trigger.getHitRange(), 1.0, 7.0);
        settingY += 35;
        
         
        context.drawText(this.textRenderer, "Crit Timing:", x + 20, settingY, TEXT_PRIMARY, false);
        renderToggleButton(context, mouseX, mouseY, x + 150, settingY - 5, trigger.isCritTiming());
        settingY += 35;
        
         
        context.drawText(this.textRenderer, "Require Weapon:", x + 20, settingY, TEXT_PRIMARY, false);
        renderToggleButton(context, mouseX, mouseY, x + 150, settingY - 5, trigger.isRequireWeapon());
    }
    
    private void renderSlider(DrawContext context, int mouseX, int mouseY, 
                              int x, int y, int width, double value, double min, double max) {
         
        drawRoundedRect(context, x, y - 2, width, 4, 0xFF2A2A2A);
        
         
        double percent = (value - min) / (max - min);
        int fillWidth = (int) (width * percent);
        drawRoundedRect(context, x, y - 2, fillWidth, 4, ACCENT);
        
         
        int handleX = x + fillWidth - 4;
        drawRoundedRect(context, handleX, y - 6, 8, 12, TEXT_PRIMARY);
    }
    
    private void renderToggleButton(DrawContext context, int mouseX, int mouseY, 
                                    int x, int y, boolean enabled) {
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
    
    private void drawRoundedRect(DrawContext context, int x, int y, int width, int height, int color) {
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
    
    private void drawGlow(DrawContext context, int x, int y, int width, int height) {
         
        int glowColor = 0x20000000;
        context.fill(x - 1, y - 1, x + width + 1, y, glowColor);
        context.fill(x - 1, y + height, x + width + 1, y + height + 1, glowColor);
        context.fill(x - 1, y - 1, x, y + height + 1, glowColor);
        context.fill(x + width, y - 1, x + width + 1, y + height + 1, glowColor);
    }
    
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
             
            if (mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + 30) {
                 
                int closeX = x + width - 25;
                int closeY = y + 8;
                if (!(mouseX >= closeX && mouseX <= closeX + 15 &&
                      mouseY >= closeY && mouseY <= closeY + 15)) {
                    dragging = true;
                    dragOffsetX = (int) (mouseX - x);
                    dragOffsetY = (int) (mouseY - y);
                    return true;
                }
            }
            
             
            int closeX = x + width - 25;
            int closeY = y + 8;
            if (mouseX >= closeX && mouseX <= closeX + 15 &&
                mouseY >= closeY && mouseY <= closeY + 15) {
                this.close();
                return true;
            }
            
             
            int backX = x + 10;
            int backY = y + height - 35;
            if (mouseX >= backX && mouseX <= backX + 60 &&
                mouseY >= backY && mouseY <= backY + 25) {
                this.close();
                return true;
            }
            
             
            if (module instanceof TriggerBot) {
                return handleTriggerBotClicks(mouseX, mouseY);
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
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
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0) {
            dragging = false;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }
    
    private boolean handleTriggerBotClicks(double mouseX, double mouseY) {
        TriggerBot trigger = (TriggerBot) module;
        int settingY = y + 45;
        
         
        int modeX = x + 150;
        for (String mode : new String[]{"1.8", "1.9"}) {
            if (mouseX >= modeX && mouseX <= modeX + 40 &&
                mouseY >= settingY - 5 && mouseY <= settingY + 15) {
                trigger.setMode(mode);
                return true;
            }
            modeX += 45;
        }
        settingY += 35;
        
        String modeValue = trigger.getMode();
        
         
        if (modeValue.equals("1.8")) {
            if (handleSliderClick(mouseX, mouseY, x + 200, settingY, 150, 1.0, 20.0, trigger.getCPS())) {
                double newValue = calculateSliderValue(mouseX, x + 200, 150, 1.0, 20.0);
                trigger.setCPS(newValue);
                return true;
            }
            settingY += 35;
            
             
            if (mouseX >= x + 150 && mouseX <= x + 192 &&
                mouseY >= settingY - 5 && mouseY <= settingY + 15) {
                trigger.setBlockhit(!trigger.isBlockhit());
                return true;
            }
            settingY += 35;
        }
        
         
        if (modeValue.equals("1.9")) {
            if (handleSliderClick(mouseX, mouseY, x + 220, settingY, 130, 0.0, 100.0, trigger.getCooldownProgress())) {
                double newValue = calculateSliderValue(mouseX, x + 220, 130, 0.0, 100.0);
                trigger.setCooldownProgress(newValue);
                return true;
            }
            settingY += 35;
        }
        
         
        if (handleSliderClick(mouseX, mouseY, x + 200, settingY, 150, 1.0, 7.0, trigger.getHitRange())) {
            double newValue = calculateSliderValue(mouseX, x + 200, 150, 1.0, 7.0);
            trigger.setHitRange(newValue);
            return true;
        }
        settingY += 35;
        
         
        if (mouseX >= x + 150 && mouseX <= x + 192 &&
            mouseY >= settingY - 5 && mouseY <= settingY + 15) {
            trigger.setCritTiming(!trigger.isCritTiming());
            return true;
        }
        settingY += 35;
        
         
        if (mouseX >= x + 150 && mouseX <= x + 192 &&
            mouseY >= settingY - 5 && mouseY <= settingY + 15) {
            trigger.setRequireWeapon(!trigger.isRequireWeapon());
            return true;
        }
        
        return false;
    }
    
    private boolean handleSliderClick(double mouseX, double mouseY, int sliderX, int sliderY, 
                                     int width, double min, double max, double currentValue) {
        return mouseX >= sliderX && mouseX <= sliderX + width &&
               mouseY >= sliderY - 10 && mouseY <= sliderY + 10;
    }
    
    private double calculateSliderValue(double mouseX, int sliderX, int width, double min, double max) {
        double percent = Math.max(0, Math.min(1, (mouseX - sliderX) / width));
        return min + (percent * (max - min));
    }
    
    @Override
    public void close() {
        if (this.client != null) {
            this.client.setScreen(parent);
        }
    }
    
    @Override
    public boolean shouldPause() {
        return false;
    }
}