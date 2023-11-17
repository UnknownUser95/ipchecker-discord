package net.unknownuser.ipchecker.models;

import java.util.*;

import org.javacord.api.entity.channel.*;
import org.javacord.api.entity.user.*;

public class SendConfig {
	private List<TextChannel> channels;
	private List<User>		  users;
	private String			  serverName;
	
	public SendConfig(List<TextChannel> channels, List<User> users, String serverName) {
		super();
		this.channels	= channels;
		this.users		= users;
		this.serverName	= serverName;
	}
	
	public List<TextChannel> getChannels() {
		return channels;
	}
	
	public List<User> getUsers() {
		return users;
	}
	
	public String getServerName() {
		return serverName;
	}
}
