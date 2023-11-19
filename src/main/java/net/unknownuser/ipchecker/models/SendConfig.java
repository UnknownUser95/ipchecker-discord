package net.unknownuser.ipchecker.models;

import java.util.*;

import org.javacord.api.entity.channel.*;
import org.javacord.api.entity.user.*;

public class SendConfig {
	private List<TextChannel> channels;
	private List<User>		  users;
	private String			  serverName;
	private boolean			  notifyOnChange;
	private boolean			  notifyInital;
	
	public SendConfig(List<TextChannel> channels, List<User> users, String serverName, boolean notifyOnChange, boolean notifyInitial) {
		super();
		this.channels		= channels;
		this.users			= users;
		this.serverName		= serverName;
		this.notifyOnChange	= notifyOnChange;
		this.notifyInital	= notifyInitial;
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
	
	public boolean doNotifyOnChange() {
		return notifyOnChange;
	}
	
	public boolean doNotifyInitial() {
		return notifyInital;
	}
}
