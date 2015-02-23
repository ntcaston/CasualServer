package spikedog.casual.server;

import java.util.Objects;

/**
 * Immutable representation of HTTP request line.
 * 
 * See http://www.w3.org/Protocols/rfc2616/rfc2616-sec5.html#sec5.1
 */
public final class RequestLine {
  private final String method;
  private final String uri;
  private final String httpVersion;

  public static RequestLine fromString(String requestLineString) {
    String[] parts = requestLineString.split(" ");
    if (parts.length != 3) {
      throw new IllegalArgumentException(
          "Request line must consist of three space-separated values, got " + requestLineString);
    }
    return new RequestLine(parts[0], parts[1], parts[2]);
  }

  public RequestLine(String method, String uri, String httpVersion) {
    this.method = method;
    this.uri = uri;
    this.httpVersion = httpVersion;
  }

  public String getMethod() {
    return method;
  }

  public String getUri() {
    return uri;
  }

  public String getHttpVersion() {
    return httpVersion;
  }

  @Override
  public String toString() {
    return method + " " + uri + " " + httpVersion;
  }

  @Override
  public boolean equals(Object other) {
    if (other == null) {
      return false;
    }

    if (other == this) {
      return true;
    }

    if (!(other instanceof RequestLine)) {
      return false;
    }
    RequestLine otherLine = (RequestLine) other;
    return method.equals(otherLine.method)
        && uri.equals(otherLine.uri)
        && httpVersion.equals(otherLine.httpVersion);
  }

  @Override
  public int hashCode() {
    return Objects.hash(method, uri, httpVersion);
  }
}
