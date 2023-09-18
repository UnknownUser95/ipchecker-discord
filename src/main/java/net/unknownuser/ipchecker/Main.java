package net.unknownuser.ipchecker;

import java.time.*;
import java.util.*;

public class Main {
	
	public static void main(String[] args) {
		if(!Config.verifyAll().isEmpty()) {
			System.out.println("misconfiguration detected, fix the settings to launch!");
			System.exit(1);
		}
		
		Discord.init();
		IpChecker.init();
		
		new Thread(() -> Discord.notifyNewIp(IpChecker.getCurrentIp()), "initial message").start();
		
		LocalDateTime time = getNextHour();
		
		ZoneId	   zone		  = ZoneId.systemDefault();
		ZoneOffset zoneOffSet = zone.getRules().getOffset(time);
		
		TimerTask updateTask = new TimerTask() {
			@Override
			public void run() {
				checkUpdate();
			}
		};
		
		Timer timer = new Timer("IpChecker");
		timer.schedule(updateTask, Date.from(time.toInstant(zoneOffSet)), 1l * 60 * 60 * 1000);
		
		System.out.println("startup complete!");
	}
	
	private static LocalDateTime getNextHour() {
		LocalDateTime time = LocalDateTime.now();
		time = time.plusNanos(1000l - time.getNano());
		time = time.plusSeconds(60l - time.getSecond());
		time = time.plusMinutes(60l - time.getMinute());
		
		return time;
	}
	
	public static boolean checkUpdate() {
		Optional<String> newIp = IpChecker.checkForChange();
		if(newIp.isPresent()) {
			Discord.notifyNewIp(newIp.get());
		}
		return newIp.isPresent();
	}
}
