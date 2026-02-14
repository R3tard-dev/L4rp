package play451.is.larping.features.gui;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class GuiKeybind {
    private static KeyBinding openGuiKey;

    public static void register() {
        // Register RSHIFT keybind
        openGuiKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.l4rp.opengui",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_RIGHT_SHIFT,
                "category.l4rp"
        ));

        // Register tick event to check for key press
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (openGuiKey.wasPressed()) {
                MinecraftClient mc = MinecraftClient.getInstance();
                if (mc.currentScreen instanceof ClickGui) {
                    // Close GUI if already open
                    mc.setScreen(null);
                } else {
                    // Open GUI
                    mc.setScreen(ClickGui.getInstance());
                }
            }
        });
    }
}