package thirdparty.org.apache.poi.poifs.macros;

import static org.apache.poi.util.StringUtil.startsWithIgnoreCase;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.apache.poi.poifs.filesystem.DirectoryNode;
import org.apache.poi.poifs.filesystem.DocumentInputStream;
import org.apache.poi.poifs.filesystem.DocumentNode;
import org.apache.poi.poifs.filesystem.Entry;
import org.apache.poi.util.CodePageUtil;
import org.apache.poi.util.HexDump;
import org.apache.poi.util.IOUtils;
import org.apache.poi.util.LittleEndian;
import org.apache.poi.util.POILogFactory;
import org.apache.poi.util.POILogger;
import org.apache.poi.util.RLEDecompressingInputStream;
import org.apache.poi.util.StringUtil;

import thirdparty.org.apache.poi.poifs.macros.Module.ModuleType;
import thirdparty.org.apache.poi.poifs.macros.VBAMacroReader.ModuleImpl;

public class OLEVBAProject implements VBAProject {
	//arbitrary limit on size of strings to read, etc.
	private static final int MAX_STRING_LENGTH = 20000;
	
	protected static POILogger LOGGER = POILogFactory.getLogger(OLEVBAProject.class);
    
    // Constants from MS-OVBA: https://msdn.microsoft.com/en-us/library/office/cc313094(v=office.12).aspx
    private static final int STREAMNAME_RESERVED = 0x0032;
    private static final int PROJECT_CONSTANTS_RESERVED = 0x003C;
    private static final int HELP_FILE_PATH_RESERVED = 0x003D;
    private static final int REFERENCE_NAME_RESERVED = 0x003E;
    private static final int DOC_STRING_RESERVED = 0x0040;
    private static final int MODULE_DOCSTRING_RESERVED = 0x0048;  
	
	public enum SysKind {
		WINDOWS_16BIT(0),
		WINDOWS_32BIT(1),
		MACINTOSH(2),
		WINDOWS_64BIT(3);
		
		private final int value;
		private static Map<Integer, SysKind> map = new HashMap<>();
		
		SysKind(final int value) {this.value = value;}
		public int getValue() {return value;}
		public static SysKind valueOf(int val) {return map.get(val);}
		static {for (SysKind kind : SysKind.values()) {map.put(kind.getValue(), kind);}}
	}
	
    private enum DIR_STATE {
        INFORMATION_RECORD,
        REFERENCES_RECORD,
        MODULES_RECORD
    }
    
    private enum RecordType {
        // Constants from MS-OVBA: https://msdn.microsoft.com/en-us/library/office/cc313094(v=office.12).aspx
        MODULE_OFFSET(0x0031),
        PROJECT_SYS_KIND(0x01),
        PROJECT_LCID(0x0002),
        PROJECT_LCID_INVOKE(0x14),
        PROJECT_CODEPAGE(0x0003),
        PROJECT_NAME(0x04),
        PROJECT_DOC_STRING(0x05),
        PROJECT_HELP_FILE_PATH(0x06),
        PROJECT_HELP_CONTEXT(0x07, 8),
        PROJECT_LIB_FLAGS(0x08),
        PROJECT_VERSION(0x09, 10),
        PROJECT_CONSTANTS(0x0C),
        PROJECT_MODULES(0x0F),
        DIR_STREAM_TERMINATOR(0x10),
        PROJECT_COOKIE(0x13),
        MODULE_NAME(0x19),
        MODULE_NAME_UNICODE(0x47),
        MODULE_STREAM_NAME(0x1A),
        MODULE_DOC_STRING(0x1C),
        MODULE_HELP_CONTEXT(0x1E),
        MODULE_COOKIE(0x2c),
        MODULE_TYPE_PROCEDURAL(0x21, 4),
        MODULE_TYPE_OTHER(0x22, 4),
        MODULE_PRIVATE(0x28, 4),
        REFERENCE_NAME(0x16),
        REFERENCE_REGISTERED(0x0D),
        REFERENCE_PROJECT(0x0E),
        REFERENCE_CONTROL_A(0x2F),

