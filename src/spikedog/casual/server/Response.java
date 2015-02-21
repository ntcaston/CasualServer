package spikedog.casual.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Basic HTTP response representation.
 * 
 * <em>Not</em> thread-safe.
 */
public final class Response {
  private static final byte[] NEW_LINE_BYTES = new String("\n").getBytes();
  // TODO make configurable
  private static final int BUFFER_SIZE = 4096;
  
  private final OutputStream out;
  
  // TODO sorted set?
  private final List<String> headers = new ArrayList<String>();
  private StatusLine statusLine;
  private boolean statusLineWritten = false;
  private boolean begunWritingContent = false;
  private int writtenHeaderCount = 0;
  private InputStream content;
  
  Response(OutputStream out) {
    this.out = out;
  }

  public void setStatusLine(StatusLine statusLine) {
    this.statusLine = statusLine;
  }
  
  public void addHeader(String name, String value) {
    // TODO going to have to actually parse these to know when to close.... maybe?
    // TODO deal with multi-value
    headers.add(name + ": " + value);
  }
  
  public void setContent(InputStream content) {
    this.content = content;
  }
  
  public void flush() throws IOException {
    if (!statusLineWritten && statusLine != null) {
      out.write(statusLine.toString().getBytes());
      out.write(NEW_LINE_BYTES);
    }
    
    if (headers.size() > writtenHeaderCount && !begunWritingContent) {
      for (int headerIndex = writtenHeaderCount; headerIndex < headers.size(); headerIndex++) {
        out.write(headers.get(headerIndex).getBytes());
        out.write(NEW_LINE_BYTES);
      }
      writtenHeaderCount = headers.size();
    }
    
    if (content != null) {
      begunWritingContent = true;
      out.write(NEW_LINE_BYTES);
      int n = 0;
      byte[] buffer = new byte[BUFFER_SIZE];
      while ((n = content.read(buffer)) > 0) {
        out.write(buffer, 0, n);
      }
      out.close();
    }
  }
}