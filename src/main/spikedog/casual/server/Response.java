package spikedog.casual.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Basic HTTP response representation.
 *
 * <p><em>Not</em> thread-safe.
 */
public final class Response {
  // Bytes for carriage return followed by line-feed.
  private static final byte[] CRLF_BYTES = new String("\r\n").getBytes();
  private static final int BUFFER_SIZE = 4096;

  private final OutputStream out;
  private final AtomicBoolean flushed = new AtomicBoolean();
  private final LinkedHashMap<String, List<String>> headers =
      new LinkedHashMap<String, List<String>>();

  private StatusLine statusLine;
  private InputStream body;

  public Response(OutputStream out) {
    this.out = out;
  }

  /**
   * Sets the status line to be written to the underlying output stream.
   *
   * @throws IllegalStateException If this response has already been flushed.
   */
  public void setStatusLine(StatusLine statusLine) {
    if (flushed.get()) {
      throw new IllegalStateException("Attempted to set status line after response flushed.");
    }

    this.statusLine = statusLine;
  }

  /**
   * Adds a header value for a name by appending it to existing values or creating a new value-list
   * if none exists.
   *
   * @throws IllegalStateException If this response has already been flushed.
   */
  public void addHeader(String name, String value) {
    if (flushed.get()) {
      throw new IllegalStateException("Attempted to set header after response flushed.");
    }

    if (!headers.containsKey(name)) {
      headers.put(name, new ArrayList<String>(1));
    }
    List<String> valueList = headers.get(name);
    valueList.add(value);
  }

  /**
   * Sets a header name to a particular value. Overrides any existing value.
   *
   * @throws IllegalStateException If this response has already been flushed.
   */
  public void setHeader(String name, String value) {
    if (flushed.get()) {
      throw new IllegalStateException("Attempted to set a header after response flushed.");
    }

    List<String> values = new ArrayList<String>(1);
    values.add(value);
    headers.put(name, values);
  }

  /**
   * Removes all headers which have been set on this response.
   *
   * @throws IllegalStateException If this response has already been flushed.
   */
  public void clearAllHeaders() {
    if (flushed.get()) {
      throw new IllegalStateException("Attempted to clear headers after response fluhsed.");
    }

    headers.clear();
  }

  /**
   * Sets the message body of the response.
   *
   * @throws IllegalStateException If this response has already been flushed.
   */
  public void setBody(InputStream body) {
    if (flushed.get()) {
      throw new IllegalStateException(
          "Attempted to change message body after begun writing body to output.");
    }

    this.body = body;
  }

  /**
   * Writes all data to the underlying output stream. The request must not be edited after calling
   * this method.
   *
   * <p> Calls to {@code flush()} beyond the first will be ignored.
   *
   * @throws IllegalStateException If no status line has yet been set.
   */
  public void flush() throws IOException {
    if (!flushed.compareAndSet(false, true)) {
      System.out.println("Attempted to flush request twice. flush() ignored.");
      return;
    }

    if (statusLine == null) {
      throw new IllegalStateException("Attempted to flush response with no status line.");
    }

    // Write status line.
    out.write(statusLine.toString().getBytes());
    out.write(CRLF_BYTES);

    // Write headers.
    for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
      String name = entry.getKey();
      List<String> values = entry.getValue();
      StringBuilder valueStringBuilder = new StringBuilder();
      for (int valueIndex = 0; valueIndex < values.size(); valueIndex++) {
        valueStringBuilder.append(values.get(valueIndex));
        if (valueIndex < values.size() - 1) {
          valueStringBuilder.append(",");
        }
      }
      out.write((name + ": " + valueStringBuilder.toString()).getBytes());
      out.write(CRLF_BYTES);
    }

    // Write body.
    if (body != null) {
      out.write(CRLF_BYTES);
      int n = 0;
      byte[] buffer = new byte[BUFFER_SIZE];
      while ((n = body.read(buffer)) > 0) {
        out.write(buffer, 0, n);
      }
      out.close();
    }
  }

  boolean hasFlushed() {
    return flushed.get();
  }
}