        //according to the spec, REFERENCE_CONTROL_B(0x33) should have the
        //same structure as REFERENCE_CONTROL_A(0x2F).
        //However, it seems to have the int(length) record structure that most others do.
        //See 59830.xls for this record.
        REFERENCE_CONTROL_B(0x33),
        //REFERENCE_ORIGINAL(0x33),


        MODULE_TERMINATOR(0x002B),
        EOF(-1),
        UNKNOWN(-2);


        private final int VARIABLE_LENGTH = -1;
        private final int id;
        private final int constantLength;

        RecordType(int id) {
            this.id = id;
            this.constantLength = VARIABLE_LENGTH;
        }

        RecordType(int id, int constantLength) {
            this.id = id;
            this.constantLength = constantLength;
        }

        int getConstantLength() {
            return constantLength;
        }

        static RecordType lookup(int id) {
            for (RecordType type : RecordType.values()) {
                if (type.id == id) {
                    return type;
                }
            }
            return UNKNOWN;
        }
    }
 
	public Map<String, ModuleImpl> modules = new HashMap<String, ModuleImpl>();
	public SysKind sysKind;
	public Charset charset = StringUtil.WIN_1252;
	public String name;
	public ASCIIUnicodeStringPair docString;
	public int versionMajor;
	public int versionMinor;
	public ASCIIUnicodeStringPair helpFilePath;
	public ASCIIUnicodeStringPair constants;
	public DocumentNode dirNode;
	public DocumentNode projectNode;
	public DocumentNode projectWmNode;
	public DirectoryNode vbaNode;
	public DocumentNode vbaProjectNode;
	public String properties;
	public Map<String, String> nameMap = new HashMap<>();
	
	
	private OLEVBAProject() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public String getName() {
		return name;
	}
	
	@Override
	public String getProperties() {
		return properties;
	}
    
    
    /**
     * Get the path of a poifs entry.
     * @param ent The entry
     * @return Absolute path of the entry in the poifs.
     */
    protected static String getPath(Entry ent) {
    	LinkedList<String> components = new LinkedList<String>();
    	for (; ent.getParent() != null; ent = ent.getParent()) {
    		components.add(ent.getName());
    	}
    	
    	Collections.reverse(components);
    	return "\\" + String.join("\\", components);
    }
   
	
    /**
     * Reads VBA Project modules from a VBA Project directory located at
     * <tt>macroDir</tt> into <tt>modules</tt>.
     *
     * @since 3.15-beta2
     */  
	public static OLEVBAProject readVBAProject(DirectoryNode macroDir) throws IOException {
	        //bug59858 shows that dirstream may not be in this directory (\MBD00082648\_VBA_PROJECT_CUR\VBA ENTRY NAME)
	        //but may be in another directory (\_VBA_PROJECT_CUR\VBA ENTRY NAME)
	        //process the dirstream first -- "dir" is case insensitive
	    	OLEVBAProject project = new OLEVBAProject();
	    	
	    	project.projectNode = POIFSTools.findDocumentNode(macroDir, "project");
	    	project.projectWmNode = POIFSTools.findDocumentNode(macroDir, "projectwm");
	    	project.vbaNode = POIFSTools.findDirectoryNode(macroDir, "vba");
	    	project.vbaProjectNode = POIFSTools.findDocumentNode(project.vbaNode, "_vba_project");
	    	project.dirNode = POIFSTools.findDocumentNode(project.vbaNode, "dir");
	    	
	    	if (project.projectNode == null 
	    	    || project.projectWmNode == null 
	    	    || project.vbaNode == null
	    	    || project.vbaProjectNode == null
	    	    || project.dirNode == null)
	    	{
	    		LOGGER.log(POILogger.WARN, "Failed to read VBA project at ", getPath(macroDir));
	    		return null;
	    	}
	    	
	    	project.processDirStream();

	        for (Entry entry : project.vbaNode) {
	            if (! (entry instanceof DocumentNode)) { continue; }
	            
	            DocumentNode document = (DocumentNode)entry;
	            
	            if (!entry.getName().equalsIgnoreCase("dir") 
	            	&& !startsWithIgnoreCase(entry.getName(), "__srp")
	            	&& !entry.getName().equalsIgnoreCase("_vba_project")) {
	                    // process module, skip __SRP and _VBA_PROJECT since these do not contain macros
	                    project.readModuleFromDocumentStream(document, entry.getName());
	            }
	        }
	        
	        project.readNameMapRecords();
	        project.processProjectStream();
	        
	        return project;
	}
	
