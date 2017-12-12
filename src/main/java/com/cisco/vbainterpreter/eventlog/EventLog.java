package com.cisco.vbainterpreter.eventlog;

import java.io.IOException;
import java.io.OutputStream;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SequenceWriter;

public class EventLog {
	private static EventLog instance = null;
	SequenceWriter sequenceWriter = null;
	
	public static EventLog getInstance() {
		if (instance == null) {
			instance = new EventLog();
		}
		
		return instance;
	}
	
	public void init(OutputStream out) throws IOException {
		ObjectMapper objectMapper = new ObjectMapper();
		ObjectWriter writer = objectMapper.writer().withDefaultPrettyPrinter();
		this.sequenceWriter = writer.writeValues(out);
		this.sequenceWriter.init(true);
	}
	
	public synchronized void log(LogEvent evt) {
		if (this.sequenceWriter == null) {
			throw new RuntimeException("The logger has not been initialized");
		}
		try {
			this.sequenceWriter.write(evt);
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void close() throws IOException {
		if (this.sequenceWriter == null) {
			throw new RuntimeException("The logger has not been initialized");
		}
		
		this.sequenceWriter.close();
	}
}
