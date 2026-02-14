package play451.is.larping.features.modules.combat;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import play451.is.larping.features.modules.Module;
import play451.is.larping.features.modules.ModuleCategory;

import java.util.Random;

public class TriggerBot extends Module {
    
     
    private String mode = "1.9";  
    private double cps = 10.0;  
    private boolean blockhit = true;  
    private double cooldownProgress = 90.0;  
    private double hitRange = 3.0;  
    private boolean critTiming = false;  
    private boolean requireWeapon = true;  
    
     
    private long lastHitTime = 0;
    private long nextDelay = 0;
    private long blockEndTime = 0;
    private int hitCounter = 0;
    private int hitsToBlock = 4;
    private boolean isBlocking = false;
    private final Random random = new Random();
    private final MinecraftClient mc = MinecraftClient.getInstance();
    
    public TriggerBot() {
        super("TriggerBot", "Auto attack when crosshair is on target", ModuleCategory.COMBAT);
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
                && res.getEntity() instanceof PlayerEntity target) {
            
             
            if (target == mc.player || !target.isAlive() || target.isSpectator()) return;
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
    
     
    
    public void setMode(String mode) {
        this.mode = mode;
        calculateNextDelay();
    }
    
    public String getMode() {
        return mode;
    }
    
    public void setCPS(double cps) {
        this.cps = cps;
        calculateNextDelay();
    }
    
    public double getCPS() {
        return cps;
    }
    
    public void setBlockhit(boolean blockhit) {
        this.blockhit = blockhit;
    }
    
    public boolean isBlockhit() {
        return blockhit;
    }
    
    public void setCooldownProgress(double cooldownProgress) {
        this.cooldownProgress = cooldownProgress;
    }
    
    public double getCooldownProgress() {
        return cooldownProgress;
    }
    
    public void setHitRange(double hitRange) {
        this.hitRange = hitRange;
    }
    
    public double getHitRange() {
        return hitRange;
    }
    
    public void setCritTiming(boolean critTiming) {
        this.critTiming = critTiming;
    }
    
    public boolean isCritTiming() {
        return critTiming;
    }
    
    public void setRequireWeapon(boolean requireWeapon) {
        this.requireWeapon = requireWeapon;
    }
    
    public boolean isRequireWeapon() {
        return requireWeapon;
    }
}