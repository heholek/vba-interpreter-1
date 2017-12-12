/* ====================================================================
   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to You under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
==================================================================== */

package thirdparty.org.apache.poi.poifs.macros;

import static org.apache.poi.util.StringUtil.endsWithIgnoreCase;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.poi.poifs.filesystem.DirectoryNode;
import org.apache.poi.poifs.filesystem.DocumentNode;
import org.apache.poi.poifs.filesystem.Entry;
import org.apache.poi.poifs.filesystem.FileMagic;
import org.apache.poi.poifs.filesystem.OfficeXmlFileException;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.util.IOUtils;

/**
 * <p>Finds all VBA Macros in an office file (OLE2/POIFS and OOXML/OPC),
 *  and returns them.
 * </p>
 * <p>
 * <b>NOTE:</b> This does not read macros from .ppt files.
 * See org.apache.poi.hslf.usermodel.TestBugs.getMacrosFromHSLF() in the scratchpad
 * module for an example of how to do this. Patches that make macro
 * extraction from .ppt more elegant are welcomed!
 * </p>
 * 
 * @since 3.15-beta2
 */
public class VBAMacroReader implements Closeable {
    protected static final String VBA_PROJECT_OOXML = "vbaProject.bin";
    protected static final String VBA_PROJECT_POIFS = "VBA";
    protected static final String VBA_OBJECTS = "ObjectPool";

    private POIFSFileSystem fs;
    
    public VBAMacroReader(InputStream rstream) throws IOException {
        InputStream is = FileMagic.prepareToCheckMagic(rstream);
        FileMagic fm = FileMagic.valueOf(is);
        if (fm == FileMagic.OLE2) {
            fs = new POIFSFileSystem(is);
        } else {
            openOOXML(is);
        }
    }
    
    public VBAMacroReader(File file) throws IOException {
        try {
            this.fs = new POIFSFileSystem(file);
        } catch (OfficeXmlFileException e) {
            openOOXML(new FileInputStream(file));
        }
    }
    public VBAMacroReader(POIFSFileSystem fs) {
        this.fs = fs;
    }
    
    private void openOOXML(InputStream zipFile) throws IOException {
        try(ZipInputStream zis = new ZipInputStream(zipFile)) {
            ZipEntry zipEntry;
            while ((zipEntry = zis.getNextEntry()) != null) {
                if (endsWithIgnoreCase(zipEntry.getName(), VBA_PROJECT_OOXML)) {
                    try {
                        // Make a POIFSFileSystem from the contents, and close the stream
                        this.fs = new POIFSFileSystem(zis);
                        return;
                    } catch (IOException e) {
                        // Tidy up
                        zis.close();

                        // Pass on
                        throw e;
                    }
                }
            }
        }
        throw new IllegalArgumentException("No VBA project found");
    }
    
    public void close() throws IOException {
        fs.close();
        fs = null;
    }

    public List<? extends VBAProject> readProjects() throws IOException {
        //ascii -> unicode mapping for module names
        //preserve insertion order
        LinkedList<OLEVBAProject> projects = new LinkedList<OLEVBAProject>();
        findMacros(fs.getRoot(), projects);
        return projects;
    }
    
    protected static class ModuleImpl implements Module {
        Integer offset;
        byte[] buf;
        ModuleType moduleType;
        Charset charset;
        ASCIIUnicodeStringPair streamName;
        String unicodeName;
        String name;
        ASCIIUnicodeStringPair docString;
        int helpContext;
        String cookie;
        boolean modulePrivate;
        boolean readOnly;
        DocumentNode documentStream;
        void read(InputStream in) throws IOException {
            final ByteArrayOutputStream out = new ByteArrayOutputStream();
            IOUtils.copy(in, out);
            out.close();
            buf = out.toByteArray();
        }
        public String getContent() {
            if (buf != null) {
            	StringBuilder sb = new StringBuilder();
            	if (moduleType == moduleType.Class) {
            		sb.append("VERSION 1.0 CLASS\n");
            	}
            	sb.append(new String(buf, charset));
                return sb.toString();
            }
            else {
                return null;
            }
        }
        public String getName() {
        	return streamName.getUnicode();
        }
        public ModuleType geModuleType() {
            return moduleType;
        }
    }
    
    /**
     * Recursively traverses directory structure rooted at <tt>dir</tt>.
     * For each macro module that is found, the module's name and code are
     * added to <tt>modules<tt>.
     *
     * @param dir The directory of entries to look at
     * @param modules The resulting map of modules
     * @throws IOException If reading the VBA module fails
     * @since 3.15-beta2
     */
    protected void findMacros(DirectoryNode dir, List<OLEVBAProject> projects) throws IOException {
    	for (Entry child : dir) {
    		if (child instanceof DirectoryNode) {
    			if (VBA_PROJECT_POIFS.equalsIgnoreCase(child.getName())) {
    				OLEVBAProject project = OLEVBAProject.readVBAProject(dir);
    				if (project != null) {
    					projects.add(project);
    				}
    			}
    			else {
    				findMacros((DirectoryNode) child, projects);
    			}
    		}
    	}
    }
    
    protected Collection<VBAObject> objects = null;
    
    protected void findObjects(DirectoryNode dir, Collection<VBAObject> objects) throws IOException {
    	
    	VBAObject obj = VBAObject.readObject(dir);
    	if (obj != null) {
    		objects.add(obj);
    	}
    	else {
    		for (Entry child : dir) {
    			if (child instanceof DirectoryNode) {
    				findObjects((DirectoryNode) child, objects);
    			}
    		}
    	}
    }
    
    public Collection<VBAObject> readObjects() throws IOException {
    	if (objects == null) {
    		objects = new Vector<>();
    		findObjects(fs.getRoot(), objects);
    	}
    	
    	return objects;
    }
}
