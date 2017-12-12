package com.cisco.vbainterpreter.eventlog;

public abstract class LogEvent {
	private String event;
	
	public LogEvent(String event) {
		this.event = event;
	}
	
	public String getEvent() {
		return event;
	}
}
