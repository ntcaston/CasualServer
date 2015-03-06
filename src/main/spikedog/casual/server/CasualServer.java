package spikedog.casual.server;

import spikedog.casual.server.internal.StreamRequestBuilder;
import spikedog.casual.server.util.Constants;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.atomic.AtomicBoolean;


/**
 * Server class intended for handling HTTP requests. Intended for subclassing.
 *
 * Currently only compatible with HTTP/1.1.
 */
public abstract class CasualServer {
  private static final StatusLine UNSUPPORTED_METHOD_STATUS =
      new StatusLine(Constants.VERISON_HTTP_1_1, 405, "Method not allowed.");

  private final int port;
  private final SocketConfig socketConfig;
  private ServerSocket socket;

  // Resolution of socket config values.
  private final AtomicBoolean configResolved = new AtomicBoolean();
  private boolean keepAlive;
  private int socketTimeout;
  private int receiveBufferSize;
  private int sendBufferSize;
  private boolean tcpNoDelay;

  protected CasualServer(int port) {
    this(port, null);
  }

  protected CasualServer(int port, SocketConfig config) {
    this.port = port;
    this.socketConfig = config;
  }

  /**
   * Blocking call to run the server on endless loop. May be terminated with a call to
   * {@link #stop()}.
   */
  public final void run() {
    try {
      socket = new ServerSocket(port);
    } catch (Exception e) {
      e.printStackTrace();
      return;
    }

    while (true) {
      try {
        final Socket requestSocket = socket.accept();
        resolveSocketConfig(requestSocket);
        Thread requestThread = new Thread(new Runnable() {
          @Override
          public void run() {
            try {
              Request request =
                  StreamRequestBuilder.buildRequestFromStream(requestSocket.getInputStream());

              // Assign request to appropriate method.
              try {
                Response response = new Response(requestSocket.getOutputStream());
                String method = request.getRequestLine().getMethod();
                if (method.equalsIgnoreCase(Constants.METHOD_GET)) {
                  onGet(request, response);
                } else if (method.equalsIgnoreCase(Constants.METHOD_POST)) {
                  onPost(request, response);
                } else if (method.equalsIgnoreCase(Constants.METHOD_PUT)) {
                  onPut(request, response);
                } else {
                  onUnsupportedMethod(request, response);
                }
              } catch (Exception e) {
                System.err.println("Error handling request for " + request.getRequestLine());
                e.printStackTrace();
              }

              requestSocket.close();
            } catch (Exception e) {
              e.printStackTrace();
            }
          }
        });
        requestThread.setName("request_thread");
        requestThread.setDaemon(true);
        requestThread.start();
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  public void stop() throws IOException {
    socket.close();
  }

  /**
   * Should be overriden by subclasses to perform expected operation on an HTTP GET request. By
   * default this method will call {@link #onUnsupportedMethod(Request, Response)}.
   */
  protected void onGet(Request request, Response response) throws IOException {
    onUnsupportedMethod(request, response);
  }

  /**
   * Equivalent to {@link #onGet(Request, Response)} for HTTP POST.
   */
  protected void onPost(Request request, Response response) throws IOException {
    onUnsupportedMethod(request, response);
  }

  /**
   * Equivalent to {@link #onGet(Request, Response)} for HTTP PUT.
   */
  protected void onPut(Request request, Response response) throws IOException {
    onUnsupportedMethod(request, response);
  }

  /**
   * Writes a response with a 405 status code. May be overriden by subclasses to dictate the
   * behaviour.
   */
  protected void onUnsupportedMethod(Request request, Response response) throws IOException {
    response.setStatusLine(UNSUPPORTED_METHOD_STATUS);
    response.flush();
  }

  /**
   * Configures a socket based on the settings in this server's {@link SocketConfig} object. Will
   * use the provided socket to determine defaults for non-configured values if this is the first
   * time the config has been resolved.
   */
  private void resolveSocketConfig(Socket socket) throws SocketException {
    if (!configResolved.get()) {
      synchronized (configResolved) {
        if (!configResolved.get()) {
          keepAlive = socket.getKeepAlive();
          socketTimeout = socket.getSoTimeout();
          receiveBufferSize = socket.getReceiveBufferSize();
          sendBufferSize = socket.getSendBufferSize();
          tcpNoDelay = socket.getTcpNoDelay();

          if (socketConfig != null) {
            Boolean configKeepAlive = socketConfig.getKeepAlive();
            keepAlive = configKeepAlive == null ? socket.getKeepAlive() : configKeepAlive;

            Integer configSocketTimeout = socketConfig.getSocketTimeout();
            socketTimeout = configSocketTimeout == null
                ? socket.getSoTimeout() : configSocketTimeout;

            Integer configReceiveBufferSize = socketConfig.getReceiveBufferSize();
            receiveBufferSize = configReceiveBufferSize == null
                ? socket.getReceiveBufferSize() : configReceiveBufferSize;

            Integer configSendBufferSize = socketConfig.getSendBufferSize();
            sendBufferSize = configSendBufferSize == null
                ? socket.getSendBufferSize() : configSendBufferSize;

            Boolean configTcpNoDelay = socketConfig.getTcpNoDelay();
            tcpNoDelay = configTcpNoDelay == null ? socket.getTcpNoDelay() : configTcpNoDelay;
          }
          configResolved.set(true);
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
