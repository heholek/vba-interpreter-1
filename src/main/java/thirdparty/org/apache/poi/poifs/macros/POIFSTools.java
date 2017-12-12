package thirdparty.org.apache.poi.poifs.macros;

import org.apache.poi.poifs.filesystem.DirectoryNode;
import org.apache.poi.poifs.filesystem.DocumentNode;
import org.apache.poi.poifs.filesystem.Entry;

public class POIFSTools {

	private POIFSTools() {
		
	}
	
	public static Entry findEntry(DirectoryNode dir, String name) {
    	if (dir == null) {
    		return null;
    	}
    	for (Entry entry : dir) {
    		if (name.equalsIgnoreCase(entry.getName())) {
    			return entry;
    		}
    	}
    	
    	return null;
    } 
    
    public static DocumentNode findDocumentNode(DirectoryNode dir, String name) {
    	Entry entry = findEntry(dir, name);
    	if (entry != null && entry instanceof DocumentNode) {
    		return (DocumentNode) entry;
    	}
    	
    	return null;
    }
    	
    public static DirectoryNode findDirectoryNode(DirectoryNode dir, String name) {
		Entry entry = findEntry(dir, name);
    	if (entry != null && entry instanceof DirectoryNode) {
    		return (DirectoryNode) entry;
    	}
    	
    	return null;
    }

}
