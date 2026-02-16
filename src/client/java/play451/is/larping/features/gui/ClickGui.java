package play451.is.larping.features.gui;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.MinecraftClient;
import play451.is.larping.config.Config;
import play451.is.larping.features.modules.Module;
import play451.is.larping.features.modules.ModuleCategory;
import play451.is.larping.features.modules.ModuleManager;

import java.util.List;

public class ClickGui extends Screen {
    private static ClickGui INSTANCE;
    private TextRenderer customFont;
    
     
    private int x;
    private int y;
    private int width = 480;  
    private int height = 360;  
    
     
    private static final int SIDEBAR_WIDTH = 100;  
    private ModuleCategory selectedCategory = ModuleCategory.COMBAT;
    
     
    private boolean dragging = false;
    private int dragOffsetX = 0;
    private int dragOffsetY = 0;
    
     
    private int scrollOffset = 0;
    
     
    private static final int BG_DARK = 0xF0050505;
    private static final int BG_DARKER = 0xF0020202;
    private static final int SIDEBAR_BG = 0xF0080808;
    private static final int ACCENT = 0xFF1E90FF;  
    private static final int ACCENT_DARK = 0xFF1873CC;
    private static final int ACCENT_GLOW = 0x401E90FF;
    private static final int TEXT_PRIMARY = 0xFFF0F0F0;
    private static final int TEXT_SECONDARY = 0xFF999999;
    private static final int TEXT_DIM = 0xFF666666;
    private static final int MODULE_BG = 0xFF0A0A0A;
    private static final int MODULE_HOVER = 0xFF101010;
    private static final int ENABLED_GLOW = 0xFF10B981;
    private static final int BORDER_SUBTLE = 0xFF151515;

    public ClickGui() {
        super(Text.literal("Click GUI"));
        loadPosition();
        loadCustomFont();
    }
    
    private void loadPosition() {
        Config config = Config.getInstance();
        this.x = config.getGuiX();
        this.y = config.getGuiY();
         
    }
    
    private void loadCustomFont() {
        this.customFont = MinecraftClient.getInstance().textRenderer;
    }

