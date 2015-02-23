package spikedog.casual.server;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Test;

public class RequestLineTest {
  @Test
  public void testFromString() {
    RequestLine requestLine = RequestLine.fromString("GET / HTTP/1.1");
    assertEquals(new RequestLine("GET", "/", "HTTP/1.1"), requestLine);

    try {
      requestLine = RequestLine.fromString("GET HTTP/1.1");
      fail("Request line should not be constructable from invalid string.");
    } catch (IllegalArgumentException e) {
      // Expected.
    }
  }
}
