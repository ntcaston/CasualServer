package spikedog.casual.server.toolkit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import spikedog.casual.server.Response;
import spikedog.casual.server.testutils.StringyOutputStream;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.junit.Test;

public class FileServeHelperTest {
  @Test
  public void testFileNotFound() throws IOException {
    File f = new File("somepath");
    StringyOutputStream out = new StringyOutputStream();
    Response response = new Response(out);
    FileServeHelper.serveFile(f, response, "abyss");

    // Assert that the message is 404 and that some reason is given (don't care what).
    String expectedResponseStart = "HTTP/1.1 404";
    assertEquals(
        expectedResponseStart, out.getString().substring(0, expectedResponseStart.length()));
    assertTrue(out.getString().length() > expectedResponseStart.length());
  }

  @Test
  public void testExplicitContentType() throws IOException {
    String contentString = "!@$#^@$#%@#$@!$";
    File f = createFakeFile("foo.bar", contentString.getBytes(StandardCharsets.UTF_8));
    f.deleteOnExit();
    StringyOutputStream out = new StringyOutputStream();
    Response response = new Response(out);
    FileServeHelper.serveFile(f, response, "madness");
    String expectedResponseString =
        "HTTP/1.1 200 OK\n"
        + "Content-Type: madness\n"
        + "Content-Length: " + contentString.length() + "\n"
        + "\n"
        + contentString;
    assertEquals(expectedResponseString, out.getString());
  }

  @Test
  public void testInferredContentType_Js() throws IOException {
    String contentString = "(function() {window.console.log(\"hello!\");})();";
    File f = createFakeFile("foo.js", contentString.getBytes(StandardCharsets.UTF_8));
    f.deleteOnExit();
    StringyOutputStream out = new StringyOutputStream();
    Response response = new Response(out);
    FileServeHelper.serveFile(f, response);
    String expectedResponseString =
        "HTTP/1.1 200 OK\n"
        + "Content-Type: text/javascript; charset=UTF-8\n"
        + "Content-Length: " + contentString.length() + "\n"
        + "\n"
        + contentString;
    assertEquals(expectedResponseString, out.getString());
  }

  @Test
  public void testInferredContentType_Html() throws IOException {
    String contentString = "<!DOCTYPE html><html><head></head><body><h1>HI!</h1></body></html>";
    File f = createFakeFile("index.html", contentString.getBytes(StandardCharsets.UTF_8));
    f.deleteOnExit();
    StringyOutputStream out = new StringyOutputStream();
    Response response = new Response(out);
    FileServeHelper.serveFile(f, response);
    String expectedResponseString =
        "HTTP/1.1 200 OK\n"
        + "Content-Type: text/html; charset=UTF-8\n"
        + "Content-Length: " + contentString.length() + "\n"
        + "\n"
        + contentString;
    assertEquals(expectedResponseString, out.getString());
  }

  private File createFakeFile(String name, byte[] content) throws IOException {
    FileOutputStream out = null;
    try {
      File result = new File(name);
      out = new FileOutputStream(result);
      InputStream in = new ByteArrayInputStream(content);
      byte[] buffer = new byte[4096];
      int n = -1;
      while ((n = in.read(buffer)) > 0) {
        out.write(buffer, 0, n);
      }
      return result;
    } finally {
      if (out != null) {
        out.close();
      }
    }
  }
}
