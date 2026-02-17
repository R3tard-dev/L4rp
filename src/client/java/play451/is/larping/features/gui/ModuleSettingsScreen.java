package play451.is.larping.features.gui;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;
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
    
    
    private boolean listeningForKey = false;
    
    
    private static final int BG_DARK       = 0xF0050505;
    private static final int BG_DARKER     = 0xF0020202;
    private static final int ACCENT        = 0xFF1E90FF;
    private static final int TEXT_PRIMARY   = 0xFFF0F0F0;
    private static final int TEXT_SECONDARY = 0xFF999999;
    private static final int TEXT_DIM       = 0xFF666666;
    private static final int BUTTON_BG      = 0xFF0A0A0A;
    private static final int BUTTON_HOVER   = 0xFF101010;
    private static final int BORDER_SUBTLE  = 0xFF151515;
    private static final int ENABLED_GLOW   = 0xFF10B981;
    
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
        
        try {
            var field = SettingsHelper.class.getDeclaredField("textRenderer");
            field.setAccessible(true);
            field.set(settingsHelper, this.textRenderer);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        
        context.fillGradient(0, 0, this.width, this.height, 0xC0000000, 0xE0000000);
        
        
        context.fill(x - 1, y - 1, x + width + 1, y,                   0x40000000);
        context.fill(x - 1, y + height, x + width + 1, y + height + 1, 0x40000000);
        context.fill(x - 1, y - 1, x,               y + height + 1,    0x40000000);
        context.fill(x + width, y - 1, x + width + 1, y + height + 1,  0x40000000);
        
        
        context.fill(x, y, x + width, y + height, BG_DARK);
        
        
        context.fill(x, y, x + width, y + 30, BG_DARKER);
        
        
        context.drawTextWithShadow(this.textRenderer, module.getName() + " Settings",
            x + 10, y + 10, TEXT_PRIMARY);
        
        
        int closeX = x + width - 18;
        int closeY = y + 8;
        boolean closeHovered = mouseX >= closeX && mouseX <= closeX + 12 &&
                               mouseY >= closeY && mouseY <= closeY + 14;
        context.fill(closeX, closeY, closeX + 12, closeY + 14,
            closeHovered ? 0xFF3A0000 : 0xFF1A0000);
        context.drawText(this.textRenderer, "X", closeX + 3, closeY + 3,
            closeHovered ? 0xFFFF5555 : 0xFF883333, false);
        
        
        int bindRowY = y + 30;
        context.fill(x, bindRowY, x + width, bindRowY + 26, 0xFF070707);
        context.fill(x, bindRowY + 26, x + width, bindRowY + 27, BORDER_SUBTLE);
        
        context.drawText(this.textRenderer, "BIND",
            x + 10, bindRowY + 9, TEXT_DIM, false);
        
        
        int btnX = x + 45;
        int btnY = bindRowY + 4;
        int btnW = 110;
        int btnH = 18;
        boolean btnHovered = !listeningForKey && mouseX >= btnX && mouseX <= btnX + btnW &&
                              mouseY >= btnY && mouseY <= btnY + btnH;

        int btnBg;
        String btnLabel;
        if (listeningForKey) {
            btnBg    = 0xFF2A0808;
            btnLabel = "Press a key...";
        } else if (module.getKeyBind() != -1) {
            btnBg    = btnHovered ? 0xFF1A3A5A : 0xFF0D2035;
            btnLabel = module.getKeyBindName();
        } else {
            btnBg    = btnHovered ? BUTTON_HOVER : BUTTON_BG;
            btnLabel = "None";
        }
        
        context.fill(btnX, btnY, btnX + btnW, btnY + btnH, btnBg);
        
        
        if (listeningForKey) {
            context.fill(btnX, btnY, btnX + 2, btnY + btnH, 0xFFCC2222);
        } else if (module.getKeyBind() != -1) {
            context.fill(btnX, btnY, btnX + 2, btnY + btnH, ACCENT);
        }
        
        int labelW = this.textRenderer.getWidth(btnLabel);
        context.drawText(this.textRenderer, btnLabel,
            btnX + (btnW - labelW) / 2, btnY + 5, TEXT_PRIMARY, false);
        
        
        if (listeningForKey) {
            context.drawText(this.textRenderer, "ESC to clear",
                btnX + btnW + 8, btnY + 5, 0xFF774444, false);
        } else {
            String hint = module.getKeyBind() != -1 ? "click to rebind" : "click to bind";
            context.drawText(this.textRenderer, hint,
                btnX + btnW + 8, btnY + 5, TEXT_DIM, false);
        }
        
        
        int contentX = x + 10;
        int contentY = bindRowY + 36;
        
        if (module instanceof ModuleSettingsRenderer) {
            ((ModuleSettingsRenderer) module).renderSettings(
                context, mouseX, mouseY, contentX, contentY, width - 20, settingsHelper);
        } else {
            int msgW = this.textRenderer.getWidth("No settings available");
            context.drawText(this.textRenderer, "No settings available",
                x + (width - msgW) / 2, contentY + 50, TEXT_DIM, false);
        }
        
        
        context.fill(x, y + height - 22, x + width, y + height - 21, BORDER_SUBTLE);
        context.fill(x, y + height - 21, x + width, y + height, BG_DARKER);
        
        
        int backX = x + 8;
        int backY = y + height - 17;
        int backW = 50;
        int backH = 14;
        boolean backHovered = mouseX >= backX && mouseX <= backX + backW &&
                              mouseY >= backY && mouseY <= backY + backH;
        context.fill(backX, backY, backX + backW, backY + backH,
            backHovered ? BUTTON_HOVER : BUTTON_BG);
        
        context.fill(backX, backY, backX + 2, backY + backH,
            backHovered ? ACCENT : BORDER_SUBTLE);
        context.drawText(this.textRenderer, "Back",
            backX + 8, backY + 3, TEXT_SECONDARY, false);
        
        
        String statusText  = module.isEnabled() ? "ON" : "OFF";
        int    statusColor = module.isEnabled() ? ENABLED_GLOW : TEXT_DIM;
        int    statusW     = this.textRenderer.getWidth(statusText);
        context.drawText(this.textRenderer, statusText,
            x + width - statusW - 8, y + height - 17, statusColor, false);
    }
    
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (listeningForKey && button == 0) {
            listeningForKey = false;
            return true;
        }
        
        if (button == 0) {
            
            int closeX = x + width - 18;
            int closeY = y + 8;
            if (mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + 30) {
                if (!(mouseX >= closeX && mouseX <= closeX + 12 &&
                      mouseY >= closeY && mouseY <= closeY + 14)) {
                    dragging = true;
                    dragOffsetX = (int) (mouseX - x);
                    dragOffsetY = (int) (mouseY - y);
                    return true;
                }
            }
            
            
            if (mouseX >= closeX && mouseX <= closeX + 12 &&
                mouseY >= closeY && mouseY <= closeY + 14) {
                this.close();
                return true;
            }
            
            
            int bindRowY = y + 30;
            int btnX = x + 45;
            int btnY = bindRowY + 4;
            if (mouseX >= btnX && mouseX <= btnX + 110 &&
                mouseY >= btnY && mouseY <= btnY + 18) {
                listeningForKey = true;
                return true;
            }
            
            
            int backX = x + 8;
            int backY = y + height - 17;
            if (mouseX >= backX && mouseX <= backX + 50 &&
                mouseY >= backY && mouseY <= backY + 14) {
                this.close();
                return true;
            }
            
            
            if (module instanceof ModuleSettingsRenderer) {
                int contentX = x + 10;
                int contentY = y + 30 + 36; 
                return ((ModuleSettingsRenderer) module).handleSettingsClick(
                    mouseX, mouseY, contentX, contentY, width - 20, settingsHelper);
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
    
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (listeningForKey) {
            listeningForKey = false;
            if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                module.setKeyBind(-1);
            } else {
                module.setKeyBind(keyCode);
            }
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            this.close();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
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
        if (button == 0) dragging = false;
        return super.mouseReleased(mouseX, mouseY, button);
    }
    
    @Override
    public void close() {
        listeningForKey = false;
        if (this.client != null) {
            this.client.setScreen(parent);
        }
    }
    
    @Override
    public boolean shouldPause() {
        return false;
    }
}