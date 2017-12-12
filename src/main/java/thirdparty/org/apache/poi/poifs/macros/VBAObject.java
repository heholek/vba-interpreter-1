package thirdparty.org.apache.poi.poifs.macros;

import java.io.DataInput;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import javax.swing.text.Document;

import org.apache.commons.io.IOUtils;
import org.apache.poi.poifs.filesystem.DirectoryNode;
import org.apache.poi.poifs.filesystem.DocumentInputStream;
import org.apache.poi.poifs.filesystem.DocumentNode;
import org.apache.poi.poifs.filesystem.Entry;
import org.apache.poi.poifs.filesystem.EntryNode;
import org.siphon.visualbasic.ArgumentException;
import org.siphon.visualbasic.runtime.VbBoundObject;
import org.siphon.visualbasic.runtime.VbRuntimeException;
import org.siphon.visualbasic.runtime.VbValue;
import org.siphon.visualbasic.runtime.framework.vb.HeadlessCommandButton;
import org.siphon.visualbasic.runtime.framework.vb.HeadlessTextBox;

/**
 * Documented in MS-OFORMS.
 * @author jzaddach
 *
 */
public class VBAObject {

	abstract private static class Control {
    	byte minorVersion;
    	byte majorVersion;
    	short size;
    	
    	abstract public String getValue();
	}
	
    private static class MorphDataProps
    {
    	boolean variousPropertyBits;
    	boolean backColor;
    	boolean foreColor;
    	boolean maxLength;
    	boolean borderStyle;
    	boolean scrollBars;
    	boolean displayStyle;
    	boolean mousePointer;
    	boolean size;
    	boolean passwordChar;
    	boolean listWidth;
    	boolean boundColumn;
    	boolean textColumn;
    	boolean columnCount;
    	boolean listRows;
    	boolean columnInfo;
    	boolean matchEntry;
    	boolean listStyle;
    	boolean showDropButtonWhen;
    	boolean dropButtonStyle;
    	boolean multiSelect;
    	boolean value;
    	boolean caption;
    	boolean picturePosition;
    	boolean borderColor;
    	boolean specialEffect;
    	boolean mouseIcon;
    	boolean picture;
    	boolean accelerator;
    	boolean groupName;
    	
    	private MorphDataProps() {}
    	public static MorphDataProps read(DocumentInputStream in) throws IOException {
    		byte[] data = new byte[] {in.readByte(),
    				                  in.readByte(),
    				                  in.readByte(),
    				                  in.readByte(),
    				                  in.readByte(),
    				                  in.readByte(),
    				                  in.readByte(),
    				                  in.readByte()};
    		MorphDataProps props = new MorphDataProps();
    		props.variousPropertyBits = (data[0] & (1 << 0)) != 0;
    		props.backColor = (data[0] & (1 << 1)) != 0;
    		props.foreColor = (data[0] & (1 << 2)) != 0;
    		props.maxLength = (data[0] & (1 << 3)) != 0;
    		props.borderStyle = (data[0] & (1 << 4)) != 0;
    		props.scrollBars = (data[0] & (1 << 5)) != 0;
    		props.displayStyle = (data[0] & (1 << 6)) != 0;
    		props.mousePointer = (data[0] & (1 << 7)) != 0;
    		props.size = (data[1] & (1 << 0)) != 0;
    		props.passwordChar = (data[1] & (1 << 1)) != 0;
    		props.listWidth = (data[1] & (1 << 2)) != 0;
    		props.boundColumn = (data[1] & (1 << 3)) != 0;
    		props.textColumn = (data[1] & (1 << 4)) != 0;
    		props.columnCount = (data[1] & (1 << 5)) != 0;
    		props.listRows = (data[1] & (1 << 6)) != 0;
    		props.columnInfo = (data[1] & (1 << 7)) != 0;
    		props.matchEntry = (data[2] & (1 << 0)) != 0;
    		props.listStyle = (data[2] & (1 << 1)) != 0;
    		props.showDropButtonWhen = (data[2] & (1 << 2)) != 0;
        	//UnusedBits1
    		props.dropButtonStyle = (data[2] & (1 << 4)) != 0;
    		props.multiSelect = (data[2] & (1 << 5)) != 0;
    		props.value = (data[2] & (1 << 6)) != 0;
    		props.caption = (data[2] & (1 << 7)) != 0;
    		props.picturePosition = (data[3] & (1 << 0)) != 0;
    		props.borderColor = (data[3] & (1 << 1)) != 0;
    		props.specialEffect = (data[3] & (1 << 2)) != 0;
    		props.mouseIcon = (data[3] & (1 << 3)) != 0;
    		props.picture = (data[3] & (1 << 4)) != 0;
    		props.accelerator = (data[3] & (1 << 5)) != 0;
        	//UnusedBits2
        	//Reserved
    		props.groupName = (data[4] & (1 << 0)) != 0;
        	//Reserved2[31]
    		return props;
    	}
    }
  
