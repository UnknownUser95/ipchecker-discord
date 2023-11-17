package net.unknownuser.ipchecker;

import java.io.*;
import java.nio.file.*;
import java.time.*;
import java.util.*;

import com.fasterxml.jackson.databind.*;

import net.unknownuser.ipchecker.models.*;

public class Main {
	
	public static final File configFile = new File("config.json");
	
	public static void main(String[] args) {
		if(!EnvArgs.verifyAll()
				   .isEmpty()) {
			System.out.println("misconfiguration detected, fix the settings to launch!");
			System.exit(1);
		}
		
		if(!configFile.exists()) {
			System.out.println("config file is missing!");
			System.exit(2);
		}
		
		Optional<RawConfig> config = readConfig();
		
		if(config.isEmpty()) {
			System.out.println("config file is broken!");
			System.exit(3);
		}
		
		Discord.init();
		IpChecker.init();
		
		Discord.applyConfig(config.get());
		
		new Thread(Main::watchConfigFile, "config watcher").start();
		new Thread(() -> Discord.notifyNewIp(IpChecker.getCurrentIp()), "initial message").start();
		
		scheduleCheck();
		
		System.out.println("startup complete!");
	}
	
	private static void watchConfigFile() {
		System.out.println("watcher started!");
		try (WatchService watchService = FileSystems.getDefault()
													.newWatchService()) {
			Path.of(".")
				.register(watchService, StandardWatchEventKinds.ENTRY_MODIFY);
			
			WatchKey key;
			while((key = watchService.take()) != null) {
				
				for(WatchEvent<?> event : key.pollEvents()) {
					Path path = (Path) event.context();
					
					if(!path.equals(configFile.toPath())) {
						continue;
					}
					
					System.out.println("reloading config...");
					
					var cfg = readConfig();
					
					if(cfg.isPresent()) {
						Discord.applyConfig(cfg.get());
						Discord.notifyNewIp(IpChecker.getCurrentIp());
					}
				}
				
				key.reset();
			}
		} catch(IOException e) {
			throw new RuntimeException(e);
		} catch(InterruptedException e) {}
	}
	
	private static Optional<RawConfig> readConfig() {
		ObjectMapper mapper = new ObjectMapper();
		try {
			RawConfig rawConfig = mapper.readValue(configFile, RawConfig.class);
			return Optional.of(rawConfig);
		} catch(IOException e) {
			System.out.println(e.getMessage());
			return Optional.empty();
		}
	}
	
	private static void scheduleCheck() {
		LocalDateTime time = getNextHour();
		
		ZoneId	   zone		  = ZoneId.systemDefault();
		ZoneOffset zoneOffSet = zone.getRules()
									.getOffset(time);
		
		TimerTask updateTask = new TimerTask() {
			@Override
			public void run() {
				checkUpdate();
			}
		};
		
		Timer timer = new Timer("IpChecker");
		timer.schedule(updateTask, Date.from(time.toInstant(zoneOffSet)), 1l * 60 * 60 * 1000);
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
