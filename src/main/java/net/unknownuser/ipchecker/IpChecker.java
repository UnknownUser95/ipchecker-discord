package net.unknownuser.ipchecker;

import java.io.*;
import java.net.*;
import java.time.*;
import java.util.*;

import javax.net.ssl.*;

// https://ipinfo.io/ip
public abstract class IpChecker {
	private IpChecker() {
		super();
	}
	
	public static final URL IPINFO_URL;
	static {
		try {
			IPINFO_URL = new URI("https://ipinfo.io/ip").toURL(); // NOSONAR
		} catch(MalformedURLException | URISyntaxException e) {
			throw new IllegalStateException();
		}
	}
	
	private static String lastIp = "";
	
	public static void init() {
		lastIp = getIP().get(); // NOSONAR
	}
	
	public static Optional<String> checkForChange() {
		Optional<String> newIp = getIP();
		
		if(newIp.isEmpty() || lastIp.equals(newIp.get())) {
			return Optional.empty();
		}
		
		lastIp = newIp.get();
		return Optional.of(lastIp);
	}
	
	public static String getCurrentIp() {
		return lastIp;
	}
	
	private static Optional<String> getIP() {
		HttpsURLConnection connection = getConnection();
		
		try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
			return Optional.of(br.readLine());
		} catch(IOException exc) {
			System.err.println(Instant.now() + ": could not get IP: " + exc.getMessage());
			return Optional.empty();
		}
	}
	
	private static HttpsURLConnection getConnection() {
		HttpsURLConnection connection;
		try {
			connection = (HttpsURLConnection) IPINFO_URL.openConnection();
			connection.setRequestMethod("GET");
			
			return connection;
		} catch(IOException e) {
			throw new IllegalStateException();
		}
	}
}
