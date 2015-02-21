package spikedog.casual.server.sample;

import java.io.File;
import java.io.IOException;

import spikedog.casual.server.CasualServer;
import spikedog.casual.server.Request;
import spikedog.casual.server.Response;
import spikedog.casual.server.toolkit.FileServeHelper;

public class FileHost extends CasualServer {
  private final String fileRootDir;

  protected FileHost(String fileRootDir, int port) {
    super(port);
    this.fileRootDir = fileRootDir;
  }

  @Override
  protected void onGet(Request request, Response response) throws IOException {
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
