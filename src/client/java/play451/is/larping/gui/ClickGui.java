package play451.is.larping.gui;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import play451.is.larping.module.Category;
import play451.is.larping.module.Module;
import play451.is.larping.module.ModuleManager;

import java.util.List;

public class ClickGui extends Screen {

    private static final int COLUMN_WIDTH = 150;
    private static final int HEADER_HEIGHT = 20;
    private static final int MODULE_HEIGHT = 18;
    private static final int COLUMN_GAP = 2;
    private static final int TITLE_BAR_HEIGHT = 22;
    private static final int MIN_LIST_HEIGHT = 200;
    private static final int TOP_OFFSET = 40;
    private static final int PADDING = 5;

    private static final int COLOR_OVERLAY = 0x88000000;
    private static final int COLOR_TITLE_BAR = 0xDD0A0A0A;
    private static final int COLOR_TITLE_TEXT = 0xFF00CCCC;
    private static final int COLOR_HEADER = 0xFF156060;
    private static final int COLOR_HEADER_TEXT = 0xFFFFFFFF;
    private static final int COLOR_MODULE_BG = 0xCC111111;
    private static final int COLOR_MODULE_ENABLED = 0xFF155555;
    private static final int COLOR_MODULE_HOVER = 0xFF1C1C1C;
    private static final int COLOR_MODULE_TEXT = 0xFFDDDDDD;
    private static final int COLOR_BORDER = 0xFF0D4444;

    private int guiX;
    private int guiY;
    private int guiWidth;
    private int guiHeight;

    public ClickGui() {
        super(Text.literal("ClickGUI"));
    }

    @Override
    protected void init() {
        Category[] categories = Category.values();
        guiWidth = categories.length * COLUMN_WIDTH + (categories.length - 1) * COLUMN_GAP;

        int maxModules = 0;
        for (Category cat : categories) {
            int count = ModuleManager.getModulesForCategory(cat).size();
            if (count > maxModules) maxModules = count;
        }

        int listHeight = Math.max(MIN_LIST_HEIGHT, maxModules * MODULE_HEIGHT);
        guiHeight = TITLE_BAR_HEIGHT + HEADER_HEIGHT + listHeight;

        guiX = (this.width - guiWidth) / 2;
        guiY = (this.height - guiHeight) / 2 + TOP_OFFSET;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        context.fill(0, 0, this.width, this.height, COLOR_OVERLAY);

        context.fill(guiX - 1, guiY - 1, guiX + guiWidth + 1, guiY + guiHeight + 1, COLOR_BORDER);

        context.fill(guiX, guiY, guiX + guiWidth, guiY + TITLE_BAR_HEIGHT, COLOR_TITLE_BAR);
        context.drawCenteredTextWithShadow(textRenderer, Text.literal("L4rp"), guiX + guiWidth / 2, guiY + 7, COLOR_TITLE_TEXT);

        Category[] categories = Category.values();
        int colX = guiX;
        for (Category cat : categories) {
            renderColumn(context, mouseX, mouseY, colX, guiY + TITLE_BAR_HEIGHT, cat);
            colX += COLUMN_WIDTH + COLUMN_GAP;
        }

        super.render(context, mouseX, mouseY, delta);
    }

    private void renderColumn(DrawContext context, int mouseX, int mouseY, int x, int y, Category category) {
        context.fill(x, y, x + COLUMN_WIDTH, y + HEADER_HEIGHT, COLOR_HEADER);
        context.drawCenteredTextWithShadow(textRenderer, Text.literal(category.getName()), x + COLUMN_WIDTH / 2, y + 6, COLOR_HEADER_TEXT);

        List<Module> modules = ModuleManager.getModulesForCategory(category);
        int listHeight = Math.max(MIN_LIST_HEIGHT, modules.size() * MODULE_HEIGHT);
        context.fill(x, y + HEADER_HEIGHT, x + COLUMN_WIDTH, y + HEADER_HEIGHT + listHeight, COLOR_MODULE_BG);

        int modY = y + HEADER_HEIGHT;
        for (Module module : modules) {
            boolean hovered = mouseX >= x && mouseX < x + COLUMN_WIDTH
                    && mouseY >= modY && mouseY < modY + MODULE_HEIGHT;
            int bg = module.isEnabled() ? COLOR_MODULE_ENABLED : (hovered ? COLOR_MODULE_HOVER : COLOR_MODULE_BG);
            context.fill(x, modY, x + COLUMN_WIDTH, modY + MODULE_HEIGHT, bg);
            context.fill(x, modY + MODULE_HEIGHT - 1, x + COLUMN_WIDTH, modY + MODULE_HEIGHT, 0xFF0A0A0A);
            context.drawTextWithShadow(textRenderer, Text.literal(module.getName()), x + PADDING, modY + 5, COLOR_MODULE_TEXT);
            modY += MODULE_HEIGHT;
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            Category[] categories = Category.values();
            int colX = guiX;
            for (Category cat : categories) {
                List<Module> modules = ModuleManager.getModulesForCategory(cat);
                int modY = guiY + TITLE_BAR_HEIGHT + HEADER_HEIGHT;
                for (Module module : modules) {
                    if (mouseX >= colX && mouseX < colX + COLUMN_WIDTH
                            && mouseY >= modY && mouseY < modY + MODULE_HEIGHT) {
                        module.toggle();
                        return true;
                    }
                    modY += MODULE_HEIGHT;
                }
                colX += COLUMN_WIDTH + COLUMN_GAP;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}