package com.cisco.vbainterpreter.eventlog;

public class StartProcessLogEvent extends LogEvent {

	/** Command line */
	private String cmdline;
	/** Current working directory */
	private String cwd;
	
	public StartProcessLogEvent(String cmdline, String cwd) {
		super("process-start");
		this.cmdline = cmdline;
		this.cwd = cwd;
	}

	public String getCmdline() {
		return cmdline;
	}

	public String getCwd() {
		return cwd;
	}
}