	 protected void readNameMapRecords() throws IOException {
		 try(DocumentInputStreamWrapper dis = new DocumentInputStreamWrapper(new DocumentInputStream(projectWmNode))) {             
			 //see 2.3.3 PROJECTwm Stream: Module Name Information
			 //multibytecharstring
			 String mbcs = null;
			 String unicode = null;
			 //arbitrary sanity threshold
			 final int maxNameRecords = 10000;
			 int records = 0;
			 while (++records < maxNameRecords) {
				 try {
					 int b = IOUtils.readByte(dis);
					 //check for two 0x00 that mark end of record
					 if (b == 0) {
						 b = IOUtils.readByte(dis);
						 if (b == 0) {
							 return;
						 }
					 }
					 mbcs = dis.readMBCS(b, charset, MAX_STRING_LENGTH);
				 } catch (EOFException e) {
					 return;
				 }

				 try {
					 unicode = dis.readUnicode(MAX_STRING_LENGTH);
				 } catch (EOFException e) {
					 return;
				 }
				 if (mbcs.trim().length() > 0 && unicode.trim().length() > 0) {
					 nameMap.put(mbcs, unicode);
				 }

			 }
			 if (records >= maxNameRecords) {
				 LOGGER.log(POILogger.WARN, "Hit max name records to read ("+maxNameRecords+"). Stopped early.");
			 }
		 }
	 }
	 
	   
	    
	    protected void readNameMapRecords(DocumentInputStreamWrapper is,
                Map<String, String> moduleNames, Charset charset) throws IOException {
	    	//see 2.3.3 PROJECTwm Stream: Module Name Information
	    	//multibytecharstring
	    	String mbcs = null;
	    	String unicode = null;
	    	//arbitrary sanity threshold
	    	final int maxNameRecords = 10000;
	    	int records = 0;
	    	while (++records < maxNameRecords) {
	    		try {
	    			int b = IOUtils.readByte(is);
	    			//check for two 0x00 that mark end of record
	    			if (b == 0) {
	    				b = IOUtils.readByte(is);
	    				if (b == 0) {
	    					return;
	    				}
	    			}
	    			mbcs = is.readMBCS(b, charset, MAX_STRING_LENGTH);
	    		} catch (EOFException e) {
	    			return;
	    		}
	    		
	    		try {
	    			unicode = is.readUnicode(MAX_STRING_LENGTH);
	    		} catch (EOFException e) {
	    			return;
	    		}
	    		if (mbcs.trim().length() > 0 && unicode.trim().length() > 0) {
	    			moduleNames.put(mbcs, unicode);
	    		}
	    	}
	    	if (records >= maxNameRecords) {
	    		LOGGER.log(POILogger.WARN, "Hit max name records to read ("+maxNameRecords+"). Stopped early.");
	    	}
	    }
	


