package play451.is.larping.mixin;

import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import play451.is.larping.features.modules.ModuleManager;
import play451.is.larping.features.modules.combat.StunSlam;

@Mixin(ClientPlayerInteractionManager.class)
public class StunSlamMixin {

    @Inject(method = "attackEntity", at = @At("HEAD"))
    private void onAttackEntity(PlayerEntity player, Entity target, CallbackInfo ci) {
        StunSlam module = (StunSlam) ModuleManager.getInstance().getModuleByName("StunSlam");
        if (module == null || !module.isEnabled()) return;
        if (!(target instanceof PlayerEntity targetPlayer)) return;

        module.doSwapToMace(targetPlayer);
    }
}