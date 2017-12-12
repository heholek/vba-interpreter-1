package org.siphon.visualbasic.runtime.framework.vba.objects;

import java.util.Hashtable;

import org.siphon.visualbasic.runtime.VbValue;
import org.siphon.visualbasic.runtime.framework.vba.objects.winmgmts.Win32_Process;
import org.siphon.visualbasic.runtime.framework.vba.objects.winmgmts.Win32_Process.Win32_ProcessStartup;

public class ObjectRegistry {

	protected static ObjectRegistry instance = null;
	protected Hashtable<String, VbValue> objects = new Hashtable<>();
	
	private ObjectRegistry() {
		registerObject("WinMgmts:Win32_Process", VbValue.fromJava(Win32_Process.getInstance()));
		registerObject("WinMgmts:Win32_ProcessStartup", VbValue.fromJava(new Win32_ProcessStartup()));
	}
	
	public static ObjectRegistry getInstance() {
		if (instance == null) {
			instance = new ObjectRegistry();
		}
		
		return instance;
	}

	public void registerObject(String name, VbValue object) {
		objects.put(name.toLowerCase(), object);
	}
	
	public VbValue getObject(String name) {
		return objects.get(name.toLowerCase());
	}
}
