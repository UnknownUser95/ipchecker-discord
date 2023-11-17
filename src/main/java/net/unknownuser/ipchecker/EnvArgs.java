package net.unknownuser.ipchecker;

import java.util.*;

public enum EnvArgs {
	API_TOKEN;
	
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
		
		for(EnvArgs cfg : EnvArgs.values()) {
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
