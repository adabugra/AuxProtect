package dev.heliosares.auxprotect.core.commands;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import dev.heliosares.auxprotect.adapters.SenderAdapter;
import dev.heliosares.auxprotect.core.APPermission;
import dev.heliosares.auxprotect.core.Command;
import dev.heliosares.auxprotect.core.CommandException;
import dev.heliosares.auxprotect.core.IAuxProtect;
import dev.heliosares.auxprotect.core.PlatformType;

public class APCommand extends Command {

	public APCommand(IAuxProtect plugin, String label, String... aliases) {
		super(plugin, label, APPermission.NONE, aliases);
	}

	private final ArrayList<Command> commands;
	{
		commands = new ArrayList<>();
		commands.add(new LookupCommand(plugin));
		commands.add(new PurgeCommand(plugin));
		commands.add(new HelpCommand(plugin, Collections.unmodifiableList(commands)));
		commands.add(new SQLCommand(plugin));
		commands.add(new TimeCommand(plugin));
		commands.add(new DumpCommand(plugin));
		if (plugin.getPlatform() == PlatformType.SPIGOT) {
			commands.add(new TpCommand(plugin).setTabComplete(false));
			commands.add(new InvCommand(plugin).setTabComplete(false));
			commands.add(new InventoryCommand(plugin));
			commands.add(new PlaytimeCommand(plugin));
			commands.add(new ActivityCommand(plugin));
			commands.add(new MoneyCommand(plugin));
			commands.add(new SaveInvCommand(plugin));
			if (plugin.getAPConfig().isPrivate()) {
				commands.add(new RetentionCommand(plugin));
				commands.add(new XrayCommand(plugin));
			}
		}
		if (plugin.getAPConfig().isPrivate()) {
			commands.add(new WatchCommand(plugin));
		}
	}

	@Override
	public void onCommand(SenderAdapter sender, String label, String[] args) throws CommandException {
		if (args.length > 0) {
			boolean match = false;
			for (Command c : commands) {
				if (!c.exists() || !c.matches(args[0])) {
					continue;
				}
				match = true;
				if (c.hasPermission(sender)) {
					try {
						c.onCommand(sender, label, args);
					} catch (CommandException.PlatformException ignored) {
						continue;
					} catch (CommandException.NotPlayerException e) {
						sender.sendLang("notplayererror");
					} catch (CommandException e) {
						if (e.getMessage() != null) {
							sender.sendMessageRaw(e.getMessage());
						} else {
							sender.sendLang("lookup-invalid-syntax");
						}
					} catch (Throwable t) {
						sender.sendLang("error");
					}
				} else {
					sender.sendLang("no-permission");
				}
				break;
			}
			if (match) {
				return;
			}
			if (args[0].equalsIgnoreCase("debug")) {
				if (!APPermission.ADMIN.hasPermission(sender)) {
					sender.sendLang("no-permission");
					return;
				}
				int verbosity = -1;
				if (args.length == 2) {
					try {
						verbosity = Integer.parseInt(args[1]);
					} catch (NumberFormatException e) {
					}
					if (verbosity < 0 || verbosity > 5) {
						sender.sendMessageRaw("�cInvalid verbosity level. /ap debug [0-5]"); // TODO lang
						return;
					}
				} else {
					if (plugin.getDebug() > 0) {
						verbosity = 0;
					} else {
						verbosity = 1;
					}
				}
				plugin.setDebug(verbosity);
				sender.sendMessageRaw("Debug " + (verbosity > 0 ? "�aenabled. �7Level: " + verbosity : "�cdisabled."));
				return;
			} else if (args[0].equalsIgnoreCase("info")) {
				sendInfo(sender);
				return;
			} else if (args[0].equalsIgnoreCase("reload")) {
				if (!APPermission.ADMIN.hasPermission(sender)) {
					sender.sendLang("no-permission");
					return;
				}
				// plugin.config.save();
				// plugin.saveConfig();
				plugin.reloadConfig();
				sender.sendMessageRaw("�aConfig reloaded");// TODO lang
				return;
			} else if (args[0].equalsIgnoreCase("backup")) {
				if (!APPermission.SQL.hasPermission(sender) || !sender.isConsole()) {
					sender.sendLang("no-permission");
					return;
				}
				if (plugin.getSqlManager().isMySQL()) {
					sender.sendLang("backup-sqliteonly");
					return;
				}
				plugin.runAsync(() -> {
					String backup = null;
					try {
						backup = plugin.getSqlManager().backup();
					} catch (IOException e) {
						plugin.print(e);
						return;
					}
					sender.sendMessageRaw("Backup created: " + backup); // TODO lang
				});
				return;
			} else {
				sender.sendLang("unknown-subcommand");
				return;
			}
		}
		sendInfo(sender);
		if (APPermission.HELP.hasPermission(sender)) {
			sender.sendMessageRaw("�7Do �9/ap help�7 for more info.");// TODO lang
		}
	}

	private void sendInfo(SenderAdapter sender) {
		sender.sendMessageRaw("�9AuxProtect"
				+ (APPermission.ADMIN.hasPermission(sender) ? (" �7v" + plugin.getPluginVersion()) : ""));
		sender.sendMessageRaw("�7Developed by �9Heliosares");
		if (APPermission.ADMIN.hasPermission(sender)) {
			sender.sendMessageRaw("�7�ohttps://www.spigotmc.org/resources/auxprotect.99147/");
		}
	}

	@Override
	public boolean exists() {
		return true;
	}

	@Override
	public List<String> onTabComplete(SenderAdapter sender, String label, String[] args) {
		List<String> out = new ArrayList<>();
		String currentArg = args[args.length - 1];

		if (args.length <= 1) {
			out.add("info");
			if (APPermission.ADMIN.hasPermission(sender)) {
				out.add("debug");
				out.add("reload");
				out.add("stats");
				if (sender.isConsole()) {
					out.add("sqli");
					out.add("sqlu");
				}
			}
		}

		if (args.length > 0) {
			for (Command c : commands) {
				if (!c.exists() || (!c.matches(args[0]) && args.length > 1) || !c.doTabComplete()) {
					continue;
				}
				if (c.hasPermission(sender)) {
					if (args.length == 1) {
						out.add(c.getLabel());
					} else {
						Collection<String> add = (c.onTabComplete(sender, label, args));
						if (add != null) {
							out.addAll(add);
						}
					}
				}
			}
		}
		final String currentArg_ = currentArg.toLowerCase();
		return out.stream().filter((s) -> s.toLowerCase().startsWith(currentArg_)).collect(Collectors.toList());
	}

	public static List<String> tabCompletePlayerAndTime(IAuxProtect plugin, SenderAdapter sender, String label,
			String[] args) {
		if (args.length == 2) {
			return allPlayers(plugin, true);
		} else if (args.length == 3) {
			String currentArg = args[args.length - 1];
			if (APPermission.INV.hasPermission(sender) && currentArg.matches("\\d+")) {
				List<String> out = new ArrayList<>();
				out.add(currentArg + "ms");
				out.add(currentArg + "s");
				out.add(currentArg + "m");
				out.add(currentArg + "h");
				out.add(currentArg + "d");
				out.add(currentArg + "w");
				return out;
			}
		}
		return null;
	}

	public static List<String> allPlayers(IAuxProtect plugin, boolean cache) {
		List<String> out = plugin.listPlayers();
		if (cache) {
			out.addAll(plugin.getSqlManager().getCachedUsernames());
		}
		return out;
	}
}