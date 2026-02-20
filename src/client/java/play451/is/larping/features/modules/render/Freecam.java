package play451.is.larping.features.modules.render;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.Perspective;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import play451.is.larping.features.modules.Module;
import play451.is.larping.features.modules.ModuleCategory;
import play451.is.larping.features.modules.ModuleSettingsRenderer;
import play451.is.larping.features.modules.SettingsHelper;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.OtherClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;

import java.util.HashMap;
import java.util.Map;

public class Freecam extends Module implements ModuleSettingsRenderer {

    private final MinecraftClient mc = MinecraftClient.getInstance();
    private Vec3d pos;
    private float yaw, pitch;
    private Perspective oldPerspective;
    private FreecamEntity dummy;
    private double speed = 1.0;

    public Freecam() {
        super("Freecam", "Detaches the camera from your body", ModuleCategory.RENDER);
    }

    @Override
    public void onEnable() {
        if (mc.player == null || mc.world == null) {
            this.setEnabled(false);
            return;
        }

        pos = mc.player.getPos();
        yaw = mc.player.getYaw();
        pitch = mc.player.getPitch();
        oldPerspective = mc.options.getPerspective();

        dummy = new FreecamEntity(mc.player);
        mc.world.addEntity(dummy);

        mc.player.noClip = true;
    }

    @Override
    public void onDisable() {
        if (mc.player == null) return;

        mc.player.noClip = false;
        mc.player.setPosition(pos);
        mc.player.setYaw(yaw);
        mc.player.setPitch(pitch);
        mc.player.setVelocity(Vec3d.ZERO);

        if (dummy != null && mc.world != null) {
            mc.world.removeEntity(dummy.getId(), Entity.RemovalReason.DISCARDED);
        }

        mc.options.setPerspective(oldPerspective);
    }

    @Override
    public void onTick() {
        if (mc.player == null) return;

        mc.player.getAbilities().flying = true;
        mc.player.getAbilities().setFlySpeed((float) (speed * 0.05));
        mc.player.setOnGround(false);
    }

    @Override
    public Map<String, Object> saveSettings() {
        Map<String, Object> settings = new HashMap<>();
        settings.put("speed", speed);
        return settings;
    }

    @Override
    public void loadSettings(Map<String, Object> settings) {
        if (settings.containsKey("speed")) {
            speed = ((Number) settings.get("speed")).doubleValue();
        }
    }

    @Override
    public int renderSettings(DrawContext context, int mouseX, int mouseY, int startX, int startY, int width, SettingsHelper helper) {
        int settingY = startY;
        helper.renderLabel(context, "Speed:", String.format("%.1f", speed), startX, settingY);
        helper.renderSlider(context, startX + 120, settingY, 150, speed, 0.1, 5.0);
        return settingY + 40;
    }

    @Override
    public boolean handleSettingsClick(double mouseX, double mouseY, int startX, int startY, int width, SettingsHelper helper) {
        if (helper.isSliderHovered(mouseX, mouseY, startX + 120, startY, 150)) {
            speed = helper.calculateSliderValue(mouseX, startX + 120, 150, 0.1, 5.0);
            return true;
        }
        return false;
    }
}

class FreecamEntity extends OtherClientPlayerEntity {
    public FreecamEntity(PlayerEntity player) {
        super(MinecraftClient.getInstance().world, player.getGameProfile());
        this.copyFrom(player);
        this.setPose(player.getPose());
        this.headYaw = player.headYaw;
        this.bodyYaw = player.bodyYaw;
        this.resetPosition();
    }

    @Override
    public boolean isSpectator() {
        return false;
    }

    @Override
    public boolean isCreative() {
        return false;
    }
}