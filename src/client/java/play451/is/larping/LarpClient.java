package play451.is.larping;

import net.fabricmc.api.ClientModInitializer;
import play451.is.larping.features.gui.GuiKeybind;

public class LarpClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        Larp.LOGGER.info("L4rp client initialized");
        GuiKeybind.register();
    }
}