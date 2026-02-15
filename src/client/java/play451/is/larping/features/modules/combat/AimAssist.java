package play451.is.larping.features.modules.combat;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import play451.is.larping.config.Config;
import play451.is.larping.features.modules.Module;
import play451.is.larping.features.modules.ModuleCategory;
import play451.is.larping.features.modules.ModuleSettingsRenderer;
import play451.is.larping.features.modules.SettingsHelper;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class AimAssist extends Module implements ModuleSettingsRenderer {
    
     
    private boolean requireClicker = false;  
    private double range = 6.0;  
    private double strength = 0.1;  
    private double fov = 90.0;  
    private String aimMode = "Dynamic";  
    
     
    private final Map<PlayerEntity, Double> targetProgress = new HashMap<>();
    private double noiseOffset = Math.random() * 1000;
    private PlayerEntity currentTarget = null;
    private final MinecraftClient mc = MinecraftClient.getInstance();
    
    public AimAssist() {
        super("AimAssist", "Aimbot but looks legit", ModuleCategory.COMBAT);
    }
    
    @Override
    public void onEnable() {
        currentTarget = null;
        targetProgress.clear();
        noiseOffset = Math.random() * 1000;
    }
    
    @Override
    public void onDisable() {
        currentTarget = null;
        targetProgress.clear();
    }
    
    @Override
    public void onTick() {
        if (mc.player == null || mc.world == null || mc.player.isUsingItem()) return;
        
         
        if (requireClicker) {
             
             
        }
        
         
        PlayerEntity potentialTarget = mc.world.getPlayers()
                .stream()
                .filter(p -> p != mc.player && p.isAlive() && !p.isSpectator())
                .filter(p -> mc.player.squaredDistanceTo(p) <= Math.pow(range, 2))
                .filter(p -> Math.abs(MathHelper.wrapDegrees(getYawTo(p) - mc.player.getYaw())) <= fov / 2)
                .min(Comparator.comparingDouble(e -> mc.player.squaredDistanceTo(e)))
                .orElse(null);
        
        if (potentialTarget == null) {
            currentTarget = null;
            targetProgress.clear();
            return;
        }
        
         
        if (currentTarget != potentialTarget) {
            currentTarget = potentialTarget;
            targetProgress.put(currentTarget, 0.0);
        }
        
         
        Vec3d targetPos;
        if (aimMode.equals("Eyes")) {
            targetPos = currentTarget.getEyePos();
        } else {
             
            Box bb = currentTarget.getBoundingBox();
            targetPos = new Vec3d(
                    (bb.minX + bb.maxX) * 0.5 + (Math.random() - 0.5) * 0.2,
                    (bb.minY + bb.maxY) * 0.5 + 0.3 + (Math.random() - 0.5) * 0.15,
                    (bb.minZ + bb.maxZ) * 0.5 + (Math.random() - 0.5) * 0.2
            );
        }
        
        double t = targetProgress.getOrDefault(currentTarget, 0.0);
        Vec3d eyePos = mc.player.getEyePos();
        
         
        Vec3d midPoint = eyePos.add(targetPos).multiply(0.5);
        Vec3d controlPoint = midPoint.add(
                (perlinNoise(noiseOffset) - 0.5) * 0.4,
                (perlinNoise(noiseOffset + 1) - 0.5) * 0.2,
                (perlinNoise(noiseOffset + 2) - 0.5) * 0.4
        );
        noiseOffset += 0.05;
        
        Vec3d bezierPos = bezier(eyePos, controlPoint, targetPos, t);
        double dx = bezierPos.x - eyePos.x;
        double dy = bezierPos.y - eyePos.y;
        double dz = bezierPos.z - eyePos.z;
        double dist = Math.sqrt(dx * dx + dz * dz);
        
        float targetYaw = (float) (Math.toDegrees(Math.atan2(dz, dx)) - 90.0);
        float targetPitch = (float) (-Math.toDegrees(Math.atan2(dy, dist)));
        
        float yawDelta = MathHelper.wrapDegrees(targetYaw - mc.player.getYaw());
        float pitchDelta = targetPitch - mc.player.getPitch();
        
         
        float appliedStrength = (float) strength;
        if (aimMode.equals("Dynamic")) {
            double d = mc.player.squaredDistanceTo(currentTarget) / Math.pow(range, 2);
            appliedStrength *= 0.7 + 0.3 * easeOutCubic(1 - d);
        }
        
         
        float fraction = appliedStrength * (0.2f + (float)Math.random() * 0.3f);
        float smoothYaw = mc.player.getYaw() + yawDelta * fraction;
        float smoothPitch = mc.player.getPitch() + pitchDelta * fraction;
        
         
        smoothYaw += (perlinNoise(noiseOffset + 3) - 0.5) * 0.3;
        smoothPitch += (perlinNoise(noiseOffset + 4) - 0.5) * 0.3;
        
         
        mc.player.setYaw(smoothYaw);
        mc.player.setPitch(MathHelper.clamp(smoothPitch, -90.0f, 90.0f));
        mc.player.headYaw = smoothYaw;
        mc.player.bodyYaw = smoothYaw;
        
         
        t += fraction * (0.7 + Math.random() * 0.3);
        if (t > 1.0) {
            t = 1.0;
        }
        targetProgress.put(currentTarget, t);
    }
    
     
    private Vec3d bezier(Vec3d start, Vec3d control, Vec3d end, double t) {
        double u = 1 - t;
        double x = u * u * start.x + 2 * u * t * control.x + t * t * end.x;
        double y = u * u * start.y + 2 * u * t * control.y + t * t * end.y;
        double z = u * u * start.z + 2 * u * t * control.z + t * t * end.z;
        return new Vec3d(x, y, z);
    }
    
     
    private float easeOutCubic(double x) {
        double sign = Math.signum(x);
        double abs = Math.abs(x);
        return (float) (sign * (1 - Math.pow(1 - abs, 3)));
    }
    
     
    private float getYawTo(PlayerEntity entity) {
        Vec3d diff = entity.getPos().subtract(mc.player.getEyePos());
        return (float) (Math.toDegrees(Math.atan2(diff.z, diff.x)) - 90.0);
    }
    
     
    private double perlinNoise(double x) {
        return (Math.sin(x * 12.9898 + Math.cos(x * 78.233)) * 43758.5453) % 1;
    }
    
     
    @Override
    public Map<String, Object> saveSettings() {
        Map<String, Object> settings = new HashMap<>();
        settings.put("requireClicker", requireClicker);
        settings.put("range", range);
        settings.put("strength", strength);
        settings.put("fov", fov);
        settings.put("aimMode", aimMode);
        return settings;
    }
    
    @Override
    public void loadSettings(Map<String, Object> settings) {
        if (settings.containsKey("requireClicker")) requireClicker = (Boolean) settings.get("requireClicker");
        if (settings.containsKey("range")) range = ((Number) settings.get("range")).doubleValue();
        if (settings.containsKey("strength")) strength = ((Number) settings.get("strength")).doubleValue();
        if (settings.containsKey("fov")) fov = ((Number) settings.get("fov")).doubleValue();
        if (settings.containsKey("aimMode")) aimMode = (String) settings.get("aimMode");
    }
    
     
    public void setRequireClicker(boolean requireClicker) {
        this.requireClicker = requireClicker;
        Config.getInstance().saveModules();
    }
    
    public boolean isRequireClicker() {
        return requireClicker;
    }
    
    public void setRange(double range) {
        this.range = range;
        Config.getInstance().saveModules();
    }
    
    public double getRange() {
        return range;
    }
    
    public void setStrength(double strength) {
        this.strength = strength;
        Config.getInstance().saveModules();
    }
    
    public double getStrength() {
        return strength;
    }
    
    public void setFov(double fov) {
        this.fov = fov;
        Config.getInstance().saveModules();
    }
    
    public double getFov() {
        return fov;
    }
    
    public void setAimMode(String aimMode) {
        this.aimMode = aimMode;
        Config.getInstance().saveModules();
    }
    
    public String getAimMode() {
        return aimMode;
    }
    
     
    @Override
    public int renderSettings(DrawContext context, int mouseX, int mouseY, int startX, int startY, int width, SettingsHelper helper) {
        int settingY = startY;
        
         
        helper.renderLabel(context, "Range:", String.format("%.1f", range), startX, settingY);
        helper.renderSlider(context, startX + 200, settingY, 150, range, 1.0, 10.0);
        settingY += 35;
        
         
        helper.renderLabel(context, "Strength:", String.format("%.2f", strength), startX, settingY);
        helper.renderSlider(context, startX + 200, settingY, 150, strength, 0.01, 1.0);
        settingY += 35;
        
         
        helper.renderLabel(context, "FOV:", String.format("%.0f°", fov), startX, settingY);
        helper.renderSlider(context, startX + 200, settingY, 150, fov, 10.0, 360.0);
        settingY += 35;
        
         
        helper.renderLabel(context, "Aim Mode:", null, startX, settingY);
        helper.renderModeSelector(context, mouseX, mouseY, startX + 150, settingY, 
            new String[]{"Dynamic", "Eyes"}, aimMode, 60);
        settingY += 35;
        
         
        helper.renderLabel(context, "Require Clicker:", null, startX, settingY);
        helper.renderToggle(context, mouseX, mouseY, startX + 150, settingY - 5, requireClicker);
        
        return settingY + 25;
    }
    
    @Override
    public boolean handleSettingsClick(double mouseX, double mouseY, int startX, int startY, int width, SettingsHelper helper) {
        int settingY = startY;
        
         
        if (helper.isSliderHovered(mouseX, mouseY, startX + 200, settingY, 150)) {
            double newValue = helper.calculateSliderValue(mouseX, startX + 200, 150, 1.0, 10.0);
            setRange(newValue);
            return true;
        }
        settingY += 35;
        
         
        if (helper.isSliderHovered(mouseX, mouseY, startX + 200, settingY, 150)) {
            double newValue = helper.calculateSliderValue(mouseX, startX + 200, 150, 0.01, 1.0);
            setStrength(newValue);
            return true;
        }
        settingY += 35;
        
         
        if (helper.isSliderHovered(mouseX, mouseY, startX + 200, settingY, 150)) {
            double newValue = helper.calculateSliderValue(mouseX, startX + 200, 150, 10.0, 360.0);
            setFov(newValue);
            return true;
        }
        settingY += 35;
        
         
        int modeX = startX + 150;
        for (String mode : new String[]{"Dynamic", "Eyes"}) {
            if (helper.isModeButtonHovered(mouseX, mouseY, modeX, settingY, 60)) {
                setAimMode(mode);
                return true;
            }
            modeX += 65;
        }
        settingY += 35;
        
         
        if (helper.isToggleHovered(mouseX, mouseY, startX + 150, settingY - 5)) {
            setRequireClicker(!requireClicker);
            return true;
        }
        
        return false;
    }
}