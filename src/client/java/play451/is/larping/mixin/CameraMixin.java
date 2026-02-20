package play451.is.larping.mixin;

import net.minecraft.client.render.Camera;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import play451.is.larping.features.modules.render.Freecam;
import play451.is.larping.features.modules.ModuleManager;

@Mixin(Camera.class)
public abstract class CameraMixin {

    @Inject(method = "update", at = @At("RETURN"))
    private void onUpdate(BlockView area, Entity focusedEntity, boolean thirdPerson, boolean inverseView, float tickDelta, CallbackInfo ci) {
        Freecam freecam = (Freecam) ModuleManager.getInstance().getModuleByName("Freecam");
        
        if (freecam != null && freecam.isEnabled()) {
            ((CameraAccessor) this).setPos(new Vec3d(
                freecam.getX(tickDelta), 
                freecam.getY(tickDelta), 
                freecam.getZ(tickDelta)
            ));
        }
    }
}