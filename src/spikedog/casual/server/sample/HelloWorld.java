package spikedog.casual.server.sample;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import spikedog.casual.server.CasualServer;
import spikedog.casual.server.Request;
import spikedog.casual.server.Response;
import spikedog.casual.server.StatusLine;

/**
 * Super lame {@link CasualServer} demo.
 */
public class HelloWorld extends CasualServer{
  private static final String BASIC_CONTENT = "<html><head></head><body><h1>yo!</hi></body></html>";

  public static void main(String[] args) {
    new HelloWorld(8080).run();
  }

  public HelloWorld(int port) {
    super(port);
  }

  @Override
  protected void onGet(Request request, Response response) throws IOException {
    System.out.println(request);
    try {
      byte[] bodyBytes = BASIC_CONTENT.getBytes(StandardCharsets.UTF_8);

      InputStream body = new ByteArrayInputStream(bodyBytes);
      response.setBody(body);

      response.addHeader("Content-Length", "" + bodyBytes.length);
      response.addHeader("Content-Type", "text/html; charset=utf-8");

      StatusLine statusLine = new StatusLine("HTTP/1.1", 200, "OK");
      response.setStatusLine(statusLine);
    } catch (Exception e) {
      StatusLine statusLine = new StatusLine("HTTP/1.1", 500, "Fail");
      response.setStatusLine(statusLine);
      response.setBody(null);
    }
    response.flush();
    System.out.println("\n");
  }
}
