package com.cisco.vbainterpreter.eventlog;

public class EndEvent extends LogEvent {

	private long millis;
	
	public EndEvent(long millis) {
		super("end");
		this.millis = millis;
	}

	public long getMillis() {
		return millis;
	}
}
