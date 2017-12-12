package thirdparty.org.apache.poi.poifs.macros;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

import org.apache.poi.util.IOUtils;
import org.apache.poi.util.POILogFactory;
import org.apache.poi.util.POILogger;
import org.apache.poi.util.RLEDecompressingInputStream;
import org.apache.poi.util.StringUtil;

public class ExtendedRLEDecompressingInputStream extends RLEDecompressingInputStream {
	//arbitrary limit on size of strings to read, etc.
	private static final int MAX_STRING_LENGTH = 20000;
	
	protected static POILogger LOGGER = POILogFactory.getLogger(ExtendedRLEDecompressingInputStream.class);
	
	public ExtendedRLEDecompressingInputStream(InputStream in) throws IOException {
		super(in);
	}
	
	public ASCIIUnicodeStringPair readStringPair(Charset charset, int reservedByte) throws IOException {
		return readStringPair(charset, reservedByte, true);
	}

	public ASCIIUnicodeStringPair readStringPair(Charset charset, int reservedByte,
			boolean throwOnUnexpectedReservedByte) throws IOException {
		int nameLength = readInt();
		String ascii = readString(nameLength, charset);
		int reserved = readShort();

		if (reserved != reservedByte) {
			if (throwOnUnexpectedReservedByte) {
				throw new IOException("Expected " + Integer.toHexString(reservedByte) +
						"after name before Unicode name, but found: " +
						Integer.toHexString(reserved));
			} else {
				return new ASCIIUnicodeStringPair(ascii, reserved);
			}
		}
		int unicodeNameRecordLength = readInt();
		String unicode = readUnicodeString(unicodeNameRecordLength);
		return new ASCIIUnicodeStringPair(ascii, unicode);
	}

	/**
	 * Read <tt>length</tt> bytes of MBCS (multi-byte character set) characters from the stream
	 *
	 * @param stream the inputstream to read from
	 * @param length number of bytes to read from stream
	 * @param charset the character set encoding of the bytes in the stream
	 * @return a java String in the supplied character set
	 * @throws IOException If reading from the stream fails
	 */
	public String readString(int length, Charset charset) throws IOException {
		byte[] buffer = IOUtils.safelyAllocate(length, MAX_STRING_LENGTH);
		int bytesRead = IOUtils.readFully(this, buffer);
		if (bytesRead != length) {
			throw new IOException("Tried to read: "+length +
					", but could only read: "+bytesRead);
		}
		return new String(buffer, 0, length, charset);
	}
	
	public String readUnicodeString(int unicodeNameRecordLength) throws IOException {
		byte[] buffer = IOUtils.safelyAllocate(unicodeNameRecordLength, MAX_STRING_LENGTH);
		int bytesRead = IOUtils.readFully(this, buffer);
		if (bytesRead != unicodeNameRecordLength) {
			throw new EOFException();
		}
		return new String(buffer, StringUtil.UTF16LE);
	}
	
	/**
	 * Skips <tt>n</tt> bytes in an input stream, throwing IOException if the
	 * number of bytes skipped is different than requested.
	 * @throws IOException If skipping would exceed the available data or skipping did not work.
	 */
	public void trySkip(long n) throws IOException {
		long skippedBytes = IOUtils.skipFully(this, n);
		if (skippedBytes != n) {
			if (skippedBytes < 0) {
				throw new IOException(
						"Tried skipping " + n + " bytes, but no bytes were skipped. "
								+ "The end of the stream has been reached or the stream is closed.");
			} else {
				throw new IOException(
						"Tried skipping " + n + " bytes, but only " + skippedBytes + " bytes were skipped. "
								+ "This should never happen with a non-corrupt file.");
			}
		}
	}
	
}
