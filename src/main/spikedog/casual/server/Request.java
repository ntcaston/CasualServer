package spikedog.casual.server;

import spikedog.casual.server.util.Constants;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Basic HTTP request representation.
 *
 * <p><em>Not</em> thread-safe.
 */
public final class Request {
  public static final class Builder {
    private RequestLine requestLine;
    private InputStream body;
    private final Map<String, List<String>> headers = new HashMap<String, List<String>>();

    public void setRequestLine(RequestLine requestLine) {
      this.requestLine = requestLine;
    }

    public void setHeader(String name, List<String> values) {
      headers.put(name.toLowerCase(), values);
    }

    public void setBody(InputStream body) {
      this.body = body;
    }

    public Request build() {
      return new Request(requestLine, headers, body);
    }
  }

  private final RequestLine requestLine;
  private final Map<String, List<String>> headerMap;
  private final InputStream body;

  /**
   * @param headers Map from header name to values. Name must be lower-case.
   */
  private Request(RequestLine requestLine, Map<String, List<String>> headers, InputStream body) {
    this.requestLine = requestLine;
    this.headerMap = headers;
    this.body = body;
  }

  public RequestLine getRequestLine() {
    return requestLine;
  }

  public String getFirstHeaderValue(String key) {
    List<String> vals = getHeaderValues(key);
    if (vals == null || vals.isEmpty()) {
      return null;
    }
    return vals.get(0);
  }

  /**
   * @return The value of the content length header. If not known, or the value cannot be parsed,
   *     returns -1.
   */
  public long getContentLength() {
    String contentLengthString = getFirstHeaderValue(Constants.HEADER_CONTENT_LENGTH);
    if (contentLengthString == null) {
      return -1;
    }

    long result = -1;
    try {
      result = Long.parseLong(contentLengthString);
    } catch (NumberFormatException e) {
      // Unable to parse long. Leave result as -1 to represent unknown content length.
    }
    return result;
  }

  /**
   * @return List of values for this header. Should not be modified.
   */
  public List<String> getHeaderValues(String key) {
    return headerMap.get(key.toLowerCase());
  }

  /**
   * @return The headers of the request. Should not be modified. Header names are stored in
   *     lower-case for case-insensitive lookup.
   */
  public Map<String, List<String>> getAllHeaders() {
    return headerMap;
  }

  public InputStream getBody() {
    return body;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder(requestLine.toString());
    for (Map.Entry<String, List<String>> header : headerMap.entrySet()) {
      StringBuilder valueStringBuilder = new StringBuilder();
      for (int i = 0; i < header.getValue().size(); i++) {
        String valuePart = header.getValue().get(i);
        valueStringBuilder.append(valuePart);
        if (i < header.getValue().size() - 1) {
          valueStringBuilder.append(",");
        }
      }
      builder.append("\n" + header.getKey() + ": " + valueStringBuilder);
    }
    return builder.toString();
  }
}
