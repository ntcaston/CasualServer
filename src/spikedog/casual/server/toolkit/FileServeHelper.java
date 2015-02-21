package spikedog.casual.server.toolkit;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import spikedog.casual.server.Response;
import spikedog.casual.server.StatusLine;

public final class FileServeHelper {
  public static final String JSON_CONTENT_TYPE = "application/json; charset=UTF-8";

  private static final Map<String, String> MIME_TYPE_MAP = new HashMap<String, String>();
  static {
    MIME_TYPE_MAP.put("css", "text/css; charset=UTF-8");
    MIME_TYPE_MAP.put("gif", "image/gif");
    MIME_TYPE_MAP.put("html", "text/html; charset=utf-8");
    MIME_TYPE_MAP.put("jpeg", "image/jpeg");
    MIME_TYPE_MAP.put("jpg", "image/jpeg");
    MIME_TYPE_MAP.put("js", "text/javascript; charset=UTF-8");
    MIME_TYPE_MAP.put("png", "image/png");
    MIME_TYPE_MAP.put("text", "text/plain; charset=UTF-8");
  }

  public static void serveFile(File file, Response response) throws IOException {
    String contentType = "text/plain; charset=utf-8";
    String path = file.getPath();
    int extensionSplit = path.lastIndexOf('.');
    if (extensionSplit > 0 && extensionSplit < path.length() - 1) {
      String extension = path.substring(path.lastIndexOf('.') + 1);
      if (MIME_TYPE_MAP.containsKey(extension.toLowerCase())) {
        contentType = MIME_TYPE_MAP.get(extension);
      }
    }
    serveFile(file, response, contentType);
  }

  public static void serveFile(File file, Response response, String contentType)
      throws IOException {
    // TODO fix terrible security.
    if (!file.exists()) {
      response.setStatusLine(new StatusLine("HTTP/1.1", 404, "No such resource"));
      response.flush();
    }

    FileInputStream fileInputStream = null;
    try {
      fileInputStream = new FileInputStream(file);
      long contentLength = fileInputStream.getChannel().size();

      if (contentType != null) {
        response.addHeader("Content-Type", contentType);
      }
      response.addHeader("Content-Length", "" + contentLength);
      response.setContent(fileInputStream);
      response.setStatusLine(new StatusLine("HTTP/1.1", 200, "OK"));
    } catch (Exception e) {
      e.printStackTrace();
      response.setStatusLine(new StatusLine("HTTP/1.1", 500, e.getMessage()));
    }
    response.flush();
    if (fileInputStream != null) {
      fileInputStream.close();
    }
  }
}
