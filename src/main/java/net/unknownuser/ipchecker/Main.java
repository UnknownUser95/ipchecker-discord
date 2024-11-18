package net.unknownuser.ipchecker;

import com.fasterxml.jackson.databind.*;
import net.unknownuser.ipchecker.models.*;

import java.io.*;
import java.nio.file.*;
import java.time.*;
import java.util.*;

public class Main {
	
	public static final File CONFIG_FILE = new File("config.json");
	public static final File IP_FILE     = new File("ip.txt");
	
	public static void main(String[] args) {
		Optional<RawConfig> cfg = verifyEnvironment();
		
		Discord.init();
		IpChecker.init();
		
		RawConfig config = cfg.get();
		
		Discord.applyConfig(config);
		
		new Thread(Main::watchConfigFile, "config-watcher").start();
		
		boolean notify = config.doNotifyInitial();
		
		if (ipHasChanged()) {
			notify = true;
			IpChecker.writeCurrentIp();
		}
		
		if (notify) {
			new Thread(() -> Discord.notifyNewIp(IpChecker.getCurrentIp()), "initial-message").start();
		}
		
		scheduleCheck();
		
		System.out.println("startup complete!");
	}
	
	private static boolean ipHasChanged() {
		String           currentIp = IpChecker.getCurrentIp();
		Optional<String> lastIp    = IpChecker.getSavedIp();
		
		return !(lastIp.isPresent() && lastIp.get()
			.equals(currentIp));
	}
	
	private static Optional<RawConfig> verifyEnvironment() {
		if (!EnvArgs.verifyAll()
			.isEmpty()) {
			System.out.println("misconfiguration detected, fix the settings to launch!");
			System.exit(1);
		}
		
		if (!CONFIG_FILE.exists()) {
			System.out.println("config file is missing!");
			System.exit(2);
		}
		
		Optional<RawConfig> cfg = readConfig();
		
		if (cfg.isEmpty()) {
			System.out.println("config file is broken!");
			System.exit(3);
		}
		return cfg;
	}
	
	private static void watchConfigFile() {
		System.out.println("watcher started!");
		try (WatchService watchService = FileSystems.getDefault()
			.newWatchService()) {
			Path.of(".")
				.register(watchService, StandardWatchEventKinds.ENTRY_MODIFY);
			
			WatchKey key;
			while ((key = watchService.take()) != null) {
				
				boolean updateDone = false;
				
				for (WatchEvent<?> event : key.pollEvents()) {
					Path path = (Path) event.context();
					
					if (updateDone || !path.equals(CONFIG_FILE.toPath())) {
						continue;
					}
					
					updateDone = true;
					System.out.println("reloading config...");
					
					var cfg = readConfig();
					
					if (cfg.isPresent()) {
						RawConfig config = cfg.get();
						
						Discord.applyConfig(config);
						
						if (config.doNotifyOnChange()) {
							Discord.notifyNewIp(IpChecker.getCurrentIp());
						}
					}
				}
				
				key.reset();
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		} catch (InterruptedException e) {
		}
	}
	
	private static Optional<RawConfig> readConfig() {
		ObjectMapper mapper = new ObjectMapper();
		try {
			RawConfig rawConfig = mapper.readValue(CONFIG_FILE, RawConfig.class);
			return Optional.of(rawConfig);
		} catch (IOException e) {
			System.out.println(e.getMessage());
			return Optional.empty();
		}
	}
	
	private static void scheduleCheck() {
		LocalDateTime time = getNextHour();
		
		ZoneId zone = ZoneId.systemDefault();
		ZoneOffset zoneOffSet = zone.getRules()
			.getOffset(time);
		
		TimerTask updateTask = new TimerTask() {
			@Override
			public void run() {
				checkUpdate();
			}
		};
		
		Timer timer = new Timer("IpChecker");
		timer.schedule(updateTask, Date.from(time.toInstant(zoneOffSet)), 60 * 60 * 1000L);
	}
	
	private static LocalDateTime getNextHour() {
		LocalDateTime time = LocalDateTime.now();
		time = time.plusNanos(1000L - time.getNano());
		time = time.plusSeconds(60L - time.getSecond());
		time = time.plusMinutes(60L - time.getMinute());
		
		return time;
	}
	
	public static boolean checkUpdate() {
		Optional<String> newIp = IpChecker.checkForChange();
		newIp.ifPresent(Discord::notifyNewIp);
		return newIp.isPresent();
	}
}
