package spikedog.casual.server;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Basic HTTP request representation.
 * 
 * <em>Not</em> thread-safe.
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
      headers.put(name, values);
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

  private Request(RequestLine requestLine, Map<String, List<String>> headers, InputStream body) {
    this.requestLine = requestLine;
    this.headerMap = headers;
    this.body = body;
  }

  public RequestLine getRequestLine() {
    return requestLine;
  }

  public String getFirstHeaderValue(String key) {
    List<String> vals = headerMap.get(key);
    if (vals == null || vals.isEmpty()) {
      return null;
    }
    return vals.get(0);
  }

  public List<String> getHeaderValues(String key) {
    return headerMap.get(key);
  }

  /** @return The headers of the request. Should not be modified. */
  public Map<String, List<String>> getHeaders() {
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
      builder.append("\n" + header.getKey() + " : " + valueStringBuilder);
    }
    return builder.toString();
  }
}
