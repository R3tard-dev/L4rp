package play451.is.larping.gui.api;

import net.minecraft.client.gui.DrawContext;
import play451.is.larping.module.setting.Setting;

public class Button {
    protected final Setting<?> setting;
    protected final Frame      parent;
    protected int    x, y, height;
    protected final String description;

    private static final int PADDING      = 2;
    private static final int TEXT_PADDING = 4;

    public Button(Frame parent, int height, String description) {
        this.setting     = null;
        this.parent      = parent;
        this.height      = height;
        this.description = description;
    }

    public Button(Setting<?> setting, Frame parent, int height, String description) {
        this.setting     = setting;
        this.parent      = parent;
        this.height      = height;
        this.description = description;
    }

    public void render(DrawContext context, int mouseX, int mouseY, float delta) {}
    public void mouseClicked(double mouseX, double mouseY, int button) {}
    public void mouseReleased(double mouseX, double mouseY, int button) {}
    public void keyPressed(int keyCode, int scanCode, int modifiers) {}
    public void mouseScrolled(double mouseX, double mouseY, double hAmt, double vAmt) {}
    public void mouseDragged(double mouseX, double mouseY, int button, double dX, double dY) {}
    public void charTyped(char chr, int modifiers) {}

    public int   getWidth()       { return parent.getWidth(); }
    public int   getHeight()      { return height; }
    public int   getX()           { return x; }
    public int   getY()           { return y; }
    public void  setX(int x)      { this.x = x; }
    public void  setY(int y)      { this.y = y; }
    public int   getPadding()     { return PADDING; }
    public int   getTextPadding() { return TEXT_PADDING; }
    public Frame getParent()      { return parent; }

    public boolean isHovering(double mouseX, double mouseY) {
        return mouseX >= x + PADDING && mouseX < x + getWidth() - PADDING
            && mouseY >= y           && mouseY < y + height;
    }
}