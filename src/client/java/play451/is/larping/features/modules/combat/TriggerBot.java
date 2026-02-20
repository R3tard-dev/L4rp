package play451.is.larping.features.modules.combat;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import play451.is.larping.config.Config;
import play451.is.larping.features.modules.Module;
import play451.is.larping.features.modules.ModuleCategory;
import play451.is.larping.features.modules.ModuleSettingsRenderer;
import play451.is.larping.features.modules.SettingsHelper;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class TriggerBot extends Module implements ModuleSettingsRenderer {
    
    private String mode = "1.9";  
    private double cps = 10.0;  
    private boolean blockhit = true;  
    private double cooldownProgress = 90.0;  
    private double hitRange = 3.0;  
    private boolean critTiming = false;  
    private boolean requireWeapon = true;
    private boolean attackPlayers = true;
    private boolean attackMobs = true;  
    
    private long lastHitTime = 0;
    private long nextDelay = 0;
    private long blockEndTime = 0;
    private int hitCounter = 0;
    private int hitsToBlock = 4;
    private boolean isBlocking = false;
    private final Random random = new Random();
    private final MinecraftClient mc = MinecraftClient.getInstance();
    
    public TriggerBot() {
        super("TriggerBot", "Its a triggerbot", ModuleCategory.COMBAT);
    }
    
    @Override
    public void onEnable() {
        lastHitTime = 0;
        hitCounter = 0;
        isBlocking = false;
        calculateNextDelay();
    }
    
    @Override
    public void onDisable() {
        if (isBlocking) {
            stopBlocking();
        }
    }
    
    @Override
    public void onTick() {
        if (mc.player == null || mc.world == null || mc.player.isDead()) return;
        
        if (isBlocking) {
            if (System.currentTimeMillis() >= blockEndTime) {
                stopBlocking();
            } else {
                return;  
            }
        }
        
        if (mc.player.isUsingItem()) return;
        
        if (requireWeapon) {
            var stack = mc.player.getMainHandStack();
            if (!stack.isIn(ItemTags.SWORDS) && !stack.isIn(ItemTags.AXES)) return;
        }
        
        if (mc.crosshairTarget instanceof EntityHitResult res
                && res.getType() == HitResult.Type.ENTITY
                && res.getEntity() instanceof LivingEntity target) {
            
            if (target == mc.player || !target.isAlive()) return;
            
            if (target instanceof PlayerEntity) {
                if (!attackPlayers || target.isSpectator()) return;
            } else if (!attackMobs) {
                return;
            }

            if (mc.player.squaredDistanceTo(target) > Math.pow(hitRange, 2)) return;
            
            if (mode.equals("1.9")) {
                if (mc.player.getAttackCooldownProgress(0.5f) < (float) cooldownProgress / 100f) return;
            }
            
            if (critTiming && (mc.player.isOnGround() || mc.player.fallDistance <= 0)) return;
            
            long now = System.currentTimeMillis();
            if (now - lastHitTime >= nextDelay) {
                mc.interactionManager.attackEntity(mc.player, target);
                mc.player.swingHand(Hand.MAIN_HAND);
                
                lastHitTime = now;
                hitCounter++;
                
                if (mode.equals("1.8") && blockhit && hitCounter >= hitsToBlock) {
                    startBlocking();
                } else {
                    calculateNextDelay();
                }
            }
        }
    }
    
    private void startBlocking() {
        isBlocking = true;
        hitCounter = 0;
        hitsToBlock = 4 + random.nextInt(2);  
        long blockDuration = 350 + random.nextInt(101);  
        blockEndTime = System.currentTimeMillis() + blockDuration;
        
        mc.options.useKey.setPressed(true);
        mc.interactionManager.interactItem(mc.player, Hand.OFF_HAND);
    }
    
    private void stopBlocking() {
        isBlocking = false;
        mc.options.useKey.setPressed(false);
        calculateNextDelay();
    }
    
    private void calculateNextDelay() {
        if (mode.equals("1.8")) {
            double randomCps = cps + (random.nextDouble() * 6.0) - 3.0;  
            nextDelay = (long) (1000.0 / Math.max(1.0, randomCps));
        } else {
            nextDelay = 10;  
        }
    }
    
    @Override
    public Map<String, Object> saveSettings() {
        Map<String, Object> settings = new HashMap<>();
        settings.put("mode", mode);
        settings.put("cps", cps);
        settings.put("blockhit", blockhit);
        settings.put("cooldownProgress", cooldownProgress);
        settings.put("hitRange", hitRange);
        settings.put("critTiming", critTiming);
        settings.put("requireWeapon", requireWeapon);
        settings.put("attackPlayers", attackPlayers);
        settings.put("attackMobs", attackMobs);
        return settings;
    }
    
    @Override
    public void loadSettings(Map<String, Object> settings) {
        if (settings.containsKey("mode")) mode = (String) settings.get("mode");
        if (settings.containsKey("cps")) cps = ((Number) settings.get("cps")).doubleValue();
        if (settings.containsKey("blockhit")) blockhit = (Boolean) settings.get("blockhit");
        if (settings.containsKey("cooldownProgress")) cooldownProgress = ((Number) settings.get("cooldownProgress")).doubleValue();
        if (settings.containsKey("hitRange")) hitRange = ((Number) settings.get("hitRange")).doubleValue();
        if (settings.containsKey("critTiming")) critTiming = (Boolean) settings.get("critTiming");
        if (settings.containsKey("requireWeapon")) requireWeapon = (Boolean) settings.get("requireWeapon");
        if (settings.containsKey("attackPlayers")) attackPlayers = (Boolean) settings.get("attackPlayers");
        if (settings.containsKey("attackMobs")) attackMobs = (Boolean) settings.get("attackMobs");
    }
    
    public void setMode(String mode) {
        this.mode = mode;
        calculateNextDelay();
        Config.getInstance().saveModules();
    }
    
    public String getMode() {
        return mode;
    }
    
    public void setCPS(double cps) {
        this.cps = cps;
        calculateNextDelay();
        Config.getInstance().saveModules();
    }
    
    public double getCPS() {
        return cps;
    }
    
    public void setBlockhit(boolean blockhit) {
        this.blockhit = blockhit;
        Config.getInstance().saveModules();
    }
    
    public boolean isBlockhit() {
        return blockhit;
    }
    
    public void setCooldownProgress(double cooldownProgress) {
        this.cooldownProgress = cooldownProgress;
        Config.getInstance().saveModules();
    }
    
    public double getCooldownProgress() {
        return cooldownProgress;
    }
    
    public void setHitRange(double hitRange) {
        this.hitRange = hitRange;
        Config.getInstance().saveModules();
    }
    
    public double getHitRange() {
        return hitRange;
    }
    
    public void setCritTiming(boolean critTiming) {
        this.critTiming = critTiming;
        Config.getInstance().saveModules();
    }
    
    public boolean isCritTiming() {
        return critTiming;
    }
    
    public void setRequireWeapon(boolean requireWeapon) {
        this.requireWeapon = requireWeapon;
        Config.getInstance().saveModules();
    }
    
    public boolean isRequireWeapon() {
        return requireWeapon;
    }

    public void setAttackPlayers(boolean attackPlayers) {
        this.attackPlayers = attackPlayers;
        Config.getInstance().saveModules();
    }

    public boolean isAttackPlayers() {
        return attackPlayers;
    }

    public void setAttackMobs(boolean attackMobs) {
        this.attackMobs = attackMobs;
        Config.getInstance().saveModules();
    }

    public boolean isAttackMobs() {
        return attackMobs;
    }
    
    @Override
    public int renderSettings(DrawContext context, int mouseX, int mouseY, int startX, int startY, int width, SettingsHelper helper) {
        int settingY = startY;
        
        helper.renderLabel(context, "Mode:", null, startX, settingY);
        helper.renderModeSelector(context, mouseX, mouseY, startX + 150, settingY, 
            new String[]{"1.8", "1.9"}, mode, 40);
        settingY += 35;
        
        if (mode.equals("1.8")) {
            helper.renderLabel(context, "CPS:", String.format("%.1f", cps), startX, settingY);
            helper.renderSlider(context, startX + 200, settingY, 150, cps, 1.0, 20.0);
            settingY += 35;
            
            helper.renderLabel(context, "Blockhit:", null, startX, settingY);
            helper.renderToggle(context, mouseX, mouseY, startX + 150, settingY - 5, blockhit);
            settingY += 35;
        }
        
        if (mode.equals("1.9")) {
            helper.renderLabel(context, "Cooldown %:", String.format("%.0f%%", cooldownProgress), startX, settingY);
            helper.renderSlider(context, startX + 220, settingY, 130, cooldownProgress, 0.0, 100.0);
            settingY += 35;
        }
        
        helper.renderLabel(context, "Hit Range:", String.format("%.1f", hitRange), startX, settingY);
        helper.renderSlider(context, startX + 200, settingY, 150, hitRange, 1.0, 7.0);
        settingY += 35;
        
        helper.renderLabel(context, "Crit Timing:", null, startX, settingY);
        helper.renderToggle(context, mouseX, mouseY, startX + 150, settingY - 5, critTiming);
        settingY += 35;
        
        helper.renderLabel(context, "Require Weapon:", null, startX, settingY);
        helper.renderToggle(context, mouseX, mouseY, startX + 150, settingY - 5, requireWeapon);
        settingY += 35;

        helper.renderLabel(context, "Players:", null, startX, settingY);
        helper.renderToggle(context, mouseX, mouseY, startX + 150, settingY - 5, attackPlayers);
        settingY += 35;

        helper.renderLabel(context, "Mobs:", null, startX, settingY);
        helper.renderToggle(context, mouseX, mouseY, startX + 150, settingY - 5, attackMobs);
        
        return settingY + 25;
    }
    
    @Override
    public boolean handleSettingsClick(double mouseX, double mouseY, int startX, int startY, int width, SettingsHelper helper) {
        int settingY = startY;
        
        int modeX = startX + 150;
        for (String modeOption : new String[]{"1.8", "1.9"}) {
            if (helper.isModeButtonHovered(mouseX, mouseY, modeX, settingY, 40)) {
                setMode(modeOption);
                return true;
            }
            modeX += 45;
        }
        settingY += 35;
        
        if (mode.equals("1.8")) {
            if (helper.isSliderHovered(mouseX, mouseY, startX + 200, settingY, 150)) {
                double newValue = helper.calculateSliderValue(mouseX, startX + 200, 150, 1.0, 20.0);
                setCPS(newValue);
                return true;
            }
            settingY += 35;
            
            if (helper.isToggleHovered(mouseX, mouseY, startX + 150, settingY - 5)) {
                setBlockhit(!blockhit);
                return true;
            }
            settingY += 35;
        }
        
        if (mode.equals("1.9")) {
            if (helper.isSliderHovered(mouseX, mouseY, startX + 220, settingY, 130)) {
                double newValue = helper.calculateSliderValue(mouseX, startX + 220, 130, 0.0, 100.0);
                setCooldownProgress(newValue);
                return true;
            }
            settingY += 35;
        }
        
        if (helper.isSliderHovered(mouseX, mouseY, startX + 200, settingY, 150)) {
            double newValue = helper.calculateSliderValue(mouseX, startX + 200, 150, 1.0, 7.0);
            setHitRange(newValue);
            return true;
        }
        settingY += 35;
        
        if (helper.isToggleHovered(mouseX, mouseY, startX + 150, settingY - 5)) {
            setCritTiming(!critTiming);
            return true;
        }
        settingY += 35;
        
        if (helper.isToggleHovered(mouseX, mouseY, startX + 150, settingY - 5)) {
            setRequireWeapon(!requireWeapon);
            return true;
        }
        settingY += 35;

        if (helper.isToggleHovered(mouseX, mouseY, startX + 150, settingY - 5)) {
            setAttackPlayers(!attackPlayers);
            return true;
        }
        settingY += 35;

        if (helper.isToggleHovered(mouseX, mouseY, startX + 150, settingY - 5)) {
            setAttackMobs(!attackMobs);
            return true;
        }
        
        return false;
    }
}