    private static class MorphDataDataBlock
    {
    	int variousPropertyBits;
    	int backColor;
    	int foreColor;
    	int maxLength;
    	int borderStyle;
    	byte scrollBars;
    	byte displayStyle;
    	byte mousePointer;
    	short passwordChar;
    	int listWidth;
    	short boundColumn;
    	short textColumn;
    	short columnCount;
    	short listRows;
    	short columnInfo;
    	byte matchEntry;
    	byte listStyle;
    	byte showDropButtonWhen;
    	byte dropButtonStyle;
    	byte multiSelect;
    	int valueSize;
    	int captionSize;
    	int picturePosition;
    	int borderColor;
    	int specialEffect;
    	short mouseIcon;
    	short picture;
    	short accelerator;
    	int groupNameSize;
    	
    	private MorphDataDataBlock() {}
    	
    	public static MorphDataDataBlock read(DocumentInputStream in, MorphDataProps mask) throws IOException {
    		MorphDataDataBlock block = new MorphDataDataBlock();
    		AlignedDataInput din = new AlignedDataInput(in);
    		if (mask.variousPropertyBits) {block.variousPropertyBits = din.readInt();}
    		if (mask.backColor) {block.backColor = din.readInt();}
    		if (mask.foreColor) {block.foreColor = din.readInt();}
    		if (mask.maxLength) {block.maxLength = din.readInt();}
    		if (mask.borderStyle) {block.borderStyle = din.readByte();}
    		if (mask.scrollBars) {block.scrollBars = din.readByte();}
    		if (mask.displayStyle) {block.displayStyle = din.readByte();}
    		if (mask.mousePointer) {block.mousePointer = din.readByte();}
    		if (mask.passwordChar) {block.passwordChar = din.readShort();}
    		if (mask.listWidth) {block.listWidth = din.readInt();}
    		if (mask.boundColumn) {block.boundColumn = din.readShort();}
    		if (mask.textColumn) {block.textColumn = din.readShort();}
    		if (mask.columnCount) {block.columnCount = din.readShort();}
    		if (mask.listRows) {block.listRows = din.readShort();}
    		if (mask.columnInfo) {block.columnInfo = din.readShort();}
    		if (mask.matchEntry) {block.matchEntry = din.readByte();}
    		if (mask.listStyle) {block.listStyle = din.readByte();}
    		if (mask.showDropButtonWhen) {block.showDropButtonWhen = din.readByte();}
    		if (mask.dropButtonStyle) {block.dropButtonStyle = din.readByte();}
    		if (mask.multiSelect) {block.multiSelect = din.readByte();}
    		if (mask.value) {block.valueSize = din.readInt();}
    		if (mask.caption) {block.captionSize = din.readInt();}
    		if (mask.picturePosition) {block.picturePosition = din.readInt();}
    		if (mask.borderColor) {block.borderColor = din.readInt();}
    		if (mask.specialEffect) {block.specialEffect = din.readInt();}
    		if (mask.mouseIcon) {block.mouseIcon = din.readShort();}
    		if (mask.picture) {block.picture = din.readShort();}
    		if (mask.accelerator) {block.accelerator = din.readShort();}
    		if (mask.groupName) {block.groupNameSize = din.readInt();}
    		
    		while (din.getOffset() % 4 != 0) {
    			din.readByte();	
    		}

    		return block;
    	}
    }
    
