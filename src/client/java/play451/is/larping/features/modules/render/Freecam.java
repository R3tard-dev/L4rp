package play451.is.larping.features.modules.render;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import play451.is.larping.features.modules.Module;
import play451.is.larping.features.modules.ModuleCategory;
import play451.is.larping.features.modules.ModuleSettingsRenderer;
import play451.is.larping.features.modules.SettingsHelper;

import java.util.HashMap;
import java.util.Map;

public class Freecam extends Module implements ModuleSettingsRenderer {

    private final MinecraftClient mc = MinecraftClient.getInstance();

    private double horizontalSpeed = 1.0;
    private double verticalSpeed = 0.5;

    private float freeYaw, freePitch;
    private float prevFreeYaw, prevFreePitch;

    private double freeX, freeY, freeZ;
    private double prevFreeX, prevFreeY, prevFreeZ;

    public Freecam() {
        super("Freecam", "Allows independent camera movement", ModuleCategory.RENDER);
    }

    @Override
    public void onEnable() {
        if (mc.player == null || mc.world == null) {
            this.setEnabled(false);
            return;
        }

        mc.chunkCullingEnabled = false;

        freeYaw = prevFreeYaw = mc.player.getYaw();
        freePitch = prevFreePitch = mc.player.getPitch();

        freeX = prevFreeX = mc.player.getX();
        freeY = prevFreeY = mc.player.getEyeY();
        freeZ = prevFreeZ = mc.player.getZ();
    }

    @Override
    public void onDisable() {
        if (mc.player == null) return;
        mc.chunkCullingEnabled = true;
    }

    @Override
    public void onTick() {
        if (mc.player == null) return;

        prevFreeYaw = freeYaw;
        prevFreePitch = freePitch;
        prevFreeX = freeX;
        prevFreeY = freeY;
        prevFreeZ = freeZ;

        freeYaw = mc.player.getYaw();
        freePitch = mc.player.getPitch();

        double f = 0;
        double s = 0;
        
        if (mc.options.forwardKey.isPressed()) f++;
        if (mc.options.backKey.isPressed()) f--;
        if (mc.options.leftKey.isPressed()) s++;
        if (mc.options.rightKey.isPressed()) s--;
        
        if (f != 0 || s != 0) {
            double yawRad = Math.toRadians(freeYaw);
            double sin = Math.sin(yawRad);
            double cos = Math.cos(yawRad);
            
            freeX += (f * cos - s * sin) * horizontalSpeed * 0.2;
            freeZ += (f * sin + s * cos) * horizontalSpeed * 0.2;
        }

        if (mc.options.jumpKey.isPressed()) freeY += verticalSpeed * 0.2;
        if (mc.options.sneakKey.isPressed()) freeY -= verticalSpeed * 0.2;

        mc.player.setVelocity(Vec3d.ZERO);
    }

    public double getX(float tickDelta) { return MathHelper.lerp(tickDelta, prevFreeX, freeX); }
    public double getY(float tickDelta) { return MathHelper.lerp(tickDelta, prevFreeY, freeY); }
    public double getZ(float tickDelta) { return MathHelper.lerp(tickDelta, prevFreeZ, freeZ); }

    @Override
    public Map<String, Object> saveSettings() {
        Map<String, Object> settings = new HashMap<>();
        settings.put("hSpeed", horizontalSpeed);
        settings.put("vSpeed", verticalSpeed);
        return settings;
    }

    @Override
    public void loadSettings(Map<String, Object> settings) {
        if (settings.containsKey("hSpeed")) horizontalSpeed = ((Number) settings.get("hSpeed")).doubleValue();
        if (settings.containsKey("vSpeed")) verticalSpeed = ((Number) settings.get("vSpeed")).doubleValue();
    }

    @Override
    public int renderSettings(DrawContext context, int mouseX, int mouseY, int startX, int startY, int width, SettingsHelper helper) {
        int y = startY;
        helper.renderLabel(context, "H-Speed:", String.format("%.1f", horizontalSpeed), startX, y);
        helper.renderSlider(context, startX + 120, y, 150, horizontalSpeed, 0.1, 3.0);
        y += 35;
        helper.renderLabel(context, "V-Speed:", String.format("%.1f", verticalSpeed), startX, y);
        helper.renderSlider(context, startX + 120, y, 150, verticalSpeed, 0.1, 3.0);
        return y + 40;
    }

    @Override
    public boolean handleSettingsClick(double mouseX, double mouseY, int startX, int startY, int width, SettingsHelper helper) {
        if (helper.isSliderHovered(mouseX, mouseY, startX + 120, startY, 150)) {
            horizontalSpeed = helper.calculateSliderValue(mouseX, startX + 120, 150, 0.1, 3.0);
            return true;
        }
        if (helper.isSliderHovered(mouseX, mouseY, startX + 120, startY + 35, 150)) {
            verticalSpeed = helper.calculateSliderValue(mouseX, startX + 120, 150, 0.1, 3.0);
            return true;
        }
        return false;
    }
}