package play451.is.larping.gui;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import play451.is.larping.module.Category;
import play451.is.larping.module.Module;
import play451.is.larping.module.ModuleManager;

import java.util.List;

public class ClickGui extends Screen {

    private static final int COLUMN_WIDTH    = 150;
    private static final int COLUMN_GAP      = 2;
    private static final int HEADER_HEIGHT   = 16;
    private static final int ROW_HEIGHT      = 14;
    private static final int TEXT_OFFSET_X   = 4;
    private static final int VERTICAL_OFFSET = 50;

    private static final int COL_HEADER_BG      = 0xFF145858;
    private static final int COL_HEADER_TEXT     = 0xFFFFFFFF;
    private static final int COL_MODULE_BG       = 0xFF0C0C0C;
    private static final int COL_MODULE_HOVER    = 0xFF1A1A1A;
    private static final int COL_MODULE_ENABLED  = 0xFF1A5555;
    private static final int COL_MODULE_TEXT     = 0xFFBBBBBB;
    private static final int COL_MODULE_TEXT_ON  = 0xFFFFFFFF;
    private static final int COL_BORDER          = 0xFF0A3A3A;
    private static final int COL_SEPARATOR       = 0xFF0A0A0A;
    private static final int COL_OVERLAY         = 0x80000000;

    private int guiX;
    private int guiY;

    public ClickGui() {
        super(Text.literal("ClickGUI"));
    }

    @Override
    protected void init() {
        Category[] cats = Category.values();
        int totalWidth = cats.length * COLUMN_WIDTH + (cats.length - 1) * COLUMN_GAP;
        guiX = (this.width - totalWidth) / 2;
        guiY = VERTICAL_OFFSET;
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        ctx.fill(0, 0, this.width, this.height, COL_OVERLAY);

        Category[] cats = Category.values();
        int x = guiX;
        for (Category cat : cats) {
            renderColumn(ctx, mouseX, mouseY, x, guiY, cat);
            x += COLUMN_WIDTH + COLUMN_GAP;
        }

        super.render(ctx, mouseX, mouseY, delta);
    }

    private void renderColumn(DrawContext ctx, int mouseX, int mouseY, int x, int y, Category cat) {
        List<Module> mods = ModuleManager.getModulesForCategory(cat);
        int colHeight = HEADER_HEIGHT + mods.size() * ROW_HEIGHT;

        ctx.fill(x - 1, y - 1, x + COLUMN_WIDTH + 1, y + colHeight + 1, COL_BORDER);

        ctx.fill(x, y, x + COLUMN_WIDTH, y + HEADER_HEIGHT, COL_HEADER_BG);

        ctx.drawCenteredTextWithShadow(
            textRenderer,
            Text.literal(cat.getName()),
            x + COLUMN_WIDTH / 2,
            y + (HEADER_HEIGHT - 8) / 2,
            COL_HEADER_TEXT
        );

        int rowY = y + HEADER_HEIGHT;
        for (Module mod : mods) {
            boolean hovered = mouseX >= x && mouseX < x + COLUMN_WIDTH
                           && mouseY >= rowY && mouseY < rowY + ROW_HEIGHT;
            boolean enabled = mod.isEnabled();

            int bg = enabled ? COL_MODULE_ENABLED : (hovered ? COL_MODULE_HOVER : COL_MODULE_BG);
            ctx.fill(x, rowY, x + COLUMN_WIDTH, rowY + ROW_HEIGHT, bg);

            ctx.fill(x, rowY + ROW_HEIGHT - 1, x + COLUMN_WIDTH, rowY + ROW_HEIGHT, COL_SEPARATOR);

            int textColor = enabled ? COL_MODULE_TEXT_ON : COL_MODULE_TEXT;
            ctx.drawTextWithShadow(
                textRenderer,
                Text.literal(mod.getName()),
                x + TEXT_OFFSET_X,
                rowY + (ROW_HEIGHT - 8) / 2,
                textColor
            );

            rowY += ROW_HEIGHT;
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            Category[] cats = Category.values();
            int x = guiX;
            for (Category cat : cats) {
                List<Module> mods = ModuleManager.getModulesForCategory(cat);
                int rowY = guiY + HEADER_HEIGHT;
                for (Module mod : mods) {
                    if (mouseX >= x && mouseX < x + COLUMN_WIDTH
                     && mouseY >= rowY && mouseY < rowY + ROW_HEIGHT) {
                        mod.toggle();
                        return true;
                    }
                    rowY += ROW_HEIGHT;
                }
                x += COLUMN_WIDTH + COLUMN_GAP;
            }
        }
        if (button == 1) {
            return false;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}