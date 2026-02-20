package play451.is.larping.features.modules.render;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.Perspective;
import play451.is.larping.features.modules.Module;
import play451.is.larping.features.modules.ModuleCategory;

public class Freelook extends Module {

    private final MinecraftClient mc = MinecraftClient.getInstance();
    private float lookYaw, lookPitch;
    private Perspective oldPerspective;

    public Freelook() {
        super("Freelook", "Look around without changing your movement direction", ModuleCategory.RENDER);
    }

    @Override
    public void onEnable() {
        if (mc.player == null) {
            this.setEnabled(false);
            return;
        }
        
        oldPerspective = mc.options.getPerspective();
        lookYaw = mc.player.getYaw();
        lookPitch = mc.player.getPitch();
        
        mc.options.setPerspective(Perspective.THIRD_PERSON_BACK);
    }

    @Override
    public void onDisable() {
        if (mc.options != null) {
            mc.options.setPerspective(oldPerspective);
        }
    }

    public float getLookYaw() {
        return lookYaw;
    }

    public float getLookPitch() {
        return lookPitch;
    }

    public void updateRotation(double deltaYaw, double deltaPitch) {
        this.lookYaw += (float) deltaYaw;
        this.lookPitch += (float) deltaPitch;
        this.lookPitch = Math.max(-90.0f, Math.min(90.0f, this.lookPitch));
    }
}