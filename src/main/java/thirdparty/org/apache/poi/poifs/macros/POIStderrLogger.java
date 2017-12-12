package thirdparty.org.apache.poi.poifs.macros;

import java.util.Map;

import org.apache.poi.util.POILogger;

public class POIStderrLogger implements POILogger {
	private static final Map<Integer, String> LEVELS = Map.of(
			POILogger.DEBUG, "DEBUG",
			POILogger.INFO, "INFO",
			POILogger.WARN, "WARN",
			POILogger.ERROR, "ERROR",
			POILogger.FATAL, "FATAL");
			
	public POIStderrLogger() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void initialize(String cat) {
		// TODO Auto-generated method stub

	}

	@Override
	public void _log(int level, Object obj1) {
		System.err.printf("%s %s\n", LEVELS.getOrDefault(level, "UNKNOWN"), obj1.toString());
		// TODO Auto-generated method stub

	}

	@Override
	public void _log(int level, Object obj1, Throwable exception) {
		System.err.printf("%s %s\n\t%s\n", LEVELS.getOrDefault(level, "UNKNOWN"), obj1.toString(), exception.getStackTrace().toString());
		// TODO Auto-generated method stub

	}

	@Override
	public boolean check(int level) {
		return true;
	}

}
