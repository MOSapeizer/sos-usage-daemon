package packet;

import org.pcap4j.packet.*;
import org.pcap4j.util.ByteArrays;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by zil on 2016/7/18.
 */
public final class HttpPacket extends AbstractPacket {

    private final HttpParser httpParser;

    public static HttpPacket newPacket(
            byte[] rawData, int offset, int length
    ) throws IllegalRawDataException {
        ByteArrays.validateBounds(rawData, offset, length);
        return new HttpPacket(rawData, offset, length);
    }

    private HttpPacket(byte[] rawData, int offset, int length){
        ByteArrayInputStream is = new ByteArrayInputStream(rawData);
        this.httpParser = new HttpParser(is);
    }



    public Builder getBuilder() {
        throw new UnsupportedOperationException();
    }

    public HttpParser getHttpParser() {
        return httpParser;
    }
}
