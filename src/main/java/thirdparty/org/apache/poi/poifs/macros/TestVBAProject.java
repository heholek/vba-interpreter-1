package thirdparty.org.apache.poi.poifs.macros;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class TestVBAProject implements VBAProject {
	private static class TestModule implements Module {
		private Path path;
		public TestModule(Path path) {
			this.path = path;
		}
		@Override
		public String getName() {
			return path.getFileName().toString();
		}
		
		@Override
		public String getContent() {
			try {
				return new String(Files.readAllBytes(path));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return "";
			}
		}
		
		@Override
		public ModuleType geModuleType() {
			return Module.ModuleType.Module;
		}
	}
	private List<TestModule> modules = new LinkedList<>();
	private TestVBAProject() {
		// TODO Auto-generated constructor stub
	}
	
	public static TestVBAProject fromFile(Path path) {
		TestVBAProject project = new TestVBAProject();
		project.modules.add(new TestModule(path));
		return project;
	}
	
	@Override
	public String getName() {
		return "TestProject";
	}
	
	@Override
	public Collection<? extends Module> getModules() {
		return modules;
	}
	
	@Override
	public String getProperties() {
		return "";
	}

}
