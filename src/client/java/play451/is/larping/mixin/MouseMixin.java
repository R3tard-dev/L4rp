package play451.is.larping.mixin;

import net.minecraft.client.Mouse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import play451.is.larping.features.modules.render.Freelook;
import play451.is.larping.features.modules.ModuleManager;

@Mixin(Mouse.class)
public class MouseMixin {
    
    @Inject(
        method = "updateMouse", 
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;changeLookDirection(DD)V"), 
        locals = LocalCapture.CAPTURE_FAILEXCEPTION, 
        cancellable = true
    )
    private void onUpdateMouse(CallbackInfo ci, double d, double e, double f, double g, double h) {
        Freelook freelook = (Freelook) ModuleManager.getInstance().getModuleByName("Freelook");
        
        if (freelook != null && freelook.isEnabled()) {
            freelook.updateRotation(f, g);
            ci.cancel();
        }
    }
}