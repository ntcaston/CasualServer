package spikedog.casual.server;

import spikedog.casual.server.internal.SocketConfigResolver;
import spikedog.casual.server.internal.StreamRequestBuilder;
import spikedog.casual.server.util.Constants;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.net.ServerSocketFactory;

/**
 * Server class intended for handling HTTP requests. Intended for subclassing.
 *
 * <p>Currently only compatible with HTTP/1.1.
 */
public abstract class CasualServer {
  private static final StatusLine UNSUPPORTED_METHOD_STATUS =
      new StatusLine(Constants.VERISON_HTTP_1_1, 405, "Method not allowed.");

  private final int port;
  private final ServerSocketFactory socketFactory;
  private final SocketConfigResolver socketConfigResolver;
  private final Executor requestExecutor;

  private ServerSocket socket;

  /**
   * Creates a server which executes all requests on a single thread. Subclasses which are
   * threadsafe may call to {@link CasualServer(int, ExecutorService, SocketConfig)} to use multiple
   * request threads.
   *
   * @param port The port which the server will bind to.
   */
  protected CasualServer(int port) {
    this(port, Executors.newSingleThreadExecutor(), null);
  }

  /**
   * Creates a server with custom configuration.
   *
   * @param port The port which the server will bind to.
   * @param requestExecutor The executor which request methods (i.e. {@link onGet}, {@link onPut})
   *     will be executed on.
   * @param config Configuration to be applied to all server sockets. Values which are not set on
   *     this config will have system defaults used in their place.
   */
  protected CasualServer(int port, ExecutorService requestExecutor, SocketConfig config) {
    this(port, requestExecutor, new SocketConfigResolver(config), ServerSocketFactory.getDefault());
  }

  CasualServer(
      int port,
      ExecutorService requestExecutor,
      SocketConfigResolver configResolver,
      ServerSocketFactory socketFactory) {
    this.port = port;
    this.requestExecutor = requestExecutor;
    this.socketConfigResolver = configResolver;
    this.socketFactory = socketFactory;
  }

  /**
   * Blocking call to run the server on endless loop. May be terminated with a call to
   * {@link #stop()}.
   */
  public final void start() throws IOException {
    socket = socketFactory.createServerSocket(port);

    while (true) {
      try {
        final Socket requestSocket = socket.accept();
        socketConfigResolver.configureSocket(requestSocket);
        requestExecutor.execute(new Runnable() {
          @Override
          public void run() {
            Request request = null;
            Response response = null;
            try {
              response = new Response(requestSocket.getOutputStream());
              request =
                  StreamRequestBuilder.buildRequestFromStream(requestSocket.getInputStream());

              // Assign request to appropriate method.
              String method = request.getRequestLine().getMethod();
              if (method.equalsIgnoreCase(Constants.METHOD_GET)) {
                onGet(request, response);
              } else if (method.equalsIgnoreCase(Constants.METHOD_POST)) {
                onPost(request, response);
              } else if (method.equalsIgnoreCase(Constants.METHOD_PUT)) {
                onPut(request, response);
              } else if (method.equalsIgnoreCase(Constants.METHOD_DELETE)) {
                onDelete(request, response);
              } else if (method.equalsIgnoreCase(Constants.METHOD_HEAD)) {
                onHead(request, response);
              } else if (method.equalsIgnoreCase(Constants.METHOD_OPTIONS)) {
                onOptions(request, response);
              } else if (method.equalsIgnoreCase(Constants.METHOD_TRACE)) {
                onTrace(request, response);
              } else {
                onUnsupportedMethod(request, response);
              }
            } catch (Exception e) {
              if (response != null && !response.hasFlushed()) {
                response.setStatusLine(
                    new StatusLine(Constants.VERISON_HTTP_1_1, 500, "Server error"));
                response.clearAllHeaders();
                response.setBody(null);
                try {
                  response.flush();
                } catch (IOException e1) {
                  // We tried... giving up.
                  e1.printStackTrace();
                }
              }
              System.err.println("Error handling request for " + request);
              e.printStackTrace();
              throw new RuntimeException(e);
            } finally {
              if (requestSocket != null) {
                try {
                  requestSocket.close();
                } catch (IOException e) {
                  e.printStackTrace();
                }
              }
            }
          }
        });
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  public final void stop() throws IOException {
    socket.close();
  }

  /**
   * Should be overriden by subclasses to perform desired operation on an HTTP GET request. By
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
   * Equivalent to {@link #onGet(Request, Response)} for HTTP DELETE.
   */
  protected void onDelete(Request request, Response response) throws IOException {
    onUnsupportedMethod(request, response);
  }

  /**
   * Equivalent to {@link #onGet(Request, Response)} for HTTP HEAD.
   */
  protected void onHead(Request request, Response response) throws IOException {
    onUnsupportedMethod(request, response);
  }

  /**
   * Equivalent to {@link #onGet(Request, Response)} for HTTP OPTIONS.
   */
  protected void onOptions(Request request, Response response) throws IOException {
    onUnsupportedMethod(request, response);
  }

  /**
   * Equivalent to {@link #onGet(Request, Response)} for HTTP TRACE.
   */
  protected void onTrace(Request request, Response response) throws IOException {
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
