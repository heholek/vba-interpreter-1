package thirdparty.org.apache.poi.poifs.macros;

import java.util.Collection;

public interface VBAProject {
	public String getName();
	public Collection<? extends Module> getModules();
	public String getProperties();
}
