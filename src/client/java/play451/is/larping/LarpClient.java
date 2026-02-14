package play451.is.larping;

import net.fabricmc.api.ClientModInitializer;
import play451.is.larping.features.gui.GuiKeybind;

import play451.is.larping.features.modules.combat.*;
import play451.is.larping.features.modules.movement.*;


public class LarpClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        Larp.LOGGER.info("L4rp client initialized");
        GuiKeybind.register();

        new ExampleCombatModule();
        new ExampleMovementModule();
    }
}