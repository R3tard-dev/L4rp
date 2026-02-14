package play451.is.larping.features.gui;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import play451.is.larping.config.Config;
import play451.is.larping.features.modules.Module;
import play451.is.larping.features.modules.ModuleCategory;
import play451.is.larping.features.modules.ModuleManager;

import java.util.List;

public class ClickGui extends Screen {
    private static ClickGui INSTANCE;
    
    // Main window
    private int x;
    private int y;
    private int width;
    private int height;
    
    // Sidebar
    private static final int SIDEBAR_WIDTH = 180;
    private ModuleCategory selectedCategory = ModuleCategory.COMBAT;
    
    // Dragging
    private boolean dragging = false;
    private int dragOffsetX = 0;
    private int dragOffsetY = 0;
    
    // Scrolling
    private int scrollOffset = 0;
    
    // Colors - Ultra Dark Theme
    private static final int BG_DARK = 0xF0050505;
    private static final int BG_DARKER = 0xF0020202;
    private static final int SIDEBAR_BG = 0xF0080808;
    private static final int ACCENT = 0xFF7C3AED; // Purple
    private static final int ACCENT_DARK = 0xFF6D28D9;
    private static final int ACCENT_GLOW = 0x407C3AED;
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
    }
    
    private void loadPosition() {
        Config config = Config.getInstance();
        this.x = config.getGuiX();
        this.y = config.getGuiY();
        this.width = config.getGuiWidth();
        this.height = config.getGuiHeight();
    }

    public static ClickGui getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ClickGui();
        }
        return INSTANCE;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // Dark overlay
        context.fillGradient(0, 0, this.width, this.height, 0xB0000000, 0xB0000000);
        
        // Main window with rounded effect
        drawRoundedRect(context, x, y, width, height, BG_DARK);
        
        // Sidebar
        drawRoundedRect(context, x, y, SIDEBAR_WIDTH, height, SIDEBAR_BG);
        
        // Subtle vertical divider
        context.fill(x + SIDEBAR_WIDTH, y + 40, x + SIDEBAR_WIDTH + 1, y + height, BORDER_SUBTLE);
        
        // Title bar area (no separate bar, just text)
        String title = "L4RP";
        int titleWidth = this.textRenderer.getWidth(title);
        context.drawTextWithShadow(this.textRenderer, title, 
            x + SIDEBAR_WIDTH / 2 - titleWidth / 2, y + 15, ACCENT);
        
        // Render sidebar
        renderSidebar(context, mouseX, mouseY);
        
        // Content area
        renderContentArea(context, mouseX, mouseY);
        
        // Subtle outer glow
        drawGlow(context, x, y, width, height);
        
        // Footer
        context.drawText(this.textRenderer, "RSHIFT to close", 
            x + SIDEBAR_WIDTH + 15, y + height - 20, TEXT_DIM, false);
        context.drawText(this.textRenderer, "v1.0", 
            x + width - 35, y + height - 20, TEXT_DIM, false);
    }
    
    private void renderSidebar(DrawContext context, int mouseX, int mouseY) {
        int categoryY = y + 50;
        
        for (ModuleCategory category : ModuleCategory.values()) {
            List<Module> modules = ModuleManager.getInstance().getModulesByCategory(category);
            if (modules.isEmpty()) continue; // Skip empty categories
            
            boolean isSelected = category == selectedCategory;
            boolean isHovered = mouseX >= x + 5 && mouseX <= x + SIDEBAR_WIDTH - 5 && 
                               mouseY >= categoryY && mouseY <= categoryY + 40;
            
            // Category background
            if (isSelected) {
                drawRoundedRect(context, x + 8, categoryY, SIDEBAR_WIDTH - 16, 40, ACCENT_DARK);
                // Glow effect
                context.fill(x + 5, categoryY, x + 8, categoryY + 40, ACCENT_GLOW);
            } else if (isHovered) {
                drawRoundedRect(context, x + 8, categoryY, SIDEBAR_WIDTH - 16, 40, MODULE_HOVER);
            }
            
            // Icon and text
            
            int textColor = isSelected ? TEXT_PRIMARY : TEXT_SECONDARY;
            context.drawText(this.textRenderer, category.getName(), 
                x + 40, categoryY + 13, textColor, false);
            
            // Module count badge
            String count = String.valueOf(modules.size());
            int badgeX = x + SIDEBAR_WIDTH - 30;
            int badgeY = categoryY + 12;
            int badgeWidth = this.textRenderer.getWidth(count) + 6;
            
            drawRoundedRect(context, badgeX, badgeY, badgeWidth, 16, isSelected ? ACCENT_GLOW : 0xFF0F0F0F);
            context.drawText(this.textRenderer, count, badgeX + 3, badgeY + 4, TEXT_DIM, false);
            
            categoryY += 45;
        }
    }
    
    private void renderContentArea(DrawContext context, int mouseX, int mouseY) {
        int contentX = x + SIDEBAR_WIDTH + 20;
        int contentY = y + 50;
        int contentWidth = width - SIDEBAR_WIDTH - 35;
        
        List<Module> modules = ModuleManager.getInstance().getModulesByCategory(selectedCategory);
        
        context.drawTextWithShadow(this.textRenderer, selectedCategory.getName(), 
            contentX + 20, contentY - 5, TEXT_PRIMARY);
        
        contentY += 20;
        
        // No modules message
        if (modules.isEmpty()) {
            String msg = "No modules in this category";
            int msgWidth = this.textRenderer.getWidth(msg);
            context.drawText(this.textRenderer, msg, 
                contentX + contentWidth / 2 - msgWidth / 2, contentY + 50, TEXT_DIM, false);
            return;
        }
        
        // Module grid
        int moduleWidth = 185;
        int moduleHeight = 70;
        int moduleSpacing = 12;
        int modulesPerRow = Math.max(1, contentWidth / (moduleWidth + moduleSpacing));
        
        for (int i = 0; i < modules.size(); i++) {
            Module module = modules.get(i);
            int col = i % modulesPerRow;
            int row = i / modulesPerRow;
            
            int moduleX = contentX + col * (moduleWidth + moduleSpacing);
            int moduleY = contentY + row * (moduleHeight + moduleSpacing);
            
            // Check visibility
            if (moduleY + moduleHeight < y + 50 || moduleY > y + height - 30) {
                continue;
            }
            
            boolean isHovered = mouseX >= moduleX && mouseX <= moduleX + moduleWidth && 
                               mouseY >= moduleY && mouseY <= moduleY + moduleHeight;
            
            // Module card background
            int bgColor = isHovered ? MODULE_HOVER : MODULE_BG;
            drawRoundedRect(context, moduleX, moduleY, moduleWidth, moduleHeight, bgColor);
            
            // Enabled indicator on left edge
            if (module.isEnabled()) {
                context.fill(moduleX, moduleY + 5, moduleX + 2, moduleY + moduleHeight - 5, ACCENT);
                // Subtle glow
                drawRoundedRect(context, moduleX, moduleY, moduleWidth, moduleHeight, 
                    isHovered ? 0x20FFFFFF : 0x10FFFFFF);
            } else if (isHovered) {
                context.fill(moduleX, moduleY + 5, moduleX + 2, moduleY + moduleHeight - 5, BORDER_SUBTLE);
            }
            
            // Module name
            context.drawText(this.textRenderer, module.getName(), 
                moduleX + 12, moduleY + 12, TEXT_PRIMARY, false);
            
            // Module description
            String desc = module.getDescription();
            if (desc.length() > 28) {
                desc = desc.substring(0, 25) + "...";
            }
            context.drawText(this.textRenderer, desc, 
                moduleX + 12, moduleY + 28, TEXT_DIM, false);
            
            // Toggle switch
            int toggleX = moduleX + moduleWidth - 55;
            int toggleY = moduleY + moduleHeight - 28;
            renderToggle(context, toggleX, toggleY, module.isEnabled());
            
            // Status text
            String status = module.isEnabled() ? "ON" : "OFF";
            int statusColor = module.isEnabled() ? ENABLED_GLOW : TEXT_DIM;
            context.drawText(this.textRenderer, status, 
                moduleX + 12, moduleY + moduleHeight - 22, statusColor, false);
        }
    }
    
    private void renderToggle(DrawContext context, int x, int y, boolean enabled) {
        int width = 42;
        int height = 20;
        
        // Toggle background with rounded edges
        int bgColor = enabled ? ENABLED_GLOW : 0xFF1A1A1A;
        drawRoundedRect(context, x, y, width, height, bgColor);
        
        // Toggle circle
        int circleSize = 16;
        int circleX = enabled ? x + width - circleSize - 2 : x + 2;
        int circleY = y + 2;
        drawRoundedRect(context, circleX, circleY, circleSize, circleSize, TEXT_PRIMARY);
    }
    
    private void drawRoundedRect(DrawContext context, int x, int y, int width, int height, int color) {
        // Simple rounded rect simulation
        // Main body
        context.fill(x + 1, y, x + width - 1, y + height, color);
        context.fill(x, y + 1, x + width, y + height - 1, color);
        
        // Corners (2x2 pixels)
        context.fill(x + 1, y + 1, x + 2, y + 2, color);
        context.fill(x + width - 2, y + 1, x + width - 1, y + 2, color);
        context.fill(x + 1, y + height - 2, x + 2, y + height - 1, color);
        context.fill(x + width - 2, y + height - 2, x + width - 1, y + height - 1, color);
    }
    
    private void drawGlow(DrawContext context, int x, int y, int width, int height) {
        // Subtle outer glow
        int glowColor = 0x20000000;
        context.fill(x - 1, y - 1, x + width + 1, y, glowColor);
        context.fill(x - 1, y + height, x + width + 1, y + height + 1, glowColor);
        context.fill(x - 1, y - 1, x, y + height + 1, glowColor);
        context.fill(x + width, y - 1, x + width + 1, y + height + 1, glowColor);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            // Title bar drag (top 40px)
            if (mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + 40) {
                dragging = true;
                dragOffsetX = (int) (mouseX - x);
                dragOffsetY = (int) (mouseY - y);
                return true;
            }
            
            // Sidebar category click
            int categoryY = y + 50;
            for (ModuleCategory category : ModuleCategory.values()) {
                List<Module> modules = ModuleManager.getInstance().getModulesByCategory(category);
                if (modules.isEmpty()) continue;
                
                if (mouseX >= x + 5 && mouseX <= x + SIDEBAR_WIDTH - 5 && 
                    mouseY >= categoryY && mouseY <= categoryY + 40) {
                    selectedCategory = category;
                    scrollOffset = 0;
                    return true;
                }
                categoryY += 45;
            }
            
            // Module clicks
            int contentX = x + SIDEBAR_WIDTH + 20;
            int contentY = y + 70;
            int moduleWidth = 185;
            int moduleHeight = 70;
            int moduleSpacing = 12;
            int contentWidth = width - SIDEBAR_WIDTH - 35;
            int modulesPerRow = Math.max(1, contentWidth / (moduleWidth + moduleSpacing));
            
            List<Module> modules = ModuleManager.getInstance().getModulesByCategory(selectedCategory);
            for (int i = 0; i < modules.size(); i++) {
                Module module = modules.get(i);
                int col = i % modulesPerRow;
                int row = i / modulesPerRow;
                
                int moduleX = contentX + col * (moduleWidth + moduleSpacing);
                int moduleY = contentY + row * (moduleHeight + moduleSpacing);
                
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
                // Save position when dragging stops
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

    @Override
    public boolean shouldPause() {
        return false;
    }
}