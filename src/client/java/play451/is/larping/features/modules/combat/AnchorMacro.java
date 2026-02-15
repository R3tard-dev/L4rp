package play451.is.larping.features.modules.combat;

import net.minecraft.block.Blocks;
import net.minecraft.block.RespawnAnchorBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.item.ShieldItem;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.state.property.Property;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import org.lwjgl.glfw.GLFW;
import play451.is.larping.config.Config;
import play451.is.larping.features.modules.Module;
import play451.is.larping.features.modules.ModuleCategory;

import java.util.HashMap;
import java.util.Map;

public class AnchorMacro extends Module {
    
     
    private int switchDelay = 0;  
    private int glowstoneDelay = 0;  
    private int explodeDelay = 0;  
    private int totemSlot = 1;  
    
     
    private int switchCounter = 0;
    private int glowCounter = 0;
    private int explodeCounter = 0;
    private boolean placedGlowstone = false;
    private boolean explodedAnchor = false;
    private BlockHitResult storedHit = null;
    
    private final MinecraftClient mc = MinecraftClient.getInstance();
    
    public AnchorMacro() {
        super("AnchorMacro", "Automatically charges and explodes respawn anchors", ModuleCategory.COMBAT);
    }
    
    @Override
    public void onEnable() {
        reset();
    }
    
    @Override
    public void onDisable() {
        reset();
    }
    
    private void reset() {
        switchCounter = 0;
        glowCounter = 0;
        explodeCounter = 0;
        placedGlowstone = false;
        explodedAnchor = false;
        storedHit = null;
    }
    
    @Override
    public void onTick() {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (mc.currentScreen != null) return;
        if (isUsingShieldOrFood()) return;
        
         
        boolean rightClick = GLFW.glfwGetMouseButton(mc.getWindow().getHandle(), GLFW.GLFW_MOUSE_BUTTON_RIGHT) == GLFW.GLFW_PRESS;
        
        if (!rightClick) {
            placedGlowstone = false;
            explodedAnchor = false;
            storedHit = null;
            return;
        }
        
         
        if (!(mc.crosshairTarget instanceof BlockHitResult hit)) return;
        storedHit = hit;
        BlockPos pos = hit.getBlockPos();
        
         
        if (!isBlockAtPosition(pos, Blocks.RESPAWN_ANCHOR)) return;
        
         
        mc.options.useKey.setPressed(false);
        
         
        if (isRespawnAnchorUncharged(pos) && !placedGlowstone) {
            placeGlowstone();
        } 
         
        else if (isRespawnAnchorCharged(pos) && !explodedAnchor) {
            explodeAnchor();
        }
    }
    
    private boolean isUsingShieldOrFood() {
        boolean foodMain = mc.player.getMainHandStack().getItem().getComponents().contains(DataComponentTypes.FOOD);
        boolean foodOff = mc.player.getOffHandStack().getItem().getComponents().contains(DataComponentTypes.FOOD);
        boolean shieldMain = mc.player.getMainHandStack().getItem() instanceof ShieldItem;
        boolean shieldOff = mc.player.getOffHandStack().getItem() instanceof ShieldItem;
        boolean rightClick = GLFW.glfwGetMouseButton(mc.getWindow().getHandle(), GLFW.GLFW_MOUSE_BUTTON_RIGHT) == GLFW.GLFW_PRESS;
        return (foodMain || foodOff || shieldMain || shieldOff) && rightClick;
    }
    
