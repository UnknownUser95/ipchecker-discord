package net.unknownuser.ipchecker;

public class EnvArgMissing extends RuntimeException {
	private static final long serialVersionUID = 1L;
	
	public static final String MESSAGE_FORMAT = "The required environment variable %s is not set!";
	
	public EnvArgMissing(String argName) {
		super(String.format(MESSAGE_FORMAT, argName));
	}
}