    private static class MorphDataExtraBlock
    {
    	protected int width;
    	protected int height;
    	protected String value;
    	protected String caption;
    	protected String groupName;
    	private MorphDataExtraBlock() {}
    	public static MorphDataExtraBlock read(DocumentInputStream in, MorphDataProps props, MorphDataDataBlock data) throws IOException {
    		MorphDataExtraBlock block = new MorphDataExtraBlock();
    		AlignedDataInput din = new AlignedDataInput(in);
    		if (props.size) {
    			block.width = din.readInt();
    			block.height = din.readInt();
    		}
    		if (props.value) {
    			block.value = din.readString(data.valueSize);
    		}
    		if (props.caption) {
    			block.caption = din.readString(data.captionSize);
    		}
    		if (props.groupName) {
    			block.groupName = din.readString(data.groupNameSize);
    		}
    		
    		return block;
    	}
    }
    
    private static class MorphDataControl extends Control
    {
    	MorphDataProps propMask;
    	MorphDataDataBlock dataBlock;
    	MorphDataExtraBlock extraBlock;
    	
    	private MorphDataControl() {}
    	
    	public static MorphDataControl read(DocumentInputStream in) throws IOException {
    		MorphDataControl control = new MorphDataControl();
    		control.minorVersion = in.readByte();
    		control.majorVersion = in.readByte();
    		assert control.majorVersion == 2;
    		assert control.minorVersion == 0;
    		control.size = in.readShort();
    		control.propMask = MorphDataProps.read(in);
    		control.dataBlock = MorphDataDataBlock.read(in, control.propMask);
    		control.extraBlock = MorphDataExtraBlock.read(in, control.propMask, control.dataBlock);
    		return control;
    	}
    	
    	public String getValue() {
    		return extraBlock.value;
    	}
    }
    
    private static class CommandButtonDataBlock
    {
    	int foreColor;
    	int backColor;
    	int variousPropertyBits;
    	int captionSize;
    	int picturePosition;
    	byte mousePointer;
    	short picture;
    	short accelerator;
    	short mouseIcon;
    	
    	public static CommandButtonDataBlock read(DocumentInputStream in, CommandButtonProps mask) throws IOException {
    		CommandButtonDataBlock block = new CommandButtonDataBlock();
    		AlignedDataInput din = new AlignedDataInput(in);
    		if (mask.foreColor) {block.foreColor = din.readInt();}
    		if (mask.backColor) {block.backColor = din.readInt();}
    		if (mask.variousPropertyBits) {block.variousPropertyBits = din.readInt();}
    		if (mask.caption) {block.captionSize = din.readInt();}
    		if (mask.picturePosition) {block.picturePosition = din.readInt();}
    		if (mask.mousePointer) {block.mousePointer = din.readByte();}
    		if (mask.picture) {block.picture = din.readShort();}
    		if (mask.accelerator) {block.accelerator = din.readShort();}
    		if (mask.mouseIcon) {block.mouseIcon = din.readShort();}
    		
    		while (din.getOffset() % 4 != 0) {
    			din.readByte();	
    		}

    		return block;
    	}
    }
    
    private static class CommandButtonExtraBlock
    {
    	protected String caption;
    	protected long size;
    	
    	private CommandButtonExtraBlock() {}
    	public static CommandButtonExtraBlock read(DocumentInputStream in, CommandButtonProps props, CommandButtonDataBlock data) throws IOException {
    		CommandButtonExtraBlock block = new CommandButtonExtraBlock();
    		AlignedDataInput din = new AlignedDataInput(in);
    		if (props.caption) {
    			block.caption = din.readString(data.captionSize);
    		}
    		if (props.size) {
    			block.size = din.readLong();
    		}
    		
    		return block;
    	}
    }
    
    private static class CommandButtonProps
    {
    	boolean foreColor;
    	boolean backColor;
    	boolean variousPropertyBits;
    	boolean caption;
    	boolean picturePosition;
    	boolean size;
    	boolean mousePointer;
    	boolean picture;
    	boolean accelerator;
    	boolean takeFocusOnClick;
    	boolean mouseIcon;
    	
    	private CommandButtonProps() {}
    	public static CommandButtonProps read(DocumentInputStream in) throws IOException {
    		byte[] data = new byte[] {in.readByte(),
    				                  in.readByte(),
    				                  in.readByte(),
    				                  in.readByte()};
    		CommandButtonProps props = new CommandButtonProps();
    		props.foreColor = (data[0] & (1 << 0)) != 0;
    		props.backColor = (data[0] & (1 << 1)) != 0;
    		props.variousPropertyBits = (data[0] & (1 << 2)) != 0;
    		props.caption = (data[0] & (1 << 3)) != 0;
    		props.picturePosition = (data[0] & (1 << 4)) != 0;
    		props.size = (data[0] & (1 << 5)) != 0;
    		props.mousePointer = (data[0] & (1 << 6)) != 0;
    		props.picture = (data[0] & (1 << 7)) != 0;
    		props.accelerator = (data[1] & (1 << 0)) != 0;
    		props.takeFocusOnClick = (data[1] & (1 << 1)) != 0;
    		props.mouseIcon = (data[1] & (1 << 2)) != 0;
    		//UnusedBits
    		return props;
    	}
    }
    
