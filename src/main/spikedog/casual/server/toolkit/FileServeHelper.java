package spikedog.casual.server.toolkit;

import spikedog.casual.server.Response;
import spikedog.casual.server.StatusLine;
import spikedog.casual.server.util.Constants;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Assists in serving files as responses.
 */
public final class FileServeHelper {
  private static final Map<String, String> MIME_TYPE_MAP = new HashMap<String, String>();
  static {
    MIME_TYPE_MAP.put("css", "text/css");
    MIME_TYPE_MAP.put("gif", "image/gif");
    MIME_TYPE_MAP.put("html", "text/html; charset=UTF-8");
    MIME_TYPE_MAP.put("jpeg", "image/jpeg");
    MIME_TYPE_MAP.put("jpg", "image/jpeg");
    MIME_TYPE_MAP.put("js", "text/javascript; charset=UTF-8");
    MIME_TYPE_MAP.put("png", "image/png");
    MIME_TYPE_MAP.put("text", "text/plain; charset=UTF-8");
  }

  public static void serveFile(File file, Response response) throws IOException {
    String contentType = null;
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
    if (!file.isFile() || !file.exists()) {
      response.setStatusLine(new StatusLine(Constants.VERISON_HTTP_1_1, 404, "No such resource"));
      response.flush();
      return;
    }

    FileInputStream fileInputStream = null;
    try {
      fileInputStream = new FileInputStream(file);
      long contentLength = fileInputStream.getChannel().size();

      if (contentType != null) {
        response.addHeader(Constants.HEADER_CONTENT_TYPE, contentType);
      }

      response.addHeader(Constants.HEADER_CONTENT_LENGTH, "" + contentLength);
      response.setBody(fileInputStream);
      response.setStatusLine(new StatusLine(Constants.VERISON_HTTP_1_1, 200, "OK"));
      response.flush();
    } catch (Exception e) {
      e.printStackTrace();
      response.setStatusLine(new StatusLine(Constants.VERISON_HTTP_1_1, 500, e.getMessage()));
    } finally {
      if (fileInputStream != null) {
        fileInputStream.close();
      }
    }
  }
}
