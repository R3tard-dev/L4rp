package play451.is.larping.features.gui;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import play451.is.larping.features.modules.Module;
import play451.is.larping.features.modules.ModuleSettingsRenderer;
import play451.is.larping.features.modules.SettingsHelper;

public class ModuleSettingsScreen extends Screen {
    private final Screen parent;
    private final Module module;
    private final SettingsHelper settingsHelper;
    
    private int x;
    private int y;
    private int width = 360;
    private int height = 280;
    
     
    private boolean dragging = false;
    private int dragOffsetX = 0;
    private int dragOffsetY = 0;
    
     
    private static final int BG_DARK = 0xF0050505;
    private static final int TEXT_PRIMARY = 0xFFF0F0F0;
    private static final int TEXT_SECONDARY = 0xFF999999;
    private static final int BUTTON_BG = 0xFF1A1A1A;
    private static final int BUTTON_HOVER = 0xFF252525;
    private static final int BORDER_SUBTLE = 0xFF151515;
    
    public ModuleSettingsScreen(Screen parent, Module module) {
        super(Text.literal("Module Settings"));
        this.parent = parent;
        this.module = module;
        this.settingsHelper = new SettingsHelper(null);  
    }
    
    @Override
    protected void init() {
        super.init();
        this.x = (this.width - 360) / 2;
        this.y = (this.height - 280) / 2;
        
         
        java.lang.reflect.Field field;
        try {
            field = SettingsHelper.class.getDeclaredField("textRenderer");
            field.setAccessible(true);
            field.set(settingsHelper, this.textRenderer);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
         
        context.fillGradient(0, 0, this.width, this.height, 0xB0000000, 0xB0000000);
        
         
        drawGlow(context, x, y, width, height);
        
         
        settingsHelper.drawRoundedRect(context, x, y, width, height, BG_DARK);
        
         
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
        int contentX = x + 20;
        
         
        if (module instanceof ModuleSettingsRenderer) {
            ((ModuleSettingsRenderer) module).renderSettings(context, mouseX, mouseY, contentX, contentY, width - 40, settingsHelper);
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
        
        settingsHelper.drawRoundedRect(context, backX, backY, backWidth, backHeight, 
            backHovered ? BUTTON_HOVER : BUTTON_BG);
        context.drawText(this.textRenderer, "Back", backX + 18, backY + 8, TEXT_PRIMARY, false);
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
            
             
            if (module instanceof ModuleSettingsRenderer) {
                int contentY = y + 45;
                int contentX = x + 20;
                return ((ModuleSettingsRenderer) module).handleSettingsClick(mouseX, mouseY, contentX, contentY, width - 40, settingsHelper);
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