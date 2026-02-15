package play451.is.larping.features.modules;

import net.minecraft.client.gui.DrawContext;

 
public interface ModuleSettingsRenderer {
    
     
    int renderSettings(DrawContext context, int mouseX, int mouseY, int startX, int startY, int width, SettingsHelper settingsHelper);
    
     
    boolean handleSettingsClick(double mouseX, double mouseY, int startX, int startY, int width, SettingsHelper settingsHelper);
}