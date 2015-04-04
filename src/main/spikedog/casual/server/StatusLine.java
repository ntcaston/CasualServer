package spikedog.casual.server;

import java.util.Objects;

/**
 * Immutable representation of HTTP status line. {@link #toString} returns a string suitable for use
 * in an HTTP response.
 *
 * <p>See http://www.w3.org/Protocols/rfc2616/rfc2616-sec6.html#sec6.1
 */
public final class StatusLine {
  private final String httpVersion;
  private final int statusCode;
  private final String reasonPhrase;

  public StatusLine(String httpVersion, int statusCode, String reasonPhrase) {
    this.httpVersion = httpVersion;
    this.statusCode = statusCode;
    this.reasonPhrase = reasonPhrase;
  }

  @Override
  public String toString() {
    return String.format("%s %s %s", httpVersion, statusCode, reasonPhrase);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }

    if (obj == this) {
      return true;
    }

    if (!(obj instanceof StatusLine)) {
      return false;
    }

    StatusLine statusLine = (StatusLine) obj;
    return httpVersion.equals(statusLine.httpVersion)
        && statusCode == statusLine.statusCode
        && reasonPhrase.equals(statusLine.reasonPhrase);
  }

  @Override
  public int hashCode() {
    return Objects.hash(httpVersion, statusCode, reasonPhrase);
  }
}
