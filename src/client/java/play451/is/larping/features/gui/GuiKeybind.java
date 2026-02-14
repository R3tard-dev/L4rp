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
         
        openGuiKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.l4rp.opengui",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_RIGHT_SHIFT,
                "category.l4rp"
        ));

         
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (openGuiKey.wasPressed()) {
                MinecraftClient mc = MinecraftClient.getInstance();
                if (mc.currentScreen instanceof ClickGui) {
                     
                    mc.setScreen(null);
                } else {
                     
                    mc.setScreen(ClickGui.getInstance());
                }
            }
        });
    }
}