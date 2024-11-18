package net.unknownuser.ipchecker.models;

import com.fasterxml.jackson.annotation.*;
import org.javacord.api.*;
import org.javacord.api.entity.channel.*;
import org.javacord.api.entity.user.*;

import java.util.*;
import java.util.concurrent.*;

public class RawConfig {
	@JsonProperty(defaultValue = "[]")
	private List<Long> users;
	@JsonProperty(defaultValue = "[]")
	private List<Long> channels;
	@JsonProperty(value = "server_name", defaultValue = "Server")
	private String     serverName;
	@JsonProperty(value = "notify_on_change", defaultValue = "false")
	private boolean    notifyOnChange;
	@JsonProperty(value = "notify_initial", defaultValue = "true")
	private boolean    notifyInitial;
	@JsonProperty(value = "use_ipv6", defaultValue = "true")
	private boolean    useIPv6;
	
	public SendConfig initialize(DiscordApi api) {
		List<User> userChannels = users.stream()
			.map(api::getUserById)
			.map(CompletableFuture::join)
			.toList();
		List<TextChannel> textChannel = channels.stream()
			.map(api::getTextChannelById)
			.filter(Optional::isPresent)
			.map(Optional::get)
			.toList();
		
		if (System.getProperty("java.net.preferIPv6Addresses") == null) {
			System.out.println("using IPv4 addresses by default. Use '-Djava.net.preferIPv6Addresses=true' to use IPv6 addresses");
		} else if (System.getProperty("java.net.preferIPv6Addresses").equals("true")) {
			System.out.println("using IPv6");
		} else {
			System.out.println("using IPv4 (or system default)");
		}
		
		return new SendConfig(textChannel, userChannels, serverName, notifyOnChange, notifyInitial);
	}
	
	public boolean doNotifyOnChange() {
		return notifyOnChange;
	}
	
	public boolean doNotifyInitial() {
		return notifyInitial;
	}
}
