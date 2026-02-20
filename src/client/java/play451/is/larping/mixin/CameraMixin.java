package play451.is.larping.mixin;

import net.minecraft.client.render.Camera;
import net.minecraft.entity.Entity;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import play451.is.larping.features.modules.render.Freelook;
import play451.is.larping.features.modules.ModuleManager;

@Mixin(Camera.class)
public abstract class CameraMixin {
    @Shadow private float yaw;
    @Shadow private float pitch;

    @Inject(method = "update", at = @At("RETURN"))
    private void onUpdate(BlockView area, Entity focusedEntity, boolean thirdPerson, boolean inverseView, float tickDelta, CallbackInfo ci) {
        Freelook freelook = (Freelook) ModuleManager.getInstance().getModuleByName("Freelook");
        
        if (freelook != null && freelook.isEnabled()) {
            this.setRotation(freelook.getLookYaw(), freelook.getLookPitch());
        }
    }

    @Shadow
    protected abstract void setRotation(float yaw, float pitch);
}