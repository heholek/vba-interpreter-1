package thirdparty.org.apache.poi.poifs.macros;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import org.apache.poi.util.IOUtils;
import org.apache.poi.util.POILogFactory;
import org.apache.poi.util.POILogger;

public class DocumentInputStreamWrapper extends InputStream {
	protected static final POILogger LOGGER = POILogFactory.getLogger(DocumentInputStreamWrapper.class);
	
	protected InputStream in;
	
	public DocumentInputStreamWrapper(InputStream in) {
		this.in = in;
		// TODO Auto-generated constructor stub
	}

	@Override
	public int read() throws IOException {
		return in.read();
	}
	
	@Override
	public void close() throws IOException {
		in.close();
	}
	
	public String readMBCS(int firstByte, Charset charset, int maxLength) throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		int len = 0;
		int b = firstByte;
		while (b > 0 && len < maxLength) {
			++len;
			bos.write(b);
			b = IOUtils.readByte(in);
		}
		return new String(bos.toByteArray(), charset);
	}
	
	public String readUnicode(int maxLength) throws IOException {
		//reads null-terminated unicode string
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		int b0 = IOUtils.readByte(in);
		int b1 = IOUtils.readByte(in);

		int read = 2;
		while ((b0 + b1) != 0 && read < maxLength) {

			bos.write(b0);
			bos.write(b1);
			b0 = IOUtils.readByte(in);
			b1 = IOUtils.readByte(in);
			read += 2;
		}
		if (read >= maxLength) {
			LOGGER.log(POILogger.WARN, "stopped reading unicode name after "+read+" bytes");
		}
		return new String (bos.toByteArray(), StandardCharsets.UTF_16LE);
	}
	
	/**
	 * Skips <tt>n</tt> bytes in an input stream, throwing IOException if the
	 * number of bytes skipped is different than requested.
	 * @throws IOException If skipping would exceed the available data or skipping did not work.
	 */
	public void trySkip(long n) throws IOException {
		long skippedBytes = IOUtils.skipFully(in, n);
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