	private void processDirStream() throws IOException {
	    DIR_STATE dirState = DIR_STATE.INFORMATION_RECORD;
	    try (DocumentInputStream dis = new DocumentInputStream(dirNode)) {
	        int recordId = 0;
	        ModuleImpl currentModule = new ModuleImpl();
	
	        try (ExtendedRLEDecompressingInputStream in = new ExtendedRLEDecompressingInputStream(dis)) {
	            while (true) {
	                recordId = in.readShort();
	                if (recordId == -1) {
	                    break;
	                }
	                RecordType type = RecordType.lookup(recordId);
	
	                if (type.equals(RecordType.EOF) || type.equals(RecordType.DIR_STREAM_TERMINATOR)) {
	                    break;
	                }
	                switch (type) {
	                	case PROJECT_SYS_KIND:
	                		int sysKindSize = in.readInt();
	                		assert sysKindSize == 4;
	                		sysKind = SysKind.valueOf(in.readInt());
	                		break;
	                    case PROJECT_VERSION:
	                    	int projectVersionReserved = in.readInt();
	                    	assert projectVersionReserved == 4;
	                    	versionMajor = in.readInt();
	                    	versionMinor = in.readShort();
	                        break;
	                    case PROJECT_CODEPAGE:
	                        in.readInt();//record size must == 4
	                        int codepage = in.readShort();
	                        charset = Charset.forName(CodePageUtil.codepageToEncoding(codepage, true));
	                        break;
	                    case PROJECT_NAME:
	                    	int projectNameSize = in.readInt();
	                    	name = in.readString( projectNameSize, charset);
	                    	break;
	                    case MODULE_STREAM_NAME:
	                        currentModule.streamName = in.readStringPair(charset, STREAMNAME_RESERVED);
	                        break;
	                    case MODULE_NAME_UNICODE:
	                    	int unicodeNameSize = in.readInt();
	                    	currentModule.unicodeName = in.readUnicodeString(unicodeNameSize);
	                    	break;
	                    case MODULE_NAME:
	                    	int nameSize = in.readInt();
	                    	currentModule.name = in.readString(nameSize, charset);
	                    	break;
	                    case PROJECT_DOC_STRING:
	                        docString = in.readStringPair(charset, DOC_STRING_RESERVED);
	                        break;
	                    case PROJECT_HELP_FILE_PATH:
	                        helpFilePath = in.readStringPair(charset, HELP_FILE_PATH_RESERVED);
	                        break;
	                    case PROJECT_CONSTANTS:
	                        constants = in.readStringPair(charset, PROJECT_CONSTANTS_RESERVED);
	                        break;
	                    case REFERENCE_NAME:
	                        if (dirState.equals(DIR_STATE.INFORMATION_RECORD)) {
	                            dirState = DIR_STATE.REFERENCES_RECORD;
	                        }
	                        ASCIIUnicodeStringPair stringPair = in.readStringPair(charset, REFERENCE_NAME_RESERVED, false);
	                        if (stringPair.getPushbackRecordId() == -1) {
	                            break;
	                        }
	                        //Special handling for when there's only an ascii string and a REFERENCED_REGISTERED
	                        //record that follows.
	                        //See https://github.com/decalage2/oletools/blob/master/oletools/olevba.py#L1516
	                        //and https://github.com/decalage2/oletools/pull/135 from (@c1fe)
	                        if (stringPair.getPushbackRecordId() != RecordType.REFERENCE_REGISTERED.id) {
	                            throw new IllegalArgumentException("Unexpected reserved character. "+
	                                    "Expected "+Integer.toHexString(REFERENCE_NAME_RESERVED)
	                                    + " or "+Integer.toHexString(RecordType.REFERENCE_REGISTERED.id)+
	                                    " not: "+Integer.toHexString(stringPair.getPushbackRecordId()));
	                        }
	                        //fall through!
	                    case REFERENCE_REGISTERED:
	                        //REFERENCE_REGISTERED must come immediately after
	                        //REFERENCE_NAME to allow for fall through in special case of bug 62625
	                        int recLength = in.readInt();
	                        in.trySkip(recLength);
	                        break;
	                    case MODULE_DOC_STRING:
	                        currentModule.docString = in.readStringPair(charset, MODULE_DOCSTRING_RESERVED);
	                        break;
	                    case MODULE_HELP_CONTEXT:
	                    	int helpContextSize = in.readInt();
	                    	assert helpContextSize == 4;
	                    	currentModule.helpContext = in.readInt();
	                    	break;
	                    case MODULE_OFFSET:
	                        int modOffsetSz = in.readInt();
	                        assert modOffsetSz == 4;
	                        currentModule.offset = in.readInt();
	                        break;
	                    case PROJECT_MODULES:
	                        dirState = DIR_STATE.MODULES_RECORD;
	                        in.readInt();//size must == 2
	                        in.readShort();//number of modules
	                        break;
	                    case REFERENCE_CONTROL_A:
	                        int szTwiddled = in.readInt();
	                        in.trySkip(szTwiddled);
	                        int nextRecord = in.readShort();
	                        //reference name is optional!
	                        if (nextRecord == RecordType.REFERENCE_NAME.id) {
	                            in.readStringPair(charset, REFERENCE_NAME_RESERVED);
	                            nextRecord = in.readShort();
	                        }
	                        if (nextRecord != 0x30) {
	                            throw new IOException("Expected 0x30 as Reserved3 in a ReferenceControl record");
	                        }
	                        int szExtended = in.readInt();
	                        in.trySkip(szExtended);
	                        break;
	                    case MODULE_TERMINATOR:
	                        int endOfModulesReserved = in.readInt();
	                        assert endOfModulesReserved == 0;
	                        //must be 0;
	                        currentModule.charset = charset;
	                        modules.put(currentModule.streamName.getUnicode(), currentModule);
	                        currentModule = new ModuleImpl();
	                        break;
	                    default:
	                        if (type.getConstantLength() > -1) {
	                            in.trySkip(type.getConstantLength());
	                        } else {
	                            int recordLength = in.readInt();
	                            in.trySkip(recordLength);
	                        }
	                        break;
	                }
	            }
	        } catch (final IOException e) {
	            throw new IOException(
	                    "Error occurred while reading macros at section id "
	                            + recordId + " (" + HexDump.shortToHex(recordId) + ")", e);
	        }
	    }
	}
	
