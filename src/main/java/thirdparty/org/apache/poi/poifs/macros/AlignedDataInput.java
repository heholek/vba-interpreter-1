package thirdparty.org.apache.poi.poifs.macros;

import java.io.DataInput;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.apache.poi.poifs.filesystem.DocumentInputStream;

public class AlignedDataInput {
	protected DocumentInputStream in;
	protected int offset = 0;
	
	public AlignedDataInput(DocumentInputStream in) {
		this.in = in;
	}
	
	public long readLong() throws IOException {
		while (offset % 4 != 0) {
			readByte();
		}
		offset += 8;
		return in.readLong();
	}
	
	public int readInt() throws IOException {
		while (offset % 4 != 0) {
			readByte();
		}
		offset += 4;
		return in.readInt();
	}
	
	public byte readByte() throws IOException {
		offset += 1;
		return in.readByte();
	}
	
	public int getOffset() {
		return offset;
	}
	
	public short readShort() throws IOException {
		if (offset % 2 != 0) {
			readByte();
		}
		offset += 2;
		return in.readShort();
	}
	
	public byte[] readNBytes(int n) throws IOException {
		byte[] bytes = new byte[n];
		for (int i = 0; i < n; ++i) {
			bytes[i] = readByte();
		}
		
		return bytes;
	}
	
	public String readString(int size) throws IOException {
		boolean compressed = (size & (1 << 31)) != 0;
		size = size & ((1 << 31) - 1);
		int rawSize = compressed ? 2 * size : size;
		byte[] data = new byte[rawSize];
		if (compressed) {
			for (int i = 0; i < size; ++i) {
				data[i * 2] = readByte();
				data[i * 2 + 1] = 0;
			}
		}
		else {
			for (int i = 0; i < size; ++i) {
				data[i] = readByte();
			}
		}
		
		return new String(data, StandardCharsets.UTF_16LE);
	}
	
	

}