    public static ClickGui getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ClickGui();
        }
        return INSTANCE;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
         
        context.fillGradient(0, 0, this.width, this.height, 0xC0000000, 0xE0000000);
        
         
        context.fill(x - 1, y - 1, x + width + 1, y, 0x40000000);
        context.fill(x - 1, y + height, x + width + 1, y + height + 1, 0x40000000);
        context.fill(x - 1, y - 1, x, y + height + 1, 0x40000000);
        context.fill(x + width, y - 1, x + width + 1, y + height + 1, 0x40000000);
        
         
        context.fill(x, y, x + width, y + height, BG_DARK);
        
         
        context.fill(x, y, x + SIDEBAR_WIDTH, y + height, SIDEBAR_BG);
        
         
        context.fill(x + SIDEBAR_WIDTH, y, x + SIDEBAR_WIDTH + 1, y + height, BORDER_SUBTLE);
        
         
        context.fill(x, y, x + width, y + 30, BG_DARKER);
        
         
        String title = "L4RP";
        int titleWidth = this.customFont.getWidth(title);
        context.drawTextWithShadow(this.customFont, title, 
            x + SIDEBAR_WIDTH / 2 - titleWidth / 2, y + 10, ACCENT);
        
         
        renderSidebar(context, mouseX, mouseY);
        renderContentArea(context, mouseX, mouseY);
        
         
        context.drawText(this.customFont, "RSHIFT to close", 
            x + SIDEBAR_WIDTH + 10, y + height - 15, TEXT_DIM, false);
        context.drawText(this.customFont, "v1.0", 
            x + width - 30, y + height - 15, TEXT_DIM, false);
    }
    
    private void renderSidebar(DrawContext context, int mouseX, int mouseY) {
        int categoryY = y + 40;
        
        for (ModuleCategory category : ModuleCategory.values()) {
            List<Module> modules = ModuleManager.getInstance().getModulesByCategory(category);
            if (modules.isEmpty()) continue;
            
            boolean isSelected = category == selectedCategory;
            boolean isHovered = mouseX >= x + 2 && mouseX <= x + SIDEBAR_WIDTH - 2 && 
                               mouseY >= categoryY && mouseY <= categoryY + 24;  
            
             
            if (isSelected) {
                context.fill(x + 4, categoryY, x + SIDEBAR_WIDTH - 4, categoryY + 24, ACCENT_DARK);
                 
                context.fill(x + 2, categoryY, x + 4, categoryY + 24, ACCENT);
            } else if (isHovered) {
                context.fill(x + 4, categoryY, x + SIDEBAR_WIDTH - 4, categoryY + 24, MODULE_HOVER);
            }
            
             
            int textColor = isSelected ? TEXT_PRIMARY : TEXT_SECONDARY;
            context.drawText(this.customFont, category.getName(), 
                x + 8, categoryY + 8, textColor, false);  
            
             
            String count = String.valueOf(modules.size());
            int badgeX = x + SIDEBAR_WIDTH - 18;  
            int badgeY = categoryY + 6;
            int badgeWidth = this.customFont.getWidth(count) + 6;
            
            context.fill(badgeX, badgeY, badgeX + badgeWidth, badgeY + 12, 
                isSelected ? ACCENT_GLOW : 0xFF0F0F0F);
            context.drawText(this.customFont, count, badgeX + 3, badgeY + 2, TEXT_DIM, false);
            
            categoryY += 28;  
        }
    }
    
    private void renderContentArea(DrawContext context, int mouseX, int mouseY) {
        int contentX = x + SIDEBAR_WIDTH + 15;
        int contentY = y + 45;
        int contentWidth = width - SIDEBAR_WIDTH - 25;
        
        List<Module> modules = ModuleManager.getInstance().getModulesByCategory(selectedCategory);
        
         
        context.drawTextWithShadow(this.customFont, selectedCategory.getName(), 
            contentX, contentY - 5, TEXT_PRIMARY);
        
        contentY += 18;
        
         
        if (modules.isEmpty()) {
            String msg = "No modules in this category";
            int msgWidth = this.customFont.getWidth(msg);
            context.drawText(this.customFont, msg, 
                contentX + contentWidth / 2 - msgWidth / 2, contentY + 50, TEXT_DIM, false);
            return;
        }
        
         
        int moduleWidth = 115;  
        int moduleHeight = 48;  
        int moduleSpacing = 6;  
        int modulesPerRow = Math.max(1, contentWidth / (moduleWidth + moduleSpacing));
        
        for (int i = 0; i < modules.size(); i++) {
            Module module = modules.get(i);
            int col = i % modulesPerRow;
            int row = i / modulesPerRow;
            
            int moduleX = contentX + col * (moduleWidth + moduleSpacing);
            int moduleY = contentY + row * (moduleHeight + moduleSpacing);
            
             
            if (moduleY + moduleHeight < y + 45 || moduleY > y + height - 20) {
                continue;
            }
            
            boolean isHovered = mouseX >= moduleX && mouseX <= moduleX + moduleWidth && 
                               mouseY >= moduleY && mouseY <= moduleY + moduleHeight;
            
             
            int bgColor = isHovered ? MODULE_HOVER : MODULE_BG;
            context.fill(moduleX, moduleY, moduleX + moduleWidth, moduleY + moduleHeight, bgColor);
            
             
            if (module.isEnabled()) {
                context.fill(moduleX, moduleY + 4, moduleX + 2, moduleY + moduleHeight - 4, ACCENT);
                 
                context.fill(moduleX, moduleY, moduleX + moduleWidth, moduleY + moduleHeight, 
                    isHovered ? 0x20FFFFFF : 0x10FFFFFF);
            } else if (isHovered) {
                context.fill(moduleX, moduleY + 4, moduleX + 2, moduleY + moduleHeight - 4, BORDER_SUBTLE);
            }
            
             
            context.drawText(this.customFont, module.getName(), 
                moduleX + 8, moduleY + 6, TEXT_PRIMARY, false);
            
             
            int settingsX = moduleX + moduleWidth - 15;
            int settingsY = moduleY + 5;
            int settingsSize = 10;
            boolean settingsHovered = mouseX >= settingsX && mouseX <= settingsX + settingsSize &&
                                     mouseY >= settingsY && mouseY <= settingsY + settingsSize;
            
             
            int settingsBg = settingsHovered ? ACCENT : 0xFF1F1F1F;
            context.fill(settingsX, settingsY, settingsX + settingsSize, settingsY + settingsSize, settingsBg);
            
             
            context.fill(settingsX + 4, settingsY + 2, settingsX + 6, settingsY + 4, TEXT_PRIMARY);
            context.fill(settingsX + 2, settingsY + 4, settingsX + 4, settingsY + 6, TEXT_PRIMARY);
            context.fill(settingsX + 6, settingsY + 4, settingsX + 8, settingsY + 6, TEXT_PRIMARY);
            context.fill(settingsX + 4, settingsY + 6, settingsX + 6, settingsY + 8, TEXT_PRIMARY);
            
             
            String desc = module.getDescription();
            if (desc.length() > 19) {
                desc = desc.substring(0, 16) + "...";
            }
            context.drawText(this.customFont, desc, 
                moduleX + 8, moduleY + 18, TEXT_DIM, false);
            
             
            int toggleX = moduleX + moduleWidth - 38;
            int toggleY = moduleY + moduleHeight - 18;
            renderToggle(context, toggleX, toggleY, module.isEnabled());
            
             
            String status = module.isEnabled() ? "ON" : "OFF";
            int statusColor = module.isEnabled() ? ENABLED_GLOW : TEXT_DIM;
            context.drawText(this.customFont, status, 
                moduleX + 8, moduleY + moduleHeight - 14, statusColor, false);
        }
    }
    
    private void renderToggle(DrawContext context, int x, int y, boolean enabled) {
        int width = 36;  
        int height = 16;  
        
         
        int bgColor = enabled ? ENABLED_GLOW : 0xFF1A1A1A;
        context.fill(x, y, x + width, y + height, bgColor);
        
         
        int circleSize = 12;  
        int circleX = enabled ? x + width - circleSize - 2 : x + 2;
        int circleY = y + 2;
        context.fill(circleX, circleY, circleX + circleSize, circleY + circleSize, TEXT_PRIMARY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
             
            if (mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + 30) {
                dragging = true;
                dragOffsetX = (int) (mouseX - x);
                dragOffsetY = (int) (mouseY - y);
                return true;
            }
            
             
            int categoryY = y + 40;
            for (ModuleCategory category : ModuleCategory.values()) {
                List<Module> modules = ModuleManager.getInstance().getModulesByCategory(category);
                if (modules.isEmpty()) continue;
                
                if (mouseX >= x + 2 && mouseX <= x + SIDEBAR_WIDTH - 2 && 
                    mouseY >= categoryY && mouseY <= categoryY + 24) {  
                    selectedCategory = category;
                    scrollOffset = 0;
                    return true;
                }
                categoryY += 28;  
            }
            
             
            int contentX = x + SIDEBAR_WIDTH + 15;
            int contentY = y + 63;
            int moduleWidth = 115;  
            int moduleHeight = 48;  
            int moduleSpacing = 6;  
            int contentWidth = width - SIDEBAR_WIDTH - 25;
            int modulesPerRow = Math.max(1, contentWidth / (moduleWidth + moduleSpacing));
            
            List<Module> modules = ModuleManager.getInstance().getModulesByCategory(selectedCategory);
            for (int i = 0; i < modules.size(); i++) {
                Module module = modules.get(i);
                int col = i % modulesPerRow;
                int row = i / modulesPerRow;
                
                int moduleX = contentX + col * (moduleWidth + moduleSpacing);
                int moduleY = contentY + row * (moduleHeight + moduleSpacing);
                
                 
                int settingsX = moduleX + moduleWidth - 15;
                int settingsY = moduleY + 5;
                int settingsSize = 10;
                
                if (mouseX >= settingsX && mouseX <= settingsX + settingsSize &&
                    mouseY >= settingsY && mouseY <= settingsY + settingsSize) {
                    openModuleSettings(module);
                    return true;
                }
                
                 
                if (mouseX >= moduleX && mouseX <= moduleX + moduleWidth && 
                    mouseY >= moduleY && mouseY <= moduleY + moduleHeight) {
                    module.toggle();
                    return true;
                }
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
            if (dragging) {
                 
                Config.getInstance().setGuiPosition(x, y);
            }
            dragging = false;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }
    
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        scrollOffset = Math.max(0, Math.min(500, (int) (scrollOffset - verticalAmount * 20)));
        return true;
    }
    
    private void openModuleSettings(Module module) {
        if (this.client != null) {
            this.client.setScreen(new ModuleSettingsScreen(this, module));
        }
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}