    protected void processProjectStream() throws IOException {
    	try(DocumentInputStream dis = new DocumentInputStream(projectNode)) {
    		InputStreamReader reader = new InputStreamReader(dis, charset);
    		StringBuilder builder = new StringBuilder();
    		char[] buffer = new char[512];
    		int read;
    		while ((read = reader.read(buffer)) >= 0) {
    			builder.append(buffer, 0, read);
    		}
    		
    		properties = builder.toString();
    		//the module name map names should be in exactly the same order
    		//as the module names here. See 2.3.3 PROJECTwm Stream.
    		//At some point, we might want to enforce that.
    		for (String line : properties.split("\r\n|\n\r")) {
    			if (!line.startsWith("[")) {
    				String[] tokens = line.split("=");
    				if (tokens.length > 1 && tokens[1].length() > 1
    					&& tokens[1].startsWith("\"") && tokens[1].endsWith("\"")) {
    					// Remove any double quotes
    					tokens[1] = tokens[1].substring(1, tokens[1].length() - 1);
    				}
    				if ("Document".equals(tokens[0]) && tokens.length > 1) {
    					String mn = tokens[1].substring(0, tokens[1].indexOf("/&H"));
    					ModuleImpl module = getModule(mn);
    					
    					if (module != null) {
							module.moduleType = ModuleType.Document;
						} else {
							LOGGER.log(POILogger.WARN, "couldn't find module with name: "+mn);
						}
    				} else if ("Module".equals(tokens[0]) && tokens.length > 1) {
    					ModuleImpl module = getModule(tokens[1]);
    					if (module != null) {
    						module.moduleType = ModuleType.Module;
    					} else {
    						LOGGER.log(POILogger.WARN, "couldn't find module with name: "+tokens[1]);
    					}
    				} else if ("Class".equals(tokens[0]) && tokens.length > 1) {
    					ModuleImpl module = getModule(tokens[1]);
    					if (module != null) {
    						module.moduleType = ModuleType.Class;
    					} else {
    						LOGGER.log(POILogger.WARN, "couldn't find module with name: "+tokens[1]);
    					}
    				}
    			}
    		}
    	}
    }
    
