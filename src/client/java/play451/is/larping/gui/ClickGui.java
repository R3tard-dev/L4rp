package play451.is.larping.gui;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import play451.is.larping.module.Category;
import play451.is.larping.module.Module;
import play451.is.larping.module.ModuleManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClickGui extends Screen {

    private static final int PANEL_WIDTH   = 90;
    private static final int HEADER_HEIGHT = 13;
    private static final int ROW_HEIGHT    = 11;
    private static final int MAX_VISIBLE   = 12;
    private static final int START_Y       = 2;
    private static final int START_X_PAD  = 4;

    private static final int C_HEADER_BG      = 0xFF0F7070;
    private static final int C_HEADER_HOVER   = 0xFF128080;
    private static final int C_HEADER_TEXT    = 0xFFFFFFFF;
    private static final int C_MODULE_BG      = 0xEE0A0A0A;
    private static final int C_MODULE_HOVER   = 0xEE161616;
    private static final int C_MODULE_ENABLED = 0xEE0F5050;
    private static final int C_MODULE_TEXT    = 0xFFAAAAAA;
    private static final int C_MODULE_TEXT_ON = 0xFFFFFFFF;
    private static final int C_BORDER         = 0xFF0A4040;
    private static final int C_SEPARATOR      = 0xFF080808;
    private static final int C_SCROLLBAR_BG   = 0xFF080808;
    private static final int C_SCROLLBAR_FG   = 0xFF0F6060;
    private static final int C_OVERLAY        = 0x55000000;

    private static class Panel {
        int x, y;
        int scrollOffset = 0;
        boolean dragging = false;
        int dragOffX, dragOffY;
        Category category;

        Panel(Category category, int x, int y) {
            this.category = category;
            this.x = x;
            this.y = y;
        }
    }

    private Panel[] panels;

    public ClickGui() {
        super(Text.literal("ClickGUI"));
    }

    @Override
    protected void init() {
        Category[] cats = Category.values();
        panels = new Panel[cats.length];
        int x = START_X_PAD;
        for (int i = 0; i < cats.length; i++) {
            panels[i] = new Panel(cats[i], x, START_Y);
            x += PANEL_WIDTH + 3;
        }
    }

    private int getVisibleRows(Panel p) {
        List<Module> mods = ModuleManager.getModulesForCategory(p.category);
        return Math.min(mods.size(), MAX_VISIBLE);
    }

    private int getPanelBodyHeight(Panel p) {
        return getVisibleRows(p) * ROW_HEIGHT;
    }

    private int getPanelHeight(Panel p) {
        return HEADER_HEIGHT + getPanelBodyHeight(p);
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        ctx.fill(0, 0, this.width, this.height, C_OVERLAY);
        for (Panel p : panels) {
            renderPanel(ctx, mouseX, mouseY, p);
        }
        super.render(ctx, mouseX, mouseY, delta);
    }

    private void renderPanel(DrawContext ctx, int mouseX, int mouseY, Panel p) {
        List<Module> mods = ModuleManager.getModulesForCategory(p.category);
        int visibleRows = getVisibleRows(p);
        int bodyH = visibleRows * ROW_HEIGHT;
        int totalH = HEADER_HEIGHT + bodyH;
        boolean scrollable = mods.size() > MAX_VISIBLE;
        int scrollW = scrollable ? 3 : 0;

        ctx.fill(p.x - 1, p.y - 1, p.x + PANEL_WIDTH + 1, p.y + totalH + 1, C_BORDER);

        boolean headerHovered = mouseX >= p.x && mouseX < p.x + PANEL_WIDTH
                             && mouseY >= p.y && mouseY < p.y + HEADER_HEIGHT;
        ctx.fill(p.x, p.y, p.x + PANEL_WIDTH, p.y + HEADER_HEIGHT,
                 headerHovered ? C_HEADER_HOVER : C_HEADER_BG);
        ctx.drawCenteredTextWithShadow(textRenderer,
                Text.literal(p.category.getName()),
                p.x + PANEL_WIDTH / 2,
                p.y + (HEADER_HEIGHT - 8) / 2,
                C_HEADER_TEXT);

        int rowY = p.y + HEADER_HEIGHT;
        for (int i = p.scrollOffset; i < p.scrollOffset + visibleRows && i < mods.size(); i++) {
            Module mod = mods.get(i);
            boolean hovered = mouseX >= p.x && mouseX < p.x + PANEL_WIDTH - scrollW
                           && mouseY >= rowY && mouseY < rowY + ROW_HEIGHT;
            boolean enabled = mod.isEnabled();
            int bg = enabled ? C_MODULE_ENABLED : (hovered ? C_MODULE_HOVER : C_MODULE_BG);
            ctx.fill(p.x, rowY, p.x + PANEL_WIDTH - scrollW, rowY + ROW_HEIGHT, bg);
            ctx.fill(p.x, rowY + ROW_HEIGHT - 1, p.x + PANEL_WIDTH - scrollW, rowY + ROW_HEIGHT, C_SEPARATOR);
            ctx.drawTextWithShadow(textRenderer,
                    Text.literal(mod.getName()),
                    p.x + 3,
                    rowY + (ROW_HEIGHT - 8) / 2,
                    enabled ? C_MODULE_TEXT_ON : C_MODULE_TEXT);
            rowY += ROW_HEIGHT;
        }

        if (scrollable) {
            ctx.fill(p.x + PANEL_WIDTH - scrollW, p.y + HEADER_HEIGHT,
                     p.x + PANEL_WIDTH, p.y + HEADER_HEIGHT + bodyH, C_SCROLLBAR_BG);
            float ratio = (float) p.scrollOffset / Math.max(1, mods.size() - visibleRows);
            int barH = Math.max(4, (int)((float) visibleRows / mods.size() * bodyH));
            int barY = p.y + HEADER_HEIGHT + (int)(ratio * (bodyH - barH));
            ctx.fill(p.x + PANEL_WIDTH - scrollW, barY,
                     p.x + PANEL_WIDTH, barY + barH, C_SCROLLBAR_FG);
        }
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        for (int i = panels.length - 1; i >= 0; i--) {
            Panel p = panels[i];
            List<Module> mods = ModuleManager.getModulesForCategory(p.category);
            int totalH = getPanelHeight(p);

            if (mx >= p.x && mx < p.x + PANEL_WIDTH && my >= p.y && my < p.y + HEADER_HEIGHT) {
                if (button == 0) {
                    p.dragging = true;
                    p.dragOffX = (int) mx - p.x;
                    p.dragOffY = (int) my - p.y;
                    bringToFront(i);
                    return true;
                }
            }

            if (button == 0) {
                int rowY = p.y + HEADER_HEIGHT;
                for (int j = p.scrollOffset; j < p.scrollOffset + getVisibleRows(p) && j < mods.size(); j++) {
                    if (mx >= p.x && mx < p.x + PANEL_WIDTH && my >= rowY && my < rowY + ROW_HEIGHT) {
                        mods.get(j).toggle();
                        return true;
                    }
                    rowY += ROW_HEIGHT;
                }
            }
        }
        return super.mouseClicked(mx, my, button);
    }

    @Override
    public boolean mouseReleased(double mx, double my, int button) {
        if (button == 0) {
            for (Panel p : panels) p.dragging = false;
        }
        return super.mouseReleased(mx, my, button);
    }

    @Override
    public boolean mouseDragged(double mx, double my, int button, double dx, double dy) {
        for (Panel p : panels) {
            if (p.dragging && button == 0) {
                p.x = (int) mx - p.dragOffX;
                p.y = (int) my - p.dragOffY;
                return true;
            }
        }
        return super.mouseDragged(mx, my, button, dx, dy);
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double hx, double hy) {
        for (Panel p : panels) {
            List<Module> mods = ModuleManager.getModulesForCategory(p.category);
            int totalH = getPanelHeight(p);
            if (mx >= p.x && mx < p.x + PANEL_WIDTH
             && my >= p.y && my < p.y + totalH) {
                int maxScroll = Math.max(0, mods.size() - MAX_VISIBLE);
                p.scrollOffset = Math.max(0, Math.min(maxScroll, p.scrollOffset - (int) Math.signum(hy)));
                return true;
            }
        }
        return super.mouseScrolled(mx, my, hx, hy);
    }

    private void bringToFront(int index) {
        Panel p = panels[index];
        System.arraycopy(panels, index + 1, panels, index, panels.length - 1 - index);
        panels[panels.length - 1] = p;
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}