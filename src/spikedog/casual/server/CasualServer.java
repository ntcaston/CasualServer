package spikedog.casual.server;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Server class intended for handling HTTP requests. Intended for subclassing.
 */
public class CasualServer {
  private static final byte CARRIAGE_RETURN_BYTE = (byte) '\r';
  private static final byte LINE_FEED_BYTE = (byte) '\n';
  private static final Map<String, String> MIME_TYPE_MAP = new HashMap<String, String>();
  static {
    MIME_TYPE_MAP.put("html", "text/html; charset=utf-8");
    MIME_TYPE_MAP.put("js", "text/javascript; charset=UTF-8");
    MIME_TYPE_MAP.put("jpeg", "image/jpeg");
    MIME_TYPE_MAP.put("jpg", "image/jpeg");
    MIME_TYPE_MAP.put("png", "image/png");
  }

  private final int port;

  protected CasualServer(int port) {
    this.port = port;
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

  // TODO deal with threads and such.
  @SuppressWarnings("resource")
  public final void run() {
    ServerSocket socket;
    try {
      socket = new ServerSocket(port);
    } catch (Exception e) {
      e.printStackTrace();
      return;
    }

    while (true) {
      try {
        Socket remote = socket.accept();

        // TODO make configurable. Socket factory.
        remote.setSoTimeout(10000);
        Request.Builder requestBuilder = new Request.Builder();
        InputStream in = remote.getInputStream();
        RequestLine requestLine = RequestLine.fromString(readLine(in));
        requestBuilder.setRequestLine(requestLine);
        String headerLine = null;
        Boolean canHaveMessageBody = null;

        while (true) {

          headerLine = readLine(in);
          if (headerLine.equals("")) {
            break;
          }

          int split = headerLine.indexOf(':');
          String name = headerLine.substring(0, split).toLowerCase().trim();
          String valueSection = headerLine.substring(split + 1);
          List<String> values = new ArrayList<String>();
          String[] valueParts = valueSection.split(",");
          for (String value : valueParts) {
            values.add(value.trim());
          }

          if (name.equals("content-length")) {
            int contentLength = values.isEmpty() ? 0 : Integer.parseInt(values.get(0));
            if (contentLength == 0 && canHaveMessageBody == null) {
              canHaveMessageBody = false;
            }
            if (canHaveMessageBody == null) {
              canHaveMessageBody = true;
            }
          } else if (name.equals("transfer-encoding") && canHaveMessageBody == null) {
            canHaveMessageBody = true;
          }
          requestBuilder.setHeader(name, values);
        }

        if (canHaveMessageBody != null && canHaveMessageBody) {
          requestBuilder.setContent(remote.getInputStream());
        }

        Request request = requestBuilder.build();

        String method = request.getRequestLine().getMethod();
        try {
          if (method.equalsIgnoreCase("GET")) {
            Response response = new Response(remote.getOutputStream());
            onGet(request, response);
          } else if (method.equalsIgnoreCase("POST")) {
            Response response = new Response(remote.getOutputStream());
            onPost(request, response);
          }
        } catch (Exception e) {
          System.err.println("Error handling request for " + request.getRequestLine());
          e.printStackTrace();
        }

        remote.close();
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  // TODO add other methods.
  protected void onGet(Request request, Response response) throws IOException {
  }

  protected void onPost(Request request, Response response) throws IOException {
  }


  protected final void serveFile(File file, Response response) throws IOException {
    String contentType = "text/plain; charset=utf-8";
    String path = file.getPath();
    int extensionSplit = path.lastIndexOf('.');
    if (extensionSplit > 0 && extensionSplit < path.length() - 1) {
      String extension = path.substring(path.lastIndexOf('.') + 1);
      if (MIME_TYPE_MAP.containsKey(extension.toLowerCase())) {
        contentType = MIME_TYPE_MAP.get(extension);
      }
    }
    // TODO set up a map from file extensions to content-type
    serveFile(file, response, contentType);
  }

  protected final void serveFile(File file, Response response, String contentType)
      throws IOException {
    // TODO fix terrible security.
    if (!file.exists()) {
      response.setStatusLine(new StatusLine("HTTP/1.1", 404, "No such resource"));
      response.flush();
    }

    InputStream fileInputStream = null;
    try {
      fileInputStream = new FileInputStream(file);
      // TODO be more efficient than this.
      List<ByteBuffer> contentParts = new ArrayList<ByteBuffer>();
      byte[] buffer = new byte[4096];
      int n = 0;
      int totalSize = 0;
      while ((n = fileInputStream.read(buffer)) != -1) {
        contentParts.add(ByteBuffer.wrap(Arrays.copyOf(buffer, n)));
        totalSize += n;
      }
      ByteBuffer content = ByteBuffer.allocate(totalSize);
      for (ByteBuffer contentPart : contentParts) {
        content.put(contentPart);
      }

      if (contentType != null) {
        response.addHeader("Content-Type", contentType);
      }
      response.addHeader("Content-Length", "" + totalSize);
      response.setContent(new ByteArrayInputStream(content.array()));
      response.setStatusLine(new StatusLine("HTTP/1.1", 200, "OK"));
    } catch (Exception e) {
      e.printStackTrace();
      response.setStatusLine(new StatusLine("HTTP/1.1", 500, e.getMessage()));
    } finally {
      if (fileInputStream != null) {
        fileInputStream.close();
      }
    }
    response.flush();
  }
}
