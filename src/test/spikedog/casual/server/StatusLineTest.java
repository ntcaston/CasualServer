package spikedog.casual.server;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class StatusLineTest {
  @Test
  public void testToString() {
    StatusLine statusLine = new StatusLine("HTTP/1.1", 200, "OK");
    assertEquals("HTTP/1.1 200 OK", statusLine.toString());
  }
}