    private static class CommandButtonControl extends Control
    {
    	CommandButtonProps propMask;
    	CommandButtonDataBlock dataBlock;
    	CommandButtonExtraBlock extraBlock;
    	
    	
    	private CommandButtonControl() {}
    	
    	public static CommandButtonControl read(DocumentInputStream in) throws IOException {
    		CommandButtonControl control = new CommandButtonControl();
    		control.minorVersion = in.readByte();
    		control.majorVersion = in.readByte();
    		assert control.majorVersion == 2;
    		assert control.minorVersion == 0;
    		control.size = in.readShort();
    		control.propMask = CommandButtonProps.read(in);
    		control.dataBlock = CommandButtonDataBlock.read(in, control.propMask);
    		control.extraBlock = CommandButtonExtraBlock.read(in, control.propMask, control.dataBlock);
    		return control;
    	}
    	
    	public String getValue() {
    		return extraBlock.caption;
    	}
    }
	
    
	public String userType;
	public String ocxName;
	public Control contents;

	private VBAObject() {
		// TODO Auto-generated constructor stub
	}
	
	public static VBAObject readObject(DirectoryNode entry) throws IOException {
		DocumentNode ocxname = POIFSTools.findDocumentNode(entry, "\u0003OCXNAME");
		DocumentNode contents = POIFSTools.findDocumentNode(entry, "contents");
		DocumentNode compobj = POIFSTools.findDocumentNode(entry, "\u0001CompObj");
		
		if (compobj == null
			|| contents == null
			|| ocxname == null) {
			return null;
		}
		
		VBAObject obj = new VBAObject();
		
		try (DocumentInputStream in = new DocumentInputStream(compobj)) {
			IOUtils.skipFully(in, 4);
			int version = in.readInt();
			IOUtils.skipFully(in,  20);
			int userTypeSize = in.readInt();
			obj.userType = stripTrailingNull(new String(in.readNBytes(userTypeSize), StandardCharsets.ISO_8859_1));
			
		}
		
		try(DocumentInputStream in = new DocumentInputStream(ocxname)) {
			obj.ocxName = stripTrailingNull(new String(in.readAllBytes(), StandardCharsets.UTF_16LE));
		}
	
		try(DocumentInputStream in = new DocumentInputStream(contents)) {
			if (obj.userType.equals("Microsoft Forms 2.0 TextBox")) {
				obj.contents = MorphDataControl.read(in);
			}
			else if (obj.userType.contentEquals("Microsoft Forms 2.0 CommandButton")) {
				obj.contents = CommandButtonControl.read(in);
			}
			else {
				throw new UnsupportedOperationException("Don't know how to parse object type '" + obj.userType + "'");
			}
		}
		
		return obj;
	}
	
	private static String stripTrailingNull(String str) {
		int last = str.length() - 1;
		for (; last > 0 && str.charAt(last) == '\u0000'; last -= 1);
		return str.substring(0, last + 1);
	}
	
	public String getValue() {
		return contents.getValue();
	}
	
	public String getName() {
		StringBuilder name = new StringBuilder();
		for (char c : ocxName.toCharArray()) {
			if (c == '\0') {
				break;
			}
			
			name.append(c);
		}
		
		return name.toString();
	}
	
	public VbBoundObject createInstance() throws VbRuntimeException, ArgumentException {
		if (userType.compareTo("Microsoft Forms 2.0 TextBox") == 0) {
			HeadlessTextBox textBox = new HeadlessTextBox();
			textBox.setText(getValue());
			return textBox;
		}
		else if (userType.compareTo("Microsoft Forms 2.0 CommandButton") == 0) {
			HeadlessCommandButton commandButton = new HeadlessCommandButton();
			commandButton.setCaption(getValue());
			return commandButton;
		}
		else {
			throw new UnsupportedOperationException("Don't know how to instantiate type '" + userType + "'");
		}
	}

}
