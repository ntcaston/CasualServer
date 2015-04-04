package spikedog.casual.server.internal;

import spikedog.casual.server.Request;
import spikedog.casual.server.RequestLine;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Creates {@link Request} by reading from an {@link InputStream}.
 */
public class StreamRequestBuilder {
  private static final byte CARRIAGE_RETURN_BYTE = (byte) '\r';
  private static final byte LINE_FEED_BYTE = (byte) '\n';

  // Static helper methods only.
  private StreamRequestBuilder() {
  }

  /**
   * Constructs a {@link Request} from the data provided by an {@link InputStream}.
   */
  public static Request buildRequestFromStream(InputStream in) throws IOException {
    Request.Builder requestBuilder = new Request.Builder();
    RequestLine requestLine = RequestLine.fromString(readLine(in));
    requestBuilder.setRequestLine(requestLine);

    while (true) {
      String headerLine = readLine(in);
      if (headerLine.equals("")) {
        break;
      }

      int split = headerLine.indexOf(':');
      String name = headerLine.substring(0, split).trim();
      String valueSection = headerLine.substring(split + 1);
      List<String> values = new ArrayList<String>();
      String[] valueParts = valueSection.split(",");
      for (String value : valueParts) {
        values.add(value.trim());
      }
      requestBuilder.setHeader(name, values);
    }

    requestBuilder.setBody(in);
    return requestBuilder.build();
  }

  private static String readLine(InputStream stream) throws IOException {
    StringBuilder headerLineBuilder = new StringBuilder();
    int b = -1;

    byte lastByte = -1;
    // Wow! Such efficient!
    while ((b = stream.read()) != -1) {
      if (lastByte == CARRIAGE_RETURN_BYTE && b == LINE_FEED_BYTE) {
        break;
      }
      if (lastByte > -1) {
        headerLineBuilder.append((char) lastByte);
      }
      lastByte = (byte) b;
    }
    return headerLineBuilder.toString();
  }
}