    private void readModuleFromDocumentStream(DocumentNode documentNode, String name) throws IOException {
        ModuleImpl module = modules.get(name);
        if (module == null) {
        	LOGGER.log(POILogger.ERROR, "This module has a document stream, but was not present in the dir stream: ", name);
        	//We didn't read the dir stream before reading the module stream. Ignore the module.
        	return;
        }
        
        if (module.buf != null) {
        	//The module stream exists twice within the current directory. Ignore it.
        	LOGGER.log(POILogger.ERROR, "This module has several document streams: ", name);
        	return;
        }
        
        if (module.offset == null) {
            //This should not happen. bug 59858
            throw new IOException("Module offset for '" + name + "' was never read.");
        }
        
        module.documentStream = documentNode;

        //try the general case, where module.offset is accurate
        InputStream decompressed = null;
        DocumentInputStreamWrapper compressed = new DocumentInputStreamWrapper(new DocumentInputStream(documentNode));
        try {
            // we know the offset already, so decompress immediately on-the-fly
            compressed.trySkip(module.offset);
            decompressed = new RLEDecompressingInputStream(compressed);
            module.read(decompressed);
            return;
        } catch (IllegalArgumentException | IllegalStateException e) {
        } finally {
            IOUtils.closeQuietly(compressed);
            IOUtils.closeQuietly(decompressed);
        }

        //bad module.offset, try brute force
        compressed = new DocumentInputStreamWrapper(new DocumentInputStream(documentNode));
        byte[] decompressedBytes;
        try {
            decompressedBytes = findCompressedStreamWBruteForce(compressed);
        } finally {
            IOUtils.closeQuietly(compressed);
        }

        if (decompressedBytes != null) {
            module.read(new ByteArrayInputStream(decompressedBytes));
        }
    }
    
    /**
     * Sometimes the offset record in the dirstream is incorrect, but the macro can still be found.
     * This will try to find the the first RLEDecompressing stream that starts with "Attribute".
     * This relies on some, er, heuristics, admittedly.
     *
     * @param is full module inputstream to read
     * @return uncompressed bytes if found, <code>null</code> otherwise
     * @throws IOException for a true IOException copying the is to a byte array
     */
    private static byte[] findCompressedStreamWBruteForce(InputStream is) throws IOException {
        //buffer to memory for multiple tries
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        IOUtils.copy(is, bos);
        byte[] compressed = bos.toByteArray();
        byte[] decompressed = null;
        for (int i = 0; i < compressed.length; i++) {
            if (compressed[i] == 0x01 && i < compressed.length-1) {
                int w = LittleEndian.getUShort(compressed, i+1);
                if (w <= 0 || (w & 0x7000) != 0x3000) {
                    continue;
                }
                decompressed = tryToDecompress(new ByteArrayInputStream(compressed, i, compressed.length - i));
                if (decompressed != null) {
                    if (decompressed.length > 9) {
                        //this is a complete hack.  The challenge is that there
                        //can be many 0 length or junk streams that are uncompressed
                        //look in the first 20 characters for "Attribute"
                        int firstX = Math.min(20, decompressed.length);
                        String start = new String(decompressed, 0, firstX, StringUtil.WIN_1252);
                        if (start.contains("Attribute")) {
                            return decompressed;
                        }
                    }
                }
            }
        }
        return decompressed;
    }
    
    private static byte[] tryToDecompress(InputStream is) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            IOUtils.copy(new RLEDecompressingInputStream(is), bos);
        } catch (IllegalArgumentException | IOException | IllegalStateException e){
            return null;
        }
        return bos.toByteArray();
    }
    
    protected ModuleImpl getModule(String name) {
    	if (nameMap.containsKey(name)) {
    		return modules.get(nameMap.get(name));
    	}
    	else {
    		return modules.get(name.toLowerCase());
    	}
    }
    
    @Override
    public Collection<? extends Module> getModules() {
    	return modules.values();
    }
}
