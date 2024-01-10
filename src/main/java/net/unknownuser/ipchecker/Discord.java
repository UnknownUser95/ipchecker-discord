package net.unknownuser.ipchecker;

import java.awt.*;
import java.util.*;
import java.util.concurrent.*;

import org.javacord.api.*;
import org.javacord.api.entity.message.*;
import org.javacord.api.entity.message.embed.*;
import org.javacord.api.entity.permission.*;
import org.javacord.api.interaction.*;

import net.unknownuser.ipchecker.models.*;

public abstract class Discord {
	private Discord() {
		super();
	}
	
	private static SendConfig config = null;
	
	private static DiscordApi api;
	
	public static final String RECHECK_COMMAND_NAME = "ip-check";
	
	public static void init() {
		try {
			api = new DiscordApiBuilder().setToken(EnvArgs.API_TOKEN.get())
										 .login()
										 .join();
			
			addSlashCommand();
			
			Runtime.getRuntime()
				   .addShutdownHook(new Thread(() -> {
					   System.out.println("shutting down!");
					   api.disconnect();
				   }));
			
			System.out.println(api.createBotInvite(Permissions.fromBitmask(PermissionType.SEND_MESSAGES.getValue())));
		} catch(NoSuchElementException exc) {
			throw new InitFailedException("Could not get the channel. Is the bot invited to the server?", exc);
		} catch(CompletionException exc) {
			throw new InitFailedException("Could not log into Discord. Is the token correct?", exc);
		}
	}
	
	private static void addSlashCommand() {
		deleteOldSlashes();
		
		SlashCommand.with(RECHECK_COMMAND_NAME, "manually checks the server IP")
					.createGlobal(api)
					.join();
		
		api.addSlashCommandCreateListener(event -> {
			// ignore every other slash command
			SlashCommandInteraction interaction = event.getSlashCommandInteraction();
			if(!interaction.getCommandName()
						   .equals(RECHECK_COMMAND_NAME)) {
				return;
			}
			
			boolean hasUpdate = Main.checkUpdate();
			
			String message = hasUpdate ? "New IP found: " + IpChecker.getCurrentIp() : "IP has not changed";
			
			interaction.createImmediateResponder()
					   .setContent(message)
					   .setFlags(MessageFlag.EPHEMERAL)
					   .respond();
		});
	}
	
	private static void deleteOldSlashes() {
		Set<SlashCommand> set = api.getGlobalSlashCommands()
								   .join();
		set.stream()
		   .map(ApplicationCommand::delete)
		   .forEach(CompletableFuture::join);
	}
	
	public static void notifyNewIp(String ip) {
		EmbedBuilder embed = new EmbedBuilder().setTitle(String.format("New IP for %s", config.getServerName()))
											   .setDescription(ip)
											   .setColor(Color.RED);
		
		config.getChannels()
			  .forEach(channel -> channel.sendMessage(embed));
		config.getUsers()
			  .forEach(user -> user.sendMessage(embed));
	}
	
	public static void applyConfig(RawConfig raw) {
		config = raw.initialize(api);
	}
}
