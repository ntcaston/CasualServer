package spikedog.casual.server;

/**
 * Immutable representation of HTTP status line. {@link #toString} returns a string suitable for use
 * in an HTTP response.
 *
 * See http://www.w3.org/Protocols/rfc2616/rfc2616-sec6.html#sec6.1
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
    return httpVersion + " " + statusCode + " " + reasonPhrase;
  }
}
