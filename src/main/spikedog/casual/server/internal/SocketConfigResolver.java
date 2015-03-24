package spikedog.casual.server.internal;

import spikedog.casual.server.SocketConfig;

import java.net.Socket;
import java.net.SocketException;

/**
 * Applies the config in a {@link SocketConfig} object in an efficient manner for repeated socket
 * configurations.
 *
 * <p>This class is thread-safe.
 */
public final class SocketConfigResolver {
  private final SocketConfig config;

  private boolean configResolved = false;

  private boolean keepAlive;
  private int socketTimeout;
  private int receiveBufferSize;
  private int sendBufferSize;
  private boolean tcpNoDelay;

  public SocketConfigResolver(SocketConfig config) {
    this.config = config;
  }

  /**
   * Configures a socket based on the settings in this resolver's {@link SocketConfig} object. Will
   * use the provided socket to determine defaults for non-configured values if this is the first
   * time the config has been resolved.
   */
  public void configureSocket(Socket socket) throws SocketException {
    if (!configResolved) {
      synchronized (this) {
        if (!configResolved) {
          keepAlive = socket.getKeepAlive();
          socketTimeout = socket.getSoTimeout();
          receiveBufferSize = socket.getReceiveBufferSize();
          sendBufferSize = socket.getSendBufferSize();
          tcpNoDelay = socket.getTcpNoDelay();

          if (config != null) {
            Boolean configKeepAlive = config.getKeepAlive();
            keepAlive = configKeepAlive == null ? socket.getKeepAlive() : configKeepAlive;

            Integer configSocketTimeout = config.getSocketTimeout();
            socketTimeout = configSocketTimeout == null
                ? socket.getSoTimeout() : configSocketTimeout;

            Integer configReceiveBufferSize = config.getReceiveBufferSize();
            receiveBufferSize = configReceiveBufferSize == null
                ? socket.getReceiveBufferSize() : configReceiveBufferSize;

            Integer configSendBufferSize = config.getSendBufferSize();
            sendBufferSize = configSendBufferSize == null
                ? socket.getSendBufferSize() : configSendBufferSize;

            Boolean configTcpNoDelay = config.getTcpNoDelay();
            tcpNoDelay = configTcpNoDelay == null ? socket.getTcpNoDelay() : configTcpNoDelay;
          }
          configResolved = true;
        }
      }
    }

    socket.setKeepAlive(keepAlive);
    socket.setSoTimeout(socketTimeout);
    socket.setReceiveBufferSize(receiveBufferSize);
    socket.setSendBufferSize(sendBufferSize);
    socket.setTcpNoDelay(tcpNoDelay);
  }
}
