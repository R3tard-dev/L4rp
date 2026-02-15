package play451.is.larping.features.modules.combat;

import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import play451.is.larping.config.Config;
import play451.is.larping.features.modules.Module;
import play451.is.larping.features.modules.ModuleCategory;
import play451.is.larping.features.modules.ModuleSettingsRenderer;
import play451.is.larping.features.modules.SettingsHelper;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class HitCrystal extends Module implements ModuleSettingsRenderer {
    
     
    private double delay = 30.0;  
    private String mode = "Single Tap";  
    private String placeMode = "Silent";  
    private boolean silent = false;
    private boolean perfectTiming = false;  
    private boolean pauseOnKill = false;  
    private boolean aimAssist = true;  
    
     
    private long lastActionTime = 0;
    private int progress = 0;
    private final Random random = new Random();
    
    private final MinecraftClient mc = MinecraftClient.getInstance();
    
    public HitCrystal() {
        super("HitCrystal", "Automatically places and hits end crystals", ModuleCategory.COMBAT);
    }
    
    @Override
    public void onEnable() {
        progress = 0;
        lastActionTime = 0;
    }
    
    @Override
    public void onDisable() {
        progress = 0;
    }
    
    @Override
    public void onTick() {
        if (mc.player == null || mc.world == null) return;
        if (mc.currentScreen != null || !mc.isWindowFocused()) return;
        
         
        if (pauseOnKill && mc.world.getPlayers().stream()
                .noneMatch(p -> p.isAlive() && !p.isSpectator())) {
            return;
        }
        
         
        PlayerEntity playerEntity = mc.world.getPlayers().stream()
                .filter(p -> p != mc.player && p.isAlive() && !p.isSpectator())
                .filter(p -> mc.player.squaredDistanceTo(p) <= 100)
                .findFirst()
                .orElse(null);
        
         
        switch (progress) {
            case 0:  
                Integer obsidianSlot = findHotbarItem(Items.OBSIDIAN);
                if (obsidianSlot != null) {
                    if (placeMode.equals("Manual")) {
                        mc.player.getInventory().setSelectedSlot(obsidianSlot);
                        mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(obsidianSlot));
                    }
                    nextProgress();
                }
                break;
                
            case 1:  
                if (mc.crosshairTarget instanceof BlockHitResult blockHit && 
                    !mc.world.getBlockState(blockHit.getBlockPos()).isAir()) {
                    
                    BlockPos placePos = blockHit.getBlockPos().add(
                        blockHit.getSide().getOffsetX(),
                        blockHit.getSide().getOffsetY(),
                        blockHit.getSide().getOffsetZ()
                    );
                    
                     
                    if (mc.world.getBlockState(blockHit.getBlockPos()).getBlock() == Blocks.OBSIDIAN) {
                        nextProgress();
                        break;
                    }
                    
                     
                    if (placeMode.equals("Manual")) {
                        Integer slot = findHotbarItem(Items.OBSIDIAN);
                        if (slot != null) {
                            mc.player.getInventory().setSelectedSlot(slot);
                            mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(slot));
                        }
                    }
                    
                     
                    if (aimAssist) {
                        smoothAimAt(new Vec3d(
                            placePos.getX() + 0.5,
                            placePos.getY() + 0.5,
                            placePos.getZ() + 0.5
                        ), 0.3f);
                    }
                    
                    mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, blockHit);
                    mc.player.swingHand(Hand.MAIN_HAND);
                    nextProgress();
                }
                break;
                
            case 2:  
                Integer crystalSlot = findHotbarItem(Items.END_CRYSTAL);
                if (crystalSlot != null) {
                    if (placeMode.equals("Manual")) {
                        mc.player.getInventory().setSelectedSlot(crystalSlot);
                        mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(crystalSlot));
                    }
                    nextProgress();
                }
                break;
                
            case 3:  
                if (mc.crosshairTarget instanceof BlockHitResult blockHit && 
                    mc.world.getBlockState(blockHit.getBlockPos()).getBlock() == Blocks.OBSIDIAN) {
                    
                     
                    if (placeMode.equals("Manual")) {
                        Integer slot = findHotbarItem(Items.END_CRYSTAL);
                        if (slot != null) {
                            mc.player.getInventory().setSelectedSlot(slot);
                            mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(slot));
                        }
                    }
                    
                     
                    if (aimAssist) {
                        smoothAimAt(new Vec3d(
                            blockHit.getBlockPos().getX() + 0.5,
                            blockHit.getBlockPos().getY() + 0.5,
                            blockHit.getBlockPos().getZ() + 0.5
                        ), 0.3f);
                    }
                    
                    mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, blockHit);
                    mc.player.swingHand(Hand.MAIN_HAND);
                    nextProgress();
                }
                break;
                
            case 4:  
            case 6:  
                 
                if (perfectTiming && playerEntity != null && playerEntity.hurtTime != 0) {
                    break;
                }
                handleCrystalHit();
                break;
                
            case 5:  
                if (mc.crosshairTarget instanceof BlockHitResult blockHit && 
                    mc.world.getBlockState(blockHit.getBlockPos()).getBlock() == Blocks.OBSIDIAN) {
                    
                    mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, blockHit);
                    mc.player.swingHand(Hand.MAIN_HAND);
                    nextProgress();
                }
                break;
        }
    }
    
    private void handleCrystalHit() {
        HitResult hit = mc.crosshairTarget;
        if (hit.getType() == HitResult.Type.ENTITY) {
            EntityHitResult entityHit = (EntityHitResult) hit;
            if (entityHit.getEntity() instanceof EndCrystalEntity) {
                 
                if (aimAssist) {
                    smoothAimAt(
                        entityHit.getEntity().getPos().add(0, entityHit.getEntity().getHeight() * 0.5, 0),
                        0.3f
                    );
                }
                
                 
                mc.interactionManager.attackEntity(mc.player, entityHit.getEntity());
                mc.player.swingHand(Hand.MAIN_HAND);
                
                 
                if (mode.equals("Double Tap")) {
                    nextProgress();
                } else {
                    progress = 0;  
                }
            }
        }
    }
    
    private void nextProgress() {
        progress++;
        lastActionTime = System.currentTimeMillis();
    }
    
    private Integer findHotbarItem(Item item) {
        for (int i = 0; i < 9; i++) {
            if (mc.player.getInventory().getStack(i).getItem() == item) {
                return i;
            }
        }
        return null;
    }
    
    private void smoothAimAt(Vec3d targetPos, float strength) {
        Vec3d eyePos = mc.player.getEyePos();
        Vec3d diff = targetPos.subtract(eyePos);
        double distXZ = Math.sqrt(diff.x * diff.x + diff.z * diff.z);
        
        float targetYaw = (float) (Math.toDegrees(Math.atan2(diff.z, diff.x)) - 90);
        float targetPitch = (float) (-Math.toDegrees(Math.atan2(diff.y, distXZ)));
        
        float yawDelta = MathHelper.wrapDegrees(targetYaw - mc.player.getYaw());
        float pitchDelta = targetPitch - mc.player.getPitch();
        
         
        float fraction = strength * (0.5f + random.nextFloat() * 0.5f);
        
         
        float smoothYaw = mc.player.getYaw() + yawDelta * fraction + (random.nextFloat() - 0.5f) * 0.5f;
        float smoothPitch = mc.player.getPitch() + pitchDelta * fraction + (random.nextFloat() - 0.5f) * 0.5f;
        
         
        mc.player.setYaw(smoothYaw);
        mc.player.setPitch(MathHelper.clamp(smoothPitch, -90f, 90f));
        mc.player.headYaw = smoothYaw;
        mc.player.bodyYaw = smoothYaw;
    }
    
     
    @Override
    public Map<String, Object> saveSettings() {
        Map<String, Object> settings = new HashMap<>();
        settings.put("delay", delay);
        settings.put("mode", mode);
        settings.put("placeMode", placeMode);
        settings.put("silent", silent);
        settings.put("perfectTiming", perfectTiming);
        settings.put("pauseOnKill", pauseOnKill);
        settings.put("aimAssist", aimAssist);
        return settings;
    }
    
    @Override
    public void loadSettings(Map<String, Object> settings) {
        if (settings.containsKey("delay")) {
            delay = ((Number) settings.get("delay")).doubleValue();
        }
        if (settings.containsKey("mode")) {
            mode = (String) settings.get("mode");
        }
        if (settings.containsKey("placeMode")) {
            placeMode = (String) settings.get("placeMode");
        }
        if (settings.containsKey("silent")) {
            silent = (Boolean) settings.get("silent");
        }
        if (settings.containsKey("perfectTiming")) {
            perfectTiming = (Boolean) settings.get("perfectTiming");
        }
        if (settings.containsKey("pauseOnKill")) {
            pauseOnKill = (Boolean) settings.get("pauseOnKill");
        }
        if (settings.containsKey("aimAssist")) {
            aimAssist = (Boolean) settings.get("aimAssist");
        }
    }
    
     
    public void setDelay(double delay) {
        this.delay = Math.max(0.0, Math.min(200.0, delay));
        Config.getInstance().saveModules();
    }
    
    public double getDelay() {
        return delay;
    }
    
    public void setMode(String mode) {
        if (mode.equals("None") || mode.equals("Single Tap") || mode.equals("Double Tap")) {
            this.mode = mode;
            Config.getInstance().saveModules();
        }
    }
    
    public String getMode() {
        return mode;
    }
    
    public void setPlaceMode(String placeMode) {
        if (placeMode.equals("Silent") || placeMode.equals("Manual")) {
            this.placeMode = placeMode;
            Config.getInstance().saveModules();
        }
    }
    
    public String getPlaceMode() {
        return placeMode;
    }
    
    public void setSilent(boolean silent) {
        this.silent = silent;
        Config.getInstance().saveModules();
    }
    
    public boolean isSilent() {
        return silent;
    }
    
    public void setPerfectTiming(boolean perfectTiming) {
        this.perfectTiming = perfectTiming;
        Config.getInstance().saveModules();
    }
    
    public boolean isPerfectTiming() {
        return perfectTiming;
    }
    
    public void setPauseOnKill(boolean pauseOnKill) {
        this.pauseOnKill = pauseOnKill;
        Config.getInstance().saveModules();
    }
    
    public boolean isPauseOnKill() {
        return pauseOnKill;
    }
    
    public void setAimAssist(boolean aimAssist) {
        this.aimAssist = aimAssist;
        Config.getInstance().saveModules();
    }
    
    public boolean isAimAssist() {
        return aimAssist;
    }
    
     
    @Override
    public int renderSettings(DrawContext context, int mouseX, int mouseY, int startX, int startY, int width, SettingsHelper helper) {
        int settingY = startY;
        
         
        helper.renderLabel(context, "Delay:", String.format("%.0f ms", delay), startX, settingY);
        helper.renderSlider(context, startX + 200, settingY, 150, delay, 0.0, 200.0);
        settingY += 35;
        
         
        helper.renderLabel(context, "Mode:", null, startX, settingY);
        helper.renderModeSelector(context, mouseX, mouseY, startX + 120, settingY, 
            new String[]{"None", "Single Tap", "Double Tap"}, mode, 75);
        settingY += 35;
        
         
        helper.renderLabel(context, "Place Mode:", null, startX, settingY);
        helper.renderModeSelector(context, mouseX, mouseY, startX + 150, settingY, 
            new String[]{"Silent", "Manual"}, placeMode, 60);
        settingY += 35;
        
         
        helper.renderLabel(context, "Perfect Timing:", null, startX, settingY);
        helper.renderToggle(context, mouseX, mouseY, startX + 150, settingY - 5, perfectTiming);
        settingY += 35;
        
         
        helper.renderLabel(context, "Pause On Kill:", null, startX, settingY);
        helper.renderToggle(context, mouseX, mouseY, startX + 150, settingY - 5, pauseOnKill);
        settingY += 35;
        
         
        helper.renderLabel(context, "Aim Assist:", null, startX, settingY);
        helper.renderToggle(context, mouseX, mouseY, startX + 150, settingY - 5, aimAssist);
        
        return settingY + 25;
    }
    
    @Override
    public boolean handleSettingsClick(double mouseX, double mouseY, int startX, int startY, int width, SettingsHelper helper) {
        int settingY = startY;
        
         
        if (helper.isSliderHovered(mouseX, mouseY, startX + 200, settingY, 150)) {
            double newValue = helper.calculateSliderValue(mouseX, startX + 200, 150, 0.0, 200.0);
            setDelay(newValue);
            return true;
        }
        settingY += 35;
        
         
        int modeX = startX + 120;
        for (String modeOption : new String[]{"None", "Single Tap", "Double Tap"}) {
            if (helper.isModeButtonHovered(mouseX, mouseY, modeX, settingY, 75)) {
                setMode(modeOption);
                return true;
            }
            modeX += 80;
        }
        settingY += 35;
        
         
        int placeModeX = startX + 150;
        for (String placeModeOption : new String[]{"Silent", "Manual"}) {
            if (helper.isModeButtonHovered(mouseX, mouseY, placeModeX, settingY, 60)) {
                setPlaceMode(placeModeOption);
                return true;
            }
            placeModeX += 65;
        }
        settingY += 35;
        
         
        if (helper.isToggleHovered(mouseX, mouseY, startX + 150, settingY - 5)) {
            setPerfectTiming(!perfectTiming);
            return true;
        }
        settingY += 35;
        
         
        if (helper.isToggleHovered(mouseX, mouseY, startX + 150, settingY - 5)) {
            setPauseOnKill(!pauseOnKill);
            return true;
        }
        settingY += 35;
        
         
        if (helper.isToggleHovered(mouseX, mouseY, startX + 150, settingY - 5)) {
            setAimAssist(!aimAssist);
            return true;
        }
        
        return false;
    }
}