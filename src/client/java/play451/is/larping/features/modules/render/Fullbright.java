package play451.is.larping.features.modules.render;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import play451.is.larping.config.Config;
import play451.is.larping.features.modules.Module;
import play451.is.larping.features.modules.ModuleCategory;
import play451.is.larping.features.modules.ModuleSettingsRenderer;
import play451.is.larping.features.modules.SettingsHelper;

import java.util.HashMap;
import java.util.Map;

public class Fullbright extends Module implements ModuleSettingsRenderer {
    
     
    private String mode = "Gamma";  
    private double gammaLevel = 16.0;  
    
     
    private double previousGamma = 1.0;
    
    private final MinecraftClient mc = MinecraftClient.getInstance();
    
    public Fullbright() {
        super("Fullbright", "Makes everything bright", ModuleCategory.RENDER);
    }
    
    @Override
    public void onEnable() {
        if (mc.options == null) {
            this.toggle();
            return;
        }
        
         
        previousGamma = mc.options.getGamma().getValue();
        
         
        applyFullbright();
    }
    
    @Override
    public void onDisable() {
        if (mc.options != null) {
             
            try {
                ((play451.is.larping.ducks.ISimpleOption) (Object) mc.options.getGamma()).larping$setValue(previousGamma);
            } catch (Exception e) {
                mc.options.getGamma().setValue(previousGamma);
            }
        }
        
         
        if (mc.player != null && (mode.equals("Night Vision") || mode.equals("Both"))) {
            if (mc.player.hasStatusEffect(StatusEffects.NIGHT_VISION)) {
                StatusEffectInstance effect = mc.player.getStatusEffect(StatusEffects.NIGHT_VISION);
                 
                if (effect != null && effect.getDuration() == StatusEffectInstance.INFINITE) {
                    mc.player.removeStatusEffect(StatusEffects.NIGHT_VISION);
                }
            }
        }
    }
    
    @Override
    public void onTick() {
        if (mc.player == null || mc.world == null) return;
        
         
        applyFullbright();
    }
    
    private void applyFullbright() {
        if (mc.options == null) return;
        
         
        if (mode.equals("Gamma") || mode.equals("Both")) {
             
             
            try {
                 
                ((play451.is.larping.ducks.ISimpleOption) (Object) mc.options.getGamma()).larping$setValue(gammaLevel);
            } catch (Exception e) {
                 
                mc.options.getGamma().setValue(gammaLevel);
            }
        } else {
             
            try {
                ((play451.is.larping.ducks.ISimpleOption) (Object) mc.options.getGamma()).larping$setValue(previousGamma);
            } catch (Exception e) {
                mc.options.getGamma().setValue(previousGamma);
            }
        }
        
         
        if (mode.equals("Night Vision") || mode.equals("Both")) {
            if (mc.player != null) {
                 
                StatusEffectInstance existingEffect = mc.player.getStatusEffect(StatusEffects.NIGHT_VISION);
                
                 
                if (existingEffect == null || existingEffect.getDuration() < 400) {
                    mc.player.addStatusEffect(new StatusEffectInstance(
                        StatusEffects.NIGHT_VISION,
                        StatusEffectInstance.INFINITE,  
                        0,  
                        false,  
                        false,  
                        false   
                    ));
                }
            }
        } else {
             
            if (mc.player != null && mc.player.hasStatusEffect(StatusEffects.NIGHT_VISION)) {
                StatusEffectInstance effect = mc.player.getStatusEffect(StatusEffects.NIGHT_VISION);
                 
                if (effect != null && effect.getDuration() == StatusEffectInstance.INFINITE) {
                    mc.player.removeStatusEffect(StatusEffects.NIGHT_VISION);
                }
            }
        }
    }
    
     
    @Override
    public Map<String, Object> saveSettings() {
        Map<String, Object> settings = new HashMap<>();
        settings.put("mode", mode);
        settings.put("gammaLevel", gammaLevel);
        return settings;
    }
    
    @Override
    public void loadSettings(Map<String, Object> settings) {
        if (settings.containsKey("mode")) {
            mode = (String) settings.get("mode");
        }
        if (settings.containsKey("gammaLevel")) {
            gammaLevel = ((Number) settings.get("gammaLevel")).doubleValue();
        }
    }
    
     
    public void setMode(String mode) {
        if (mode.equals("Gamma") || mode.equals("Night Vision") || mode.equals("Both")) {
             
            if (this.isEnabled() && mc.player != null) {
                if (this.mode.equals("Night Vision") || this.mode.equals("Both")) {
                    mc.player.removeStatusEffect(StatusEffects.NIGHT_VISION);
                }
            }
            
            this.mode = mode;
            
             
            if (this.isEnabled()) {
                applyFullbright();
            }
            
            Config.getInstance().saveModules();
        }
    }
    
    public String getMode() {
        return mode;
    }
    
    public void setGammaLevel(double gammaLevel) {
        this.gammaLevel = Math.max(1.0, Math.min(100.0, gammaLevel));
        
         
        if (this.isEnabled() && (mode.equals("Gamma") || mode.equals("Both"))) {
            if (mc.options != null) {
                mc.options.getGamma().setValue(this.gammaLevel);
            }
        }
        
        Config.getInstance().saveModules();
    }
    
    public double getGammaLevel() {
        return gammaLevel;
    }
    
     
    @Override
    public int renderSettings(DrawContext context, int mouseX, int mouseY, int startX, int startY, int width, SettingsHelper helper) {
        int settingY = startY;
        
         
        helper.renderLabel(context, "Mode:", null, startX, settingY);
        helper.renderModeSelector(context, mouseX, mouseY, startX + 120, settingY, 
            new String[]{"Gamma", "Night Vision", "Both"}, mode, 85);
        settingY += 40;
        
         
        if (mode.equals("Gamma") || mode.equals("Both")) {
            helper.renderLabel(context, "Gamma Level:", String.format("%.1f", gammaLevel), startX, settingY);
            helper.renderSlider(context, startX + 180, settingY, 170, gammaLevel, 1.0, 100.0);
            settingY += 35;
        }
        
         
        helper.renderInfo(context, "Tip: Gamma works everywhere, Night Vision", startX, settingY + 10);
        helper.renderInfo(context, "is more natural looking.", startX, settingY + 20);
        
        return settingY + 30;
    }
    
    @Override
    public boolean handleSettingsClick(double mouseX, double mouseY, int startX, int startY, int width, SettingsHelper helper) {
        int settingY = startY;
        
         
        int modeX = startX + 120;
        for (String modeOption : new String[]{"Gamma", "Night Vision", "Both"}) {
            if (helper.isModeButtonHovered(mouseX, mouseY, modeX, settingY, 85)) {
                setMode(modeOption);
                return true;
            }
            modeX += 90;
        }
        settingY += 40;
        
         
        if (mode.equals("Gamma") || mode.equals("Both")) {
            if (helper.isSliderHovered(mouseX, mouseY, startX + 180, settingY, 170)) {
                double newValue = helper.calculateSliderValue(mouseX, startX + 180, 170, 1.0, 100.0);
                setGammaLevel(newValue);
                return true;
            }
        }
        
        return false;
    }
}