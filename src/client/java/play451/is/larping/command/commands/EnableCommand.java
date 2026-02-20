package play451.is.larping.features.command.commands;

import play451.is.larping.chat.ChatUtils;
import play451.is.larping.features.command.Command;
import play451.is.larping.features.modules.Module;
import play451.is.larping.features.modules.ModuleManager;

public class EnableCommand extends Command {
    public EnableCommand() {
        super("enable", "Enables a specific module", "enable (module)");
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

        if (!m.isEnabled()) {
            m.setEnabled(true);
        } else {
            ChatUtils.info(m.getName() + " is already enabled.");
        }
    }
}