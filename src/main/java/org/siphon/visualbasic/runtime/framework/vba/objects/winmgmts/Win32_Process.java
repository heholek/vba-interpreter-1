package org.siphon.visualbasic.runtime.framework.vba.objects.winmgmts;

import org.siphon.visualbasic.ArgumentMode;
import org.siphon.visualbasic.runtime.VbVarType.TypeEnum;
import org.siphon.visualbasic.runtime.framework.VbMethod;
import org.siphon.visualbasic.runtime.framework.VbParam;

import com.cisco.vbainterpreter.eventlog.EventLog;
import com.cisco.vbainterpreter.eventlog.HidesWindowProcessLogEvent;
import com.cisco.vbainterpreter.eventlog.StartProcessLogEvent;

public class Win32_Process {
	public static class Win32_ProcessStartup {
		public int CreateFlags;
		public String[] EnvironmentVariables;
		public short ErrorMode = 1;
		public int FillAttribute;
		public int PriorityClass;
		private short showWindow = 1;
		public String Title;
		public String WinstationDesktop;
		public int X;
		public int XCountChars;
		public int XSize;
		public int Y;
		public int YCountChars;
		public int YSize;
		
		@VbMethod
		public void setShowWindow(@VbParam(name = "value", type = TypeEnum.vbInteger) int value) {
			if (value == 0) {
				EventLog.getInstance().log(new HidesWindowProcessLogEvent());
			}
			showWindow = (short) value;
		}
		
		@VbMethod
		public short getShowWindow() {
			return showWindow;
		}
		
	}
	
	private static Win32_Process instance = null;
	
	private Win32_Process() {
		// TODO Auto-generated constructor stub
	}
	
	public static Win32_Process getInstance() {
		if (instance ==  null) {
			instance = new Win32_Process();
		}
		
		return instance;
	}
	
	@VbMethod
	public void Create(
			@VbParam(name="CommandLine", type = TypeEnum.vbString) 
			String commandLine,
			@VbParam(name="CurrentDirectory", type = TypeEnum.vbString)
			String currentDirectory,
			@VbParam(name="ProcessStartupInformation")
			Win32_ProcessStartup processStartupInformation,
			@VbParam(name="ProcessId",mode=ArgumentMode.ByRef, type = TypeEnum.vbInteger)
			Integer processId
			) 
	{
		EventLog.getInstance().log(new StartProcessLogEvent(commandLine, currentDirectory));
	}
}
