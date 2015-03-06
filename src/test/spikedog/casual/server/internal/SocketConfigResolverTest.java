package spikedog.casual.server.internal;

import static org.junit.Assert.assertEquals;

import spikedog.casual.server.SocketConfig;

import java.net.Socket;

import org.junit.Before;
import org.junit.Test;

public class SocketConfigResolverTest {
  private boolean defaultKeepAlive;
  private int defaultReceiveBufferSize;
  private int defaultSendBufferSize;
  private int defaultSocketTimeout;
  private boolean defaultTcpNoDelay;

  @Before
  public void setUp() throws Exception {
    Socket socket = new Socket();
    defaultKeepAlive = socket.getKeepAlive();
    defaultReceiveBufferSize = socket.getReceiveBufferSize();
    defaultSendBufferSize = socket.getSendBufferSize();
    defaultSocketTimeout = socket.getSoTimeout();
    defaultTcpNoDelay = socket.getTcpNoDelay();
    socket.close();
  }

  @Test
  public void testResolveSocket() throws Exception {
    SocketConfig config = new SocketConfig.Builder()
        .setKeepAlive(false)
        .setReceiveBufferSize(10000)
        .setSendBufferSize(20000)
        .build();
    Socket socket = new Socket();
    SocketConfigResolver resolver = new SocketConfigResolver(config);
    resolver.resolveSocketConfig(socket);
    assertEquals(false, socket.getKeepAlive());
    assertEquals(10000, socket.getReceiveBufferSize());
    assertEquals(20000, socket.getSendBufferSize());
    assertEquals(defaultSocketTimeout, socket.getSoTimeout());
    assertEquals(defaultTcpNoDelay, socket.getTcpNoDelay());
    socket.close();

    socket = new Socket();
    resolver.resolveSocketConfig(socket);
    resolver.resolveSocketConfig(socket);
    assertEquals(false, socket.getKeepAlive());
    assertEquals(10000, socket.getReceiveBufferSize());
    assertEquals(20000, socket.getSendBufferSize());
    assertEquals(defaultSocketTimeout, socket.getSoTimeout());
    assertEquals(defaultTcpNoDelay, socket.getTcpNoDelay());
    socket.close();
  }

  @Test
  public void testResolveSocket_null() throws Exception {
    Socket socket = new Socket();
    SocketConfigResolver resolver = new SocketConfigResolver(null);
    resolver.resolveSocketConfig(socket);
    assertEquals(defaultKeepAlive, socket.getKeepAlive());
    assertEquals(defaultReceiveBufferSize, socket.getReceiveBufferSize());
    assertEquals(defaultSendBufferSize, socket.getSendBufferSize());
    assertEquals(defaultSocketTimeout, socket.getSoTimeout());
    assertEquals(defaultTcpNoDelay, socket.getTcpNoDelay());
    socket.close();
  }
}
