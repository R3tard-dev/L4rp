package play451.is.larping.features.command.commands;

import play451.is.larping.chat.ChatUtils;
import play451.is.larping.features.command.Command;
import play451.is.larping.features.command.CommandManager;

public class HelpCommand extends Command {
    public HelpCommand() {
        super("help", "Shows all available commands", "help");
    }

    @Override
    public void execute(String[] args) {
        ChatUtils.info("Available Commands:");
        for (Command c : CommandManager.getInstance().getCommands()) {
            ChatUtils.info("§7+§f" + c.getName() + " §8- " + c.getDescription());
        }
    }
}