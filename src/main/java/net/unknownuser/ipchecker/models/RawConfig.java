package net.unknownuser.ipchecker.models;

import java.util.*;
import java.util.concurrent.*;

import org.javacord.api.*;
import org.javacord.api.entity.channel.*;
import org.javacord.api.entity.user.*;

import com.fasterxml.jackson.annotation.*;

public class RawConfig {
	@JsonProperty
	private List<Long> users;
	@JsonProperty
	private List<Long> channels;
	@JsonProperty(value = "server_name", defaultValue = "Server")
	private String	   serverName;
	
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
		
		return new SendConfig(textChannel, userChannels, serverName);
	}
}
