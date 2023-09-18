package net.unknownuser.ipchecker;

public class InitFailedException extends RuntimeException {
	private static final long serialVersionUID = 1558026132873281053L;
	
	public InitFailedException(Throwable cause) {
		super(cause);
	}
	
	public InitFailedException(String message, Throwable cause) {
		super(message, cause);
	}
}
