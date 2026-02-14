package play451.is.larping;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import play451.is.larping.features.gui.GuiKeybind;
import play451.is.larping.features.modules.ModuleManager;
import play451.is.larping.features.modules.combat.*;
import play451.is.larping.features.modules.movement.*;

public class LarpClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        Larp.LOGGER.info("L4rp client initialized");
        
         
        GuiKeybind.register();
        
         
        new TriggerBot();
        new AimAssist();
        new ExampleMovementModule();
        
         
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player != null && client.world != null) {
                 
                ModuleManager.getInstance().onTick();
            }
        });
    }
}