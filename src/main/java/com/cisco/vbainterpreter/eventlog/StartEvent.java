package com.cisco.vbainterpreter.eventlog;

public class StartEvent extends LogEvent {

	private String hash;
	
	public StartEvent(byte[] hash) {
		super("start");
		StringBuilder builder = new StringBuilder();
		for (byte b : hash) {
			builder.append(String.format("%02x", b & 0xff));
		}
		this.hash = builder.toString();
	}
	
	public String getHash() {
		return hash;
	}

}