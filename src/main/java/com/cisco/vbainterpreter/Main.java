package com.cisco.vbainterpreter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import thirdparty.org.apache.poi.poifs.macros.Module;
import thirdparty.org.apache.poi.poifs.macros.TestVBAProject;
import thirdparty.org.apache.poi.poifs.macros.VBAMacroReader;
import thirdparty.org.apache.poi.poifs.macros.VBAObject;
import thirdparty.org.apache.poi.poifs.macros.VBAProject;

import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.hslf.record.DocInfoListContainer;
import org.apache.poi.hslf.record.RecordTypes;
import org.apache.poi.hslf.record.VBAInfoAtom;
import org.apache.poi.hslf.record.VBAInfoContainer;
import org.apache.poi.hslf.usermodel.HSLFObjectData;
import org.apache.poi.hslf.usermodel.HSLFSlideShow;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.siphon.visualbasic.ArgumentException;
import org.siphon.visualbasic.Interpreter;
import org.siphon.visualbasic.Library;
import org.siphon.visualbasic.MethodDecl;
import org.siphon.visualbasic.compile.ImpossibleException;
import org.siphon.visualbasic.compile.VbErrorsException;
import org.siphon.visualbasic.runtime.VbRuntimeException;
import org.siphon.visualbasic.runtime.VbValue;
import org.siphon.visualbasic.runtime.framework.Debug;

import com.cisco.vbainterpreter.eventlog.EndEvent;
import com.cisco.vbainterpreter.eventlog.EventLog;
import com.cisco.vbainterpreter.eventlog.StartEvent;

import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;

public class Main
{
	private static final Logger logger = LogManager.getLogger(Main.class);
	
    //It isn't pretty, but it works...
    private static Map<String, Module> getMacrosFromHSLF(String fileName) throws IOException {
        InputStream is = null;
        POIFSFileSystem poifs = null;
        try {
            is = new FileInputStream(fileName);
            poifs = new POIFSFileSystem(is);
            //TODO: should we run the VBAMacroReader on this poifs?
            //TBD: We know that ppt typically don't store macros in the regular place,
            //but _can_ they?

            HSLFSlideShow ppt = new HSLFSlideShow(poifs);

            //get macro persist id
            DocInfoListContainer list = (DocInfoListContainer)ppt.getDocumentRecord().findFirstOfType(RecordTypes.List.typeID);
            VBAInfoContainer vbaInfo = (VBAInfoContainer)list.findFirstOfType(RecordTypes.VBAInfo.typeID);
            VBAInfoAtom vbaAtom = (VBAInfoAtom)vbaInfo.findFirstOfType(RecordTypes.VBAInfoAtom.typeID);
            long persistId = vbaAtom.getPersistIdRef();
            for (HSLFObjectData objData : ppt.getEmbeddedObjects()) {
                if (objData.getExOleObjStg().getPersistId() == persistId) {
                    try (VBAMacroReader mr = new VBAMacroReader(objData.getInputStream())) {
//                        return mr.readMacroModules();
                    	throw new UnsupportedOperationException("Not implemented");
                    }
                }
            }

            ppt.close();

        } finally {
            IOUtils.closeQuietly(poifs);
            IOUtils.closeQuietly(is);
        }
        return null;
    }

	
    public static void main(String[] args) throws IOException
    {
    	ArgumentParser argparse = ArgumentParsers.newFor("vbaemu").build()
    			.description("Run VBA macros");
    	argparse.addArgument("--test")
    		.action(Arguments.storeTrue())
    		.setDefault(false)
    		.help("Load VBA macros from text file instead of Office document");
        argparse.addArgument("--log")
            .help("Output to log file instead of stdout");
        argparse.addArgument("--files")
            .help("Extract files to this directory and keep them");
    	argparse.addArgument("input")
    		.help("Input file");
    	
    	try {
    		Namespace res = argparse.parseArgs(args);
    		
    		if (res.getString("log") != null) {
    			EventLog.getInstance().init(new FileOutputStream(res.getString("log")));
    		}
    		else {
    			EventLog.getInstance().init(System.out);
    		}
    		List<? extends VBAProject> projects;
    		Collection<VBAObject> objects;
    		if (res.getBoolean("test")) {
    			LinkedList<TestVBAProject> testProjects = new LinkedList<>();
    			projects = testProjects;
    			testProjects.add(TestVBAProject.fromFile(Paths.get(res.getString("input"))));
    			objects = new Vector<VBAObject>();
    		}
    		else {
    			try (VBAMacroReader macroReader = new VBAMacroReader(new File(res.getString("input")))) {
    				projects = macroReader.readProjects();
    				objects = macroReader.readObjects();
    			}
    			//TODO: Extract macros from ppt 
    		}
    		
    		if (projects.isEmpty()) {
    			logger.error("No VBA projects found");
    			return;
    		}
    		
    		VBAProject project = projects.iterator().next();
    		
    		byte[] hash = new byte[] {0x00, 0x00, 0x00, 0x00};
    		try (InputStream in = new FileInputStream(res.getString("input"))) {
    			MessageDigest digest = MessageDigest.getInstance("SHA-256");
    			byte[] block = new byte[4096];
    			int length;
    			while ((length = in.read(block)) > 0) {
    				digest.update(block, 0, length);
    			}
    			hash = digest.digest();
    		}
    		catch (NoSuchAlgorithmException ex) {
    			ex.printStackTrace();
    		}
    		
    		
    		long start = System.currentTimeMillis();
    		EventLog.getInstance().log(new StartEvent(hash));
    		org.siphon.visualbasic.compile.Compiler compiler = new org.siphon.visualbasic.compile.Compiler();
            compiler.bindObject("DEBUG", VbValue.fromJava(new Debug()));

			Library lib = compiler.compile("VBAMacros", project.getModules(), objects);
			
			try {
				org.siphon.visualbasic.interpreter.Interpreter interp = new org.siphon.visualbasic.interpreter.Interpreter(compiler);
				interp.add(lib);
				//interp.call("autoopen");
				//Interpreter interpreter = new Interpreter().load(compiler.generateStatements());
				//interpreter.getDebugger().setCompiler(compiler);
				
				List<MethodDecl> autoopen = lib.findAutoOpen();
				if (!autoopen.isEmpty()) {
					interp.call(autoopen.get(0).getName());
				}
				//	interpreter.invoke("VBAMacros", lib.modules.values().iterator().next().getName(), autoopen.iterator().next().getName(), 1, 2);
				//	//TODO: Run not only first function, but all of them
				//}
				else {
					logger.error("Document doesn't contain any known autorun function");
				}
			}
			catch (ImpossibleException e) {
				logger.catching(e);
			}
			
			long end = System.currentTimeMillis();
			EventLog.getInstance().log(new EndEvent(end - start));
			EventLog.getInstance().close();
			logger.info("Execution took {} milliseconds", end - start);
    	}
    	catch (ArgumentParserException e) {
    		argparse.handleError(e);
    	}
    	
    }
}
