package net.unknownuser.ipchecker.models;

import java.util.*;
import java.util.concurrent.*;

import org.javacord.api.*;
import org.javacord.api.entity.channel.*;
import org.javacord.api.entity.user.*;

import com.fasterxml.jackson.annotation.*;

public class RawConfig {
	@JsonProperty(defaultValue = "[]")
	private List<Long> users;
	@JsonProperty(defaultValue = "[]")
	private List<Long> channels;
	@JsonProperty(value = "server_name", defaultValue = "Server")
	private String	   serverName;
	@JsonProperty(value = "notify_on_change", defaultValue = "false")
	private boolean	   notifyOnChange;
	@JsonProperty(value = "notify_initial", defaultValue = "true")
	private boolean	   notifyInitial;
	
	public SendConfig initialize(DiscordApi api) {
		List<User>		  userChannels = users.stream()
											  .map(api::getUserById)
											  .map(CompletableFuture::join)
											  .toList();
		List<TextChannel> textChannel  = channels.stream()
												 .map(api::getTextChannelById)
												 .filter(Optional::isPresent)
												 .map(Optional::get)
												 .toList();
		
		return new SendConfig(textChannel, userChannels, serverName, notifyOnChange, notifyInitial);
	}
	
	public boolean doNotifyOnChange() {
		return notifyOnChange;
	}
	
	public boolean doNotifyInitial() {
		return notifyInitial;
	}
}
