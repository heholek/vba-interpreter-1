package thirdparty.org.apache.poi.poifs.macros;

public class ASCIIUnicodeStringPair {
    private final String ascii;
    private final String unicode;
    private final int pushbackRecordId;

    ASCIIUnicodeStringPair(String ascii, int pushbackRecordId) {
        this.ascii = ascii;
        this.unicode = "";
        this.pushbackRecordId = pushbackRecordId;
    }

    ASCIIUnicodeStringPair(String ascii, String unicode) {
        this.ascii = ascii;
        this.unicode = unicode;
        pushbackRecordId = -1;
    }

    public String getAscii() {
        return ascii;
    }

    public String getUnicode() {
       return unicode;
    }

    public int getPushbackRecordId() {
        return pushbackRecordId;
    }
    
    public String toString() {
    	return unicode;
    }
}
