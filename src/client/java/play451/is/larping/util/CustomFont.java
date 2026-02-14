package play451.is.larping.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

import java.awt.*;
import java.io.InputStream;

public class CustomFont {
    private static Font beVietnamPro;
    private static boolean fontLoaded = false;
    
    public static void loadFont() {
        if (fontLoaded) return;
        
        try {
            
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            String[] fontNames = ge.getAvailableFontFamilyNames();
            boolean systemFontFound = false;
            
            for (String fontName : fontNames) {
                if (fontName.contains("Be Vietnam Pro")) {
                    beVietnamPro = new Font("Be Vietnam Pro", Font.PLAIN, 16);
                    systemFontFound = true;
                    break;
                }
            }
            
            
            if (!systemFontFound) {
                InputStream fontStream = CustomFont.class.getResourceAsStream("/assets/l4rp/fonts/BeVietnamPro-Regular.ttf");
                if (fontStream != null) {
                    beVietnamPro = Font.createFont(Font.TRUETYPE_FONT, fontStream);
                    ge.registerFont(beVietnamPro);
                    beVietnamPro = beVietnamPro.deriveFont(Font.PLAIN, 16f);
                } else {
                    
                    beVietnamPro = new Font("Arial", Font.PLAIN, 16);
                }
            }
            
            fontLoaded = true;
        } catch (Exception e) {
            e.printStackTrace();
            
            beVietnamPro = new Font("Arial", Font.PLAIN, 16);
            fontLoaded = true;
        }
    }
    
    public static Font getFont(int size) {
        if (!fontLoaded) loadFont();
        return beVietnamPro.deriveFont(Font.PLAIN, (float) size);
    }
    
    public static Font getBoldFont(int size) {
        if (!fontLoaded) loadFont();
        return beVietnamPro.deriveFont(Font.BOLD, (float) size);
    }
    
    
    public static void drawText(DrawContext context, String text, int x, int y, int color, boolean shadow) {
        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
        if (shadow) {
            context.drawTextWithShadow(textRenderer, text, x, y, color);
        } else {
            context.drawText(textRenderer, text, x, y, color, false);
        }
    }
}