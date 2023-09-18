package net.unknownuser.ipchecker;

import java.util.*;

public enum Config {
	API_TOKEN,
	RESPONSE_CHANNEL;
	
	public String get() {
		String argName = this.name();
		String env	   = System.getenv(argName);
		
		if(env != null) {
			return env;
		} else {
			throw new EnvArgMissing(argName);
		}
	}
	
	public static List<EnvArgMissing> verifyAll() {
		List<EnvArgMissing> missing = new ArrayList<>();
		
		for(Config cfg : Config.values()) {
			try {
				cfg.get();
			} catch(EnvArgMissing exc) {
				System.out.println(exc.getMessage());
				missing.add(exc);
			}
		}
		
		return missing;
	}
	
}
