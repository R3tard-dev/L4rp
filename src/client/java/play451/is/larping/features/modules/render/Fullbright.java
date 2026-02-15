package play451.is.larping.features.modules.render;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import play451.is.larping.config.Config;
import play451.is.larping.features.modules.Module;
import play451.is.larping.features.modules.ModuleCategory;

import java.util.HashMap;
import java.util.Map;

public class Fullbright extends Module {
    
     
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
             
            mc.options.getGamma().setValue(previousGamma);
        }
        
         
        if (mc.player != null && (mode.equals("Night Vision") || mode.equals("Both"))) {
            mc.player.removeStatusEffect(StatusEffects.NIGHT_VISION);
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
            mc.options.getGamma().setValue(gammaLevel);
        }
        
         
        if (mode.equals("Night Vision") || mode.equals("Both")) {
            if (mc.player != null) {
                 
                StatusEffectInstance existingEffect = mc.player.getStatusEffect(StatusEffects.NIGHT_VISION);
                
                 
                if (existingEffect == null || existingEffect.getDuration() < 400) {
                    mc.player.addStatusEffect(new StatusEffectInstance(
                        StatusEffects.NIGHT_VISION,
                        Integer.MAX_VALUE,  
                        0,  
                        false,  
                        false,  
                        false   
                    ));
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
}