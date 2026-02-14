package play451.is.larping.features.gui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClickGui extends Screen {
    private static ClickGui INSTANCE;
    
    private int x = 100;
    private int y = 100;
    private int width = 550;
    private int height = 400;
    
    private static final int SIDEBAR_WIDTH = 160;
    private int selectedCategory = 0;
    private final List<Category> categories = new ArrayList<>();
    private final Map<String, List<Module>> modulesByCategory = new HashMap<>();
    
    // Dragging
    private boolean dragging = false;
    private int dragOffsetX = 0;
    private int dragOffsetY = 0;
    
    // Animation
    private float categoryAnimProgress = 0f;
    private int animatingFrom = 0;
    
    // Scrolling
    private int scrollOffset = 0;
    private static final int MAX_SCROLL = 500;
    
    // Colors - Modern Dark Theme with Purple Accent
    private static final int BACKGROUND = 0xF00A0A0A;
    private static final int SIDEBAR_BG = 0xF0141414;
    private static final int ACCENT = 0xFF8B5CF6; // Purple
    private static final int ACCENT_HOVER = 0xFFA78BFA;
    private static final int ACCENT_DIM = 0x508B5CF6;
    private static final int TEXT_PRIMARY = 0xFFF5F5F5;
    private static final int TEXT_SECONDARY = 0xFF9CA3AF;
    private static final int TEXT_DIM = 0xFF6B7280;
    private static final int BORDER = 0xFF1F1F1F;
    private static final int MODULE_BG = 0xFF161616;
    private static final int MODULE_HOVER = 0xFF1F1F1F;
    private static final int ENABLED_GREEN = 0xFF10B981;
    private static final int DISABLED_RED = 0xFFEF4444;

    public ClickGui() {
        super(Text.literal("Click GUI"));
        initializeCategories();
    }

    public static ClickGui getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ClickGui();
        }
        return INSTANCE;
    }
    
    private void initializeCategories() {
        // Combat category
        List<Module> combat = new ArrayList<>();
        combat.add(new Module("KillAura", "Combat", "Auto attack nearby entities"));
        combat.add(new Module("Velocity", "Combat", "Modify knockback"));
        combat.add(new Module("Criticals", "Combat", "Force critical hits"));
        combat.add(new Module("AutoClicker", "Combat", "Automatic clicking"));
        modulesByCategory.put("Combat", combat);
        categories.add(new Category("Combat", "⚔"));
        
        // Movement category
        List<Module> movement = new ArrayList<>();
        movement.add(new Module("Speed", "Movement", "Move faster"));
        movement.add(new Module("Fly", "Movement", "Creative flight"));
        movement.add(new Module("NoFall", "Movement", "Prevent fall damage"));
        movement.add(new Module("Sprint", "Movement", "Auto sprint"));
        movement.add(new Module("Step", "Movement", "Step up blocks"));
        modulesByCategory.put("Movement", movement);
        categories.add(new Category("Movement", "➤"));
        
        // Player category
        List<Module> player = new ArrayList<>();
        player.add(new Module("AutoEat", "Player", "Eat food automatically"));
        player.add(new Module("FastPlace", "Player", "Place blocks faster"));
        player.add(new Module("NoBreakDelay", "Player", "Remove break delay"));
        player.add(new Module("AutoTool", "Player", "Auto select best tool"));
        player.add(new Module("Freecam", "Player", "Detach camera"));
        modulesByCategory.put("Player", player);
        categories.add(new Category("Player", "👤"));
        
        // Render category
        List<Module> render = new ArrayList<>();
        render.add(new Module("ESP", "Render", "See entities through walls"));
        render.add(new Module("Tracers", "Render", "Draw lines to entities"));
        render.add(new Module("Nametags", "Render", "Enhanced nametags"));
        render.add(new Module("Fullbright", "Render", "Maximum brightness"));
        render.add(new Module("NoRender", "Render", "Remove overlays"));
        modulesByCategory.put("Render", render);
        categories.add(new Category("Render", "👁"));
        
        // World category
        List<Module> world = new ArrayList<>();
        world.add(new Module("Scaffold", "World", "Auto bridge"));
        world.add(new Module("Timer", "World", "Change game speed"));
        world.add(new Module("FastBreak", "World", "Break blocks faster"));
        world.add(new Module("Nuker", "World", "Break nearby blocks"));
        world.add(new Module("AutoMine", "World", "Auto mine blocks"));
        modulesByCategory.put("World", world);
        categories.add(new Category("World", "🌍"));
        
        // Misc category
        List<Module> misc = new ArrayList<>();
        misc.add(new Module("AutoFish", "Misc", "Automatic fishing"));
        misc.add(new Module("ChatBot", "Misc", "Auto chat responses"));
        misc.add(new Module("Spammer", "Misc", "Chat spammer"));
        misc.add(new Module("AntiAFK", "Misc", "Prevent AFK kicks"));
        misc.add(new Module("NoWeather", "Misc", "Clear weather"));
        modulesByCategory.put("Misc", misc);
        categories.add(new Category("Misc", "⚙"));
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // Update animations
        if (categoryAnimProgress < 1.0f) {
            categoryAnimProgress = Math.min(1.0f, categoryAnimProgress + delta * 4);
        }
        
        // Render dark overlay
        context.fillGradient(0, 0, this.width, this.height, 0x90000000, 0x90000000);
        
        // Main window shadow effect
        context.fill(x + 3, y + 3, x + width + 3, y + height + 3, 0x40000000);
        
        // Main window background
        context.fill(x, y, x + width, y + height, BACKGROUND);
        
        // Sidebar with gradient
        context.fill(x, y, x + SIDEBAR_WIDTH, y + height, SIDEBAR_BG);
        context.fillGradient(x + SIDEBAR_WIDTH - 1, y, x + SIDEBAR_WIDTH, y + height, ACCENT_DIM, 0x00000000);
        
        // Title bar with gradient
        context.fillGradient(x, y, x + width, y + 35, ACCENT, ACCENT_HOVER);
        
        // Title text with shadow
        String title = "L4RP CLIENT";
        int titleWidth = this.textRenderer.getWidth(title);
        context.drawTextWithShadow(this.textRenderer, title, x + SIDEBAR_WIDTH / 2 - titleWidth / 2, y + 12, TEXT_PRIMARY);
        
        // Render sidebar categories
        renderSidebar(context, mouseX, mouseY);
        
        // Content area
        renderContentArea(context, mouseX, mouseY);
        
        // Border
        drawGlowBorder(context, x, y, width, height);
        
        // Footer bar
        context.fill(x, y + height - 25, x + width, y + height, 0xFF0D0D0D);
        context.drawText(this.textRenderer, "Press RSHIFT to close", x + SIDEBAR_WIDTH + 10, y + height - 15, TEXT_DIM, false);
        context.drawText(this.textRenderer, "v1.0.0", x + width - 40, y + height - 15, TEXT_DIM, false);
    }
    
    private void renderSidebar(DrawContext context, int mouseX, int mouseY) {
        int categoryY = y + 45;
        
        for (int i = 0; i < categories.size(); i++) {
            Category cat = categories.get(i);
            boolean isSelected = i == selectedCategory;
            boolean isHovered = mouseX >= x && mouseX <= x + SIDEBAR_WIDTH && 
                               mouseY >= categoryY && mouseY <= categoryY + 35;
            
            // Category background with smooth transition
            if (isSelected) {
                context.fill(x + 5, categoryY + 2, x + SIDEBAR_WIDTH - 5, categoryY + 33, ACCENT);
                // Glowing left border
                context.fill(x + 2, categoryY + 2, x + 5, categoryY + 33, ACCENT_HOVER);
            } else if (isHovered) {
                context.fill(x + 5, categoryY + 2, x + SIDEBAR_WIDTH - 5, categoryY + 33, MODULE_HOVER);
            }
            
            // Icon
            int iconX = x + 15;
            int iconY = categoryY + 10;
            context.drawText(this.textRenderer, cat.icon, iconX, iconY, isSelected ? TEXT_PRIMARY : TEXT_SECONDARY, false);
            
            // Category text
            int textColor = isSelected ? TEXT_PRIMARY : TEXT_SECONDARY;
            context.drawText(this.textRenderer, cat.name, x + 35, categoryY + 10, textColor, false);
            
            // Module count badge
            int moduleCount = modulesByCategory.get(cat.name).size();
            String countStr = String.valueOf(moduleCount);
            int badgeX = x + SIDEBAR_WIDTH - 25;
            int badgeY = categoryY + 10;
            int badgeWidth = this.textRenderer.getWidth(countStr) + 8;
            
            context.fill(badgeX, badgeY - 2, badgeX + badgeWidth, badgeY + 10, isSelected ? ACCENT_DIM : 0xFF2A2A2A);
            context.drawText(this.textRenderer, countStr, badgeX + 4, badgeY, TEXT_DIM, false);
            
            categoryY += 37;
        }
    }
    
    private void renderContentArea(DrawContext context, int mouseX, int mouseY) {
        int contentX = x + SIDEBAR_WIDTH + 15;
        int contentY = y + 45;
        int contentWidth = width - SIDEBAR_WIDTH - 30;
        
        Category currentCategory = categories.get(selectedCategory);
        List<Module> modules = modulesByCategory.get(currentCategory.name);
        
        // Category header with icon
        context.drawText(this.textRenderer, currentCategory.icon, contentX, contentY, ACCENT, false);
        context.drawTextWithShadow(this.textRenderer, currentCategory.name, contentX + 15, contentY, TEXT_PRIMARY);
        
        // Separator line
        context.fill(contentX, contentY + 15, contentX + contentWidth, contentY + 16, BORDER);
        
        contentY += 25;
        
        // Module grid
        int moduleWidth = 170;
        int moduleHeight = 65;
        int moduleSpacing = 12;
        int modulesPerRow = Math.max(1, contentWidth / (moduleWidth + moduleSpacing));
        
        for (int i = 0; i < modules.size(); i++) {
            Module module = modules.get(i);
            int col = i % modulesPerRow;
            int row = i / modulesPerRow;
            
            int moduleX = contentX + col * (moduleWidth + moduleSpacing);
            int moduleY = contentY + row * (moduleHeight + moduleSpacing);
            
            // Check if module is visible
            if (moduleY + moduleHeight < y + 45 || moduleY > y + height - 30) {
                continue;
            }
            
            boolean isHovered = mouseX >= moduleX && mouseX <= moduleX + moduleWidth && 
                               mouseY >= moduleY && mouseY <= moduleY + moduleHeight;
            
            // Module background with hover effect
            int bgColor = isHovered ? MODULE_HOVER : MODULE_BG;
            context.fill(moduleX, moduleY, moduleX + moduleWidth, moduleY + moduleHeight, bgColor);
            
            // Module border - colored if enabled
            int borderColor = module.isEnabled() ? ACCENT : (isHovered ? BORDER : 0xFF0F0F0F);
            drawBorder(context, moduleX, moduleY, moduleWidth, moduleHeight, borderColor);
            
            // Enabled indicator bar on left
            if (module.isEnabled()) {
                context.fill(moduleX, moduleY, moduleX + 3, moduleY + moduleHeight, ACCENT_HOVER);
            }
            
            // Module name
            context.drawText(this.textRenderer, module.getName(), 
                moduleX + 10, moduleY + 10, TEXT_PRIMARY, false);
            
            // Module description
            String desc = module.getDescription();
            if (desc.length() > 25) {
                desc = desc.substring(0, 22) + "...";
            }
            context.drawText(this.textRenderer, desc, 
                moduleX + 10, moduleY + 25, TEXT_DIM, false);
            
            // Toggle indicator with animated background
            int toggleX = moduleX + moduleWidth - 50;
            int toggleY = moduleY + moduleHeight - 25;
            int toggleWidth = 40;
            int toggleHeight = 18;
            
            // Toggle background
            int toggleBg = module.isEnabled() ? ENABLED_GREEN : 0xFF2A2A2A;
            context.fill(toggleX, toggleY, toggleX + toggleWidth, toggleY + toggleHeight, toggleBg);
            
            // Toggle circle
            int circleX = module.isEnabled() ? toggleX + toggleWidth - 16 : toggleX + 2;
            context.fill(circleX, toggleY + 2, circleX + 14, toggleY + 16, TEXT_PRIMARY);
            
            // Status text
            String status = module.isEnabled() ? "ON" : "OFF";
            int statusColor = module.isEnabled() ? ENABLED_GREEN : TEXT_DIM;
            context.drawText(this.textRenderer, status, 
                moduleX + 10, moduleY + moduleHeight - 20, statusColor, false);
        }
    }
    
    private void drawGlowBorder(DrawContext context, int x, int y, int width, int height) {
        // Outer glow
        context.fill(x - 1, y - 1, x + width + 1, y, 0x40000000); // Top
        context.fill(x - 1, y + height, x + width + 1, y + height + 1, 0x40000000); // Bottom
        context.fill(x - 1, y - 1, x, y + height + 1, 0x40000000); // Left
        context.fill(x + width, y - 1, x + width + 1, y + height + 1, 0x40000000); // Right
        
        // Inner border
        drawBorder(context, x, y, width, height, BORDER);
    }
    
    private void drawBorder(DrawContext context, int x, int y, int width, int height, int color) {
        context.fill(x, y, x + width, y + 1, color); // Top
        context.fill(x, y + height - 1, x + width, y + height, color); // Bottom
        context.fill(x, y, x + 1, y + height, color); // Left
        context.fill(x + width - 1, y, x + width, y + height, color); // Right
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) { // Left click
            // Check title bar drag
            if (mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + 35) {
                dragging = true;
                dragOffsetX = (int) (mouseX - x);
                dragOffsetY = (int) (mouseY - y);
                return true;
            }
            
            // Check sidebar category click
            int categoryY = y + 45;
            for (int i = 0; i < categories.size(); i++) {
                if (mouseX >= x && mouseX <= x + SIDEBAR_WIDTH && 
                    mouseY >= categoryY && mouseY <= categoryY + 35) {
                    if (selectedCategory != i) {
                        animatingFrom = selectedCategory;
                        selectedCategory = i;
                        categoryAnimProgress = 0f;
                    }
                    return true;
                }
                categoryY += 37;
            }
            
            // Check module clicks
            int contentX = x + SIDEBAR_WIDTH + 15;
            int contentY = y + 70;
            int moduleWidth = 170;
            int moduleHeight = 65;
            int moduleSpacing = 12;
            int contentWidth = width - SIDEBAR_WIDTH - 30;
            int modulesPerRow = Math.max(1, contentWidth / (moduleWidth + moduleSpacing));
            
            Category currentCategory = categories.get(selectedCategory);
            List<Module> modules = modulesByCategory.get(currentCategory.name);
            
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
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        scrollOffset = (int) Math.max(0, Math.min(MAX_SCROLL, scrollOffset - verticalAmount * 20));
        return true;
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
    
    private static class Category {
        String name;
        String icon;
        
        Category(String name, String icon) {
            this.name = name;
            this.icon = icon;
        }
    }
}