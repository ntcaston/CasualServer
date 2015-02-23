package spikedog.casual.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Basic HTTP response representation.
 * 
 * <em>Not</em> thread-safe.
 */
public final class Response {
  // TODO need a state machine to deal with state soup.
  private static final byte[] NEW_LINE_BYTES = new String("\n").getBytes();
  // TODO make configurable
  private static final int BUFFER_SIZE = 4096;

  private final OutputStream out;

  // TODO Storing like this results in a lot of unnecessary list instantiations. Optimise this once
  // this class is well tested.
  private final LinkedHashMap<String, List<String>> headers =
      new LinkedHashMap<String, List<String>>();
  private final Set<String> writtenHeaders = new HashSet<String>();
  private StatusLine statusLine;
  private boolean statusLineWritten = false;
  private boolean begunWritingBody = false;
  private int writtenHeaderCount = 0;
  private InputStream body;

  Response(OutputStream out) {
    this.out = out;
  }

  public void setStatusLine(StatusLine statusLine) {
    if (statusLineWritten) {
      throw new IllegalStateException("Setting status line after status line written to output.");
    }
    this.statusLine = statusLine;
  }

  public void addHeader(String name, String value) {
    if (writtenHeaders.contains(name.toLowerCase())) {
      throw new IllegalStateException(
          "Attempted to change a header which has already been written to output.");
    } else if (begunWritingBody) {
      throw new IllegalStateException("Attempted to set header after message body flushed.");
    }
    if (!headers.containsKey(name)) {
      headers.put(name, new ArrayList<String>(1));
    }
    List<String> valueList = headers.get(name);
    valueList.add(value);
  }

  public void setHeader(String name, String value) {
    if (writtenHeaders.contains(name.toLowerCase())) {
      throw new IllegalStateException(
          "Attempted to set a header which has already been written to output.");
    } else if (begunWritingBody) {
      throw new IllegalStateException("Attempted to set header after message body flushed.");
    }
    List<String> values = new ArrayList<String>(1);
    values.add(value);
    headers.put(name, values);
  }

  public void setBody(InputStream body) {
    if (begunWritingBody) {
      throw new IllegalStateException(
          "Attempted to change message body after begun writing body to output.");
    }
    this.body = body;
  }

  // TODO this is a bit weird... net to check it actually all makes sense and do lots of validation
  // and error throwing and such.
  public void flush() throws IOException {
    if (!statusLineWritten && statusLine != null) {
      out.write(statusLine.toString().getBytes());
      out.write(NEW_LINE_BYTES);
      statusLineWritten = true;
    }

    // TODO make sure this is legit. It's actually possible to write headers after the body in some
    // cases (although maybe these can just be included in the body.
    if (headers.size() > writtenHeaderCount && !begunWritingBody) {
      if (!statusLineWritten) {
        throw new IllegalStateException("Attempted to flush headers before status line set.");
      }
      Iterator<Map.Entry<String, List<String>>> iterator = headers.entrySet().iterator();
      for (int headerIndex = 0; headerIndex < headers.size(); headerIndex++) {
        Map.Entry<String, List<String>> entry = iterator.next();
        if (headerIndex < writtenHeaderCount) {
          continue;
        }

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
        out.write(NEW_LINE_BYTES);
        writtenHeaders.add(name.toLowerCase());
      }
      writtenHeaderCount = headers.size();
    }

    if (body != null && !begunWritingBody) {
      if (!statusLineWritten) {
        throw new IllegalStateException("Attempted to flush message body before status line set.");
      }
      begunWritingBody = true;
      out.write(NEW_LINE_BYTES);
      int n = 0;
      byte[] buffer = new byte[BUFFER_SIZE];
      while ((n = body.read(buffer)) > 0) {
        out.write(buffer, 0, n);
      }
      out.close();
    }
  }
}
