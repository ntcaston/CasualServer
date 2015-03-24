package spikedog.casual.server;

import spikedog.casual.server.internal.SocketConfigResolver;
import spikedog.casual.server.internal.StreamRequestBuilder;
import spikedog.casual.server.util.Constants;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Server class intended for handling HTTP requests. Intended for subclassing.
 *
 * Currently only compatible with HTTP/1.1.
 */
public abstract class CasualServer {
  private static final StatusLine UNSUPPORTED_METHOD_STATUS =
      new StatusLine(Constants.VERISON_HTTP_1_1, 405, "Method not allowed.");

  private final int port;
  private final SocketConfigResolver socketConfigResolver;

  private ServerSocket socket;

  protected CasualServer(int port) {
    this(port, null);
  }

  protected CasualServer(int port, SocketConfig config) {
    this.port = port;
    socketConfigResolver = new SocketConfigResolver(config);
  }

  /**
   * Blocking call to run the server on endless loop. May be terminated with a call to
   * {@link #stop()}.
   */
  public final void start() throws IOException {
    socket = new ServerSocket(port);

    while (true) {
      Socket requestSocket = null;
      try {
        requestSocket = socket.accept();
        socketConfigResolver.resolveSocketConfig(requestSocket);
        final Socket finalRequestSocket = requestSocket;
        Thread requestThread = new Thread(new Runnable() {
          @Override
          public void run() {
            Request request = null;
            try {
              request =
                  StreamRequestBuilder.buildRequestFromStream(finalRequestSocket.getInputStream());

              // Assign request to appropriate method.
              Response response = new Response(finalRequestSocket.getOutputStream());
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
              System.err.println("Error handling request for " + request);
              e.printStackTrace();
              throw new RuntimeException(e);
            }
          }
        });
        requestThread.setName("request_thread");
        requestThread.setDaemon(true);
        requestThread.start();
      } catch (Exception e) {
        e.printStackTrace();
      } finally {
        if (requestSocket != null) {
          requestSocket.close();
        }
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
}
