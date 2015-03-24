package spikedog.casual.server.internal;

import static org.junit.Assert.assertEquals;
import static spikedog.casual.server.testutils.Streams.stringFromStream;

import spikedog.casual.server.Request;
import spikedog.casual.server.testutils.StringyInputStream;

import java.io.IOException;
import java.io.InputStream;

import org.junit.Test;

public class StreamRequestBuilderTest {
  @Test
  public void testBasic() throws IOException {
    String requestString =
        "GET / HTTP/1.1\r\n"
        + "Host: foo\r\n"
        + "\r\n"
        + "content";
    InputStream stream = new StringyInputStream(requestString);
    Request request = StreamRequestBuilder.buildRequestFromStream(stream);

    // Check request line.
    assertEquals("GET", request.getRequestLine().getMethod());
    assertEquals("/", request.getRequestLine().getUri());
    assertEquals("HTTP/1.1", request.getRequestLine().getHttpVersion());

    // Check header.
    assertEquals(1, request.getHeaderValues("Host").size());
    assertEquals("foo", request.getHeaderValues("Host").get(0));
    assertEquals("foo", request.getFirstHeaderValue("Host"));
    assertEquals(1, request.getAllHeaders().size());

    // Check content.
    assertEquals("content", stringFromStream(request.getBody()));
  }

  @Test
  public void testHeaderSpaces() throws IOException {
    String requestString =
        "GET / HTTP/1.1\r\n"
        + "Host: some host  I guess   \r\n"
        + "Date:    now is the time \r\n"
        + "\r\n"
        + "some content";
    InputStream stream = new StringyInputStream(requestString);
    Request request = StreamRequestBuilder.buildRequestFromStream(stream);

    // Check request line.
    assertEquals("GET", request.getRequestLine().getMethod());
    assertEquals("/", request.getRequestLine().getUri());
    assertEquals("HTTP/1.1", request.getRequestLine().getHttpVersion());

    // Check headers.
    assertEquals(2, request.getAllHeaders().size());

    assertEquals(1, request.getHeaderValues("Host").size());
    assertEquals("some host  I guess", request.getHeaderValues("Host").get(0));
    assertEquals("some host  I guess", request.getFirstHeaderValue("Host"));

    assertEquals(1, request.getHeaderValues("Date").size());
    assertEquals("now is the time", request.getHeaderValues("Date").get(0));
    assertEquals("now is the time", request.getFirstHeaderValue("Date"));

    // Check content.
    assertEquals("some content", stringFromStream(request.getBody()));
  }

  @Test
  public void testMultipleHeaders() throws IOException {
    String requestString =
        "POST /blah.html HTTP/2.0\r\n"
        + "Host: foo\r\n"
        + "Date: now\r\n"
        + "\r\n"
        + "some content";
    InputStream stream = new StringyInputStream(requestString);
    Request request = StreamRequestBuilder.buildRequestFromStream(stream);

    // Check request line.
    assertEquals("POST", request.getRequestLine().getMethod());
    assertEquals("/blah.html", request.getRequestLine().getUri());
    assertEquals("HTTP/2.0", request.getRequestLine().getHttpVersion());

    // Check headers.
    assertEquals(2, request.getAllHeaders().size());

    assertEquals(1, request.getHeaderValues("Host").size());
    assertEquals("foo", request.getHeaderValues("Host").get(0));
    assertEquals("foo", request.getFirstHeaderValue("Host"));

    assertEquals(1, request.getHeaderValues("Date").size());
    assertEquals("now", request.getHeaderValues("Date").get(0));
    assertEquals("now", request.getFirstHeaderValue("Date"));

    // Check content.
    assertEquals("some content", stringFromStream(request.getBody()));
  }

  @Test
  public void testMultiValueHeaders() throws IOException {
    String requestString =
        "PUT /index.html%20 HTTP/1.0\r\n"
        + "Host: foo,bar\r\n"
        + "Date: now,tomorrow, yesterday\r\n"
        + "Accept-Encoding: test\r\n"
        + "\r\n"
        + "some more content";
    InputStream stream = new StringyInputStream(requestString);
    Request request = StreamRequestBuilder.buildRequestFromStream(stream);

    // Check request line.
    assertEquals("PUT", request.getRequestLine().getMethod());
    assertEquals("/index.html%20", request.getRequestLine().getUri());
    assertEquals("HTTP/1.0", request.getRequestLine().getHttpVersion());

    // Check headers.
    assertEquals(3, request.getAllHeaders().size());

    assertEquals(2, request.getHeaderValues("Host").size());
    assertEquals("foo", request.getHeaderValues("Host").get(0));
    assertEquals("bar", request.getHeaderValues("Host").get(1));
    assertEquals("foo", request.getFirstHeaderValue("Host"));

    assertEquals(3, request.getHeaderValues("Date").size());
    assertEquals("now", request.getHeaderValues("Date").get(0));
    assertEquals("tomorrow", request.getHeaderValues("Date").get(1));
    assertEquals("yesterday", request.getHeaderValues("Date").get(2));
    assertEquals("now", request.getFirstHeaderValue("Date"));

    // Check content.
    assertEquals("some more content", stringFromStream(request.getBody()));
  }

  @Test
  public void testMultilineContent() throws IOException {
    String contentString =
        "this is \nsome content\r\n spread over seve\nral lines!\n\n\n";
    String requestString =
        "GET / HTTP/1.1\r\nHost: foo\r\n\r\n" + contentString;
    InputStream stream = new StringyInputStream(requestString);
    Request request = StreamRequestBuilder.buildRequestFromStream(stream);
    assertEquals(contentString, stringFromStream(request.getBody()));
  }
}
