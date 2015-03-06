package spikedog.casual.server.sample;

import java.io.File;
import java.io.IOException;

import spikedog.casual.server.CasualServer;
import spikedog.casual.server.Request;
import spikedog.casual.server.Response;
import spikedog.casual.server.toolkit.FileServeHelper;

/**
 * Sample server which serves files from a provided root directory.
 */
public class FileHost extends CasualServer {
  private final String fileRootDir;

  /**
   * @param args --root is the root directory to serve from, --port to set the port.
   */
  public static void main(String[] args) {
    int port = 8080;
    String rootDir = null;
    try {
      for (int i = 0; i < args.length; i += 2) {
        String val = args[i];
        if (val.equals("--port")) {
          port = Integer.parseInt(args[i + 1]);
        } else if (val.equals("--root")) {
          rootDir = args[i + 1];
        }
      }
    } catch (Exception e) {
      System.err.println("Server initialisation failed, require even number of args");
      return;
    }

    if (rootDir == null) {
      System.err.println("Root serving directory must be provided via --root arg");
      return;
    }

    System.out.println("Serving files in \"" + rootDir + "\" on port " + port);
    new FileHost("html", 8080).run();
  }

  public FileHost(String fileRootDir, int port) {
    super(port);
    this.fileRootDir = fileRootDir;
  }

  @Override
  protected void onGet(Request request, Response response) throws IOException {
    // TODO deal with serving directory lists.
    String fileUri = request.getRequestLine().getUri();
    if (fileUri.equals("/")) {
      fileUri = "index.html";
    }

    if (fileUri.charAt(0) == '/') {
      fileUri = fileUri.substring(1);
    }

    FileServeHelper.serveFile(new File(fileRootDir, fileUri), response);
  }
}