    private void placeGlowstone() {
        Integer glowSlot = findHotbarItem(Items.GLOWSTONE);
        if (glowSlot == null) return;
        
        int prev = mc.player.getInventory().getSelectedSlot();
        
         
        if (!mc.player.getMainHandStack().isOf(Items.GLOWSTONE)) {
            if (switchCounter++ < switchDelay) return;
            switchCounter = 0;
            mc.player.getInventory().setSelectedSlot(glowSlot);
            mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(glowSlot));
            return;
        }
        
         
        if (glowCounter++ < glowstoneDelay) return;
        glowCounter = 0;
        
         
        if (storedHit != null) {
            mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, storedHit);
            mc.player.swingHand(Hand.MAIN_HAND);
        }
        
        placedGlowstone = true;
        
         
        mc.player.getInventory().setSelectedSlot(prev);
        mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(prev));
    }
    
    private void explodeAnchor() {
        int targetSlot = totemSlot - 1;  
        int prev = mc.player.getInventory().getSelectedSlot();
        
         
        if (prev != targetSlot) {
            if (switchCounter++ < switchDelay) return;
            switchCounter = 0;
            mc.player.getInventory().setSelectedSlot(targetSlot);
            mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(targetSlot));
            return;
        }
        
         
        if (explodeCounter++ < explodeDelay) return;
        explodeCounter = 0;
        
         
        if (storedHit != null) {
            mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, storedHit);
            mc.player.swingHand(Hand.MAIN_HAND);
        }
        
        explodedAnchor = true;
        
         
        mc.player.getInventory().setSelectedSlot(prev);
        mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(prev));
    }
    
    private Integer findHotbarItem(Item item) {
        for (int i = 0; i < 9; i++) {
            if (mc.player.getInventory().getStack(i).getItem() == item) {
                return i;
            }
        }
        return null;
    }
    
    private static boolean isBlockAtPosition(BlockPos blockPos, net.minecraft.block.Block block) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.world == null) return false;
        return mc.world.getBlockState(blockPos).isOf(block);
    }
    
    private static boolean isRespawnAnchorCharged(BlockPos blockPos) {
        MinecraftClient mc = MinecraftClient.getInstance();
        return isBlockAtPosition(blockPos, Blocks.RESPAWN_ANCHOR)
                && (int) mc.world.getBlockState(blockPos).get((Property) RespawnAnchorBlock.CHARGES) != 0;
    }
    
    private static boolean isRespawnAnchorUncharged(BlockPos blockPos) {
        MinecraftClient mc = MinecraftClient.getInstance();
        return isBlockAtPosition(blockPos, Blocks.RESPAWN_ANCHOR)
                && (int) mc.world.getBlockState(blockPos).get((Property) RespawnAnchorBlock.CHARGES) == 0;
    }
    
     
    @Override
    public Map<String, Object> saveSettings() {
        Map<String, Object> settings = new HashMap<>();
        settings.put("switchDelay", switchDelay);
        settings.put("glowstoneDelay", glowstoneDelay);
        settings.put("explodeDelay", explodeDelay);
        settings.put("totemSlot", totemSlot);
        return settings;
    }
    
    @Override
    public void loadSettings(Map<String, Object> settings) {
        if (settings.containsKey("switchDelay")) {
            switchDelay = ((Number) settings.get("switchDelay")).intValue();
        }
        if (settings.containsKey("glowstoneDelay")) {
            glowstoneDelay = ((Number) settings.get("glowstoneDelay")).intValue();
        }
        if (settings.containsKey("explodeDelay")) {
            explodeDelay = ((Number) settings.get("explodeDelay")).intValue();
        }
        if (settings.containsKey("totemSlot")) {
            totemSlot = ((Number) settings.get("totemSlot")).intValue();
        }
    }
    
     
    public void setSwitchDelay(int switchDelay) {
        this.switchDelay = Math.max(0, Math.min(20, switchDelay));
        Config.getInstance().saveModules();
    }
    
    public int getSwitchDelay() {
        return switchDelay;
    }
    
    public void setGlowstoneDelay(int glowstoneDelay) {
        this.glowstoneDelay = Math.max(0, Math.min(20, glowstoneDelay));
        Config.getInstance().saveModules();
    }
    
    public int getGlowstoneDelay() {
        return glowstoneDelay;
    }
    
    public void setExplodeDelay(int explodeDelay) {
        this.explodeDelay = Math.max(0, Math.min(20, explodeDelay));
        Config.getInstance().saveModules();
    }
    
    public int getExplodeDelay() {
        return explodeDelay;
    }
    
    public void setTotemSlot(int totemSlot) {
        this.totemSlot = Math.max(1, Math.min(9, totemSlot));
        Config.getInstance().saveModules();
    }
    
    public int getTotemSlot() {
        return totemSlot;
    }
}