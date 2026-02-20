package play451.is.larping.chat;

import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

public class ChatUtils {

    public static final String PREFIX = "§9[L4rp.dev] ";

    public static void info(String message) {
        if (MinecraftClient.getInstance().player == null) return;
        
        MinecraftClient.getInstance().inGameHud.getChatHud().addMessage(
            Text.literal(PREFIX + "§f" + message)
        );
    }

    public static void error(String message) {
        if (MinecraftClient.getInstance().player == null) return;

        MinecraftClient.getInstance().inGameHud.getChatHud().addMessage(
            Text.literal(PREFIX + "§c" + message)
        );
    }
}