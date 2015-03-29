package spikedog.casual.server;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import spikedog.casual.server.testutils.StringyOutputStream;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.junit.Test;

public class ResponseTest {
  @Test
  public void testStatusLine() throws IOException {
    StringyOutputStream outputStream = new StringyOutputStream();
    Response response = new Response(outputStream);
    StatusLine statusLine = new StatusLine("HTTP/1.1", 200, "OK");
    response.setStatusLine(statusLine);
    response.flush();

    assertEquals("HTTP/1.1 200 OK\n", outputStream.getString());

    response.flush();
    assertEquals("HTTP/1.1 200 OK\n", outputStream.getString());

    try {
      response.setStatusLine(new StatusLine("foo", 123, "hello"));
      fail("Changed status line after flushed.");
    } catch (IllegalStateException e) {
      // Expected.
    }
  }

  @Test
  public void testHeaders() throws IOException {
    StringyOutputStream out = new StringyOutputStream();
    Response response = new Response(out);

    // Set status line to avoid IllegalStateException.
    response.setStatusLine(new StatusLine("HTTP/1.1", 200, "OK"));

    response.addHeader("Content-Type", "text/plain");
    response.addHeader("Content-Length", "51");
    response.setHeader("Date", "now");
    response.flush();

    String expected = "HTTP/1.1 200 OK\n" +
        "Content-Type: text/plain\n" +
        "Content-Length: 51\n" +
        "Date: now\n";
    assertEquals(expected, out.getString());
    response.flush();

    try {
      response.addHeader("Content-Encoding", "yup");
      fail("Modified header after flushing to output stream");
    } catch (IllegalStateException e) {
      // Expected.
    }
  }

  @Test
  public void testHeaders_clear() throws IOException {
    StringyOutputStream out = new StringyOutputStream();
    Response response = new Response(out);

    // Set status line to avoid IllegalStateException.
    response.setStatusLine(new StatusLine("HTTP/1.1", 200, "OK"));

    response.addHeader("Content-Type", "text/plain");
    response.addHeader("Content-Type", "UTF-8");
    response.addHeader("Content-Length", "51");

    response.clearAllHeaders();
    response.setHeader("Date", "now");
    response.flush();

    String expected = "HTTP/1.1 200 OK\n" +
        "Date: now\n";
    assertEquals(expected, out.getString());
    response.flush();
  }

  @Test
  public void testMessageBody() throws IOException {
    StringyOutputStream out = new StringyOutputStream();
    Response response = new Response(out);
    response.setStatusLine(new StatusLine("HTTP/1.1", 200, "OK"));
    response.addHeader("Content-Type", "text/plain");
    String content = "This is the message body content";
    InputStream contentStream = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
    response.setBody(contentStream);
    response.flush();

    String expected = "HTTP/1.1 200 OK\nContent-Type: text/plain\n\n" + content;
    assertEquals(expected, out.getString());

    try {
      response.setBody(new ByteArrayInputStream(new byte[] {1, 2, 3, 4}));
      fail("Set message body after flushing");
    } catch (IllegalStateException e) {
      // Expected.
    }
  }
}
