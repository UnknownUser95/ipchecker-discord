package net.unknownuser.ipchecker;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.*;

import org.javacord.api.*;
import org.javacord.api.entity.channel.*;
import org.javacord.api.entity.message.*;
import org.javacord.api.entity.message.embed.*;
import org.javacord.api.entity.permission.*;
import org.javacord.api.interaction.*;

public abstract class Discord {
	private Discord() {
		super();
	}
	
	private static DiscordApi  api;
	private static List<TextChannel> channels;
	
	public static void init() {
		try {
			api = new DiscordApiBuilder().setToken(Config.API_TOKEN.get()).login().join();
			addSlashCommand();
			
			Runtime.getRuntime().addShutdownHook(new Thread(() -> {
				System.out.println("shutting down!");
				api.disconnect();
			}));
			
			System.out.println(api.createBotInvite(Permissions.fromBitmask(PermissionType.SEND_MESSAGES.getValue())));
			channels = splitEnv().stream().map(id -> api.getTextChannelById(id).get()).toList();
		} catch(NoSuchElementException exc) {
			throw new InitFailedException("Could not get the channel. Is the bot invited to the server?", exc);
		} catch(CompletionException exc) {
			throw new InitFailedException("Could not log into Discord. Is the token correct?", exc);
		}
	}
	
	private static List<String> splitEnv() {
		return Arrays.asList(Config.RESPONSE_CHANNEL.get().split(","));
	}
	
	private static void addSlashCommand() {
		deleteOldSlashes();
		
		SlashCommand.with("ip-check", "manually checks the server IP").createGlobal(api).join();
		
		api.addSlashCommandCreateListener(event -> {
			boolean hasUpdate = Main.checkUpdate();
			
			String message = hasUpdate ? "New IP found: " + IpChecker.getCurrentIp() : "IP has not changed";
			
			event.getInteraction().createImmediateResponder().append(message).setFlags(MessageFlag.EPHEMERAL).respond();
		});
	}

	private static void deleteOldSlashes() {
		Set<SlashCommand> set = api.getGlobalSlashCommands().join();
		set.stream().map(c -> c.delete()).forEach(f -> f.join());
	}
	
	public static void notifyNewIp(String ip) {
		EmbedBuilder embed = new EmbedBuilder().setTitle("New Server IP").setDescription(ip).setColor(Color.RED);
		channels.forEach(channel -> channel.sendMessage(embed));
	}
}
