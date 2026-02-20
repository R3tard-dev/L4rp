package play451.is.larping.features.command.commands;

import org.lwjgl.glfw.GLFW;
import play451.is.larping.chat.ChatUtils;
import play451.is.larping.features.command.Command;
import play451.is.larping.features.modules.Module;
import play451.is.larping.features.modules.ModuleManager;

public class BindCommand extends Command {
    public BindCommand() {
        super("bind", "Binds a module to a key", "bind (module) (key)");
    }

    @Override
    public void execute(String[] args) {
        if (args.length < 2) {
            ChatUtils.error("Usage: " + getSyntax());
            return;
        }

        Module m = ModuleManager.getInstance().getModuleByName(args[0]);
        if (m == null) {
            ChatUtils.error("Module not found: " + args[0]);
            return;
        }

        String keyName = args[1].toUpperCase();
        int keyCode = -1;

        if (keyName.equalsIgnoreCase("none")) {
            keyCode = -1;
        } else {
            // Very simple key finding logic
            for (int i = 0; i < 348; i++) {
                String name = GLFW.glfwGetKeyName(i, 0);
                if (name != null && name.equalsIgnoreCase(keyName)) {
                    keyCode = i;
                    break;
                }
            }
        }

        if (keyCode == -1 && !keyName.equalsIgnoreCase("none")) {
            ChatUtils.error("Invalid key: " + keyName);
        } else {
            m.setKeyBind(keyCode);
            ChatUtils.info("Bound §b" + m.getName() + "§f to §b" + keyName);
        }
    }
}