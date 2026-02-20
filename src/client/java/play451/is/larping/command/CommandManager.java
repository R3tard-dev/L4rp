package play451.is.larping.features.command;

import play451.is.larping.chat.ChatUtils;
import play451.is.larping.features.command.commands.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CommandManager {
    private static CommandManager INSTANCE;
    private final List<Command> commands = new ArrayList<>();
    private final String prefix = "+";

    public CommandManager() {
        commands.add(new HelpCommand());
        commands.add(new EnableCommand());
        commands.add(new BindCommand());
    }

    public static CommandManager getInstance() {
        if (INSTANCE == null) INSTANCE = new CommandManager();
        return INSTANCE;
    }

    public boolean onChat(String message) {
        if (!message.startsWith(prefix)) return false;

        String raw = message.substring(prefix.length());
        String[] split = raw.split(" ");
        String cmdName = split[0];
        String[] args = Arrays.copyOfRange(split, 1, split.length);

        for (Command c : commands) {
            if (c.getName().equalsIgnoreCase(cmdName)) {
                c.execute(args);
                return true;
            }
        }

        ChatUtils.error("Unknown command. Type " + prefix + "help for a list.");
        return true;
    }

    public List<Command> getCommands() { return commands; }
}