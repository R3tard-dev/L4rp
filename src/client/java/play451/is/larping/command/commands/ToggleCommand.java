package play451.is.larping.features.command.commands;

import play451.is.larping.chat.ChatUtils;
import play451.is.larping.features.command.Command;
import play451.is.larping.features.modules.Module;
import play451.is.larping.features.modules.ModuleManager;

public class ToggleCommand extends Command {
    public ToggleCommand() {
        super("toggle", "Toggles a module on or off", "toggle (module)");
    }

    @Override
    public void execute(String[] args) {
        if (args.length < 1) {
            ChatUtils.error("Usage: " + getSyntax());
            return;
        }

        Module m = ModuleManager.getInstance().getModuleByName(args[0]);
        if (m == null) {
            ChatUtils.error("Module not found: " + args[0]);
            return;
        }

        m.toggle();
    }
}