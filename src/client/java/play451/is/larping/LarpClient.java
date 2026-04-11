package play451.is.larping;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;
import play451.is.larping.gui.ClickGui;
import play451.is.larping.module.ModuleManager;

public class LarpClient implements ClientModInitializer {

    private static KeyBinding openGui;

    @Override
    public void onInitializeClient() {
        Larp.LOGGER.info("L4rp client initialized");

        ModuleManager.init();

        openGui = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.larp.clickgui",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_RIGHT_SHIFT,
            "category.larp"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openGui.wasPressed()) {
                client.setScreen(new ClickGui());
            }
        });
    }
}