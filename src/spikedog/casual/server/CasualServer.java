package spikedog.casual.server;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import spikedog.casual.server.util.Constants;

/**
 * Server class intended for handling HTTP requests. Intended for subclassing.
 */
public class CasualServer {
  private static final byte CARRIAGE_RETURN_BYTE = (byte) '\r';
  private static final byte LINE_FEED_BYTE = (byte) '\n';

  private final int port;
  private ServerSocket socket;

  protected CasualServer(int port) {
    this.port = port;
  }

  public final void run() {
    try {
      socket = new ServerSocket(port);
    } catch (Exception e) {
      e.printStackTrace();
      return;
    }

    while (true) {
      try {
        Socket remote = socket.accept();
        final Socket requestSocket = remote;
        Thread requestThread = new Thread(new Runnable() {
          @Override
          public void run() {
            try {
              // TODO make configurable. Socket factory.
              requestSocket.setSoTimeout(10000);
              Request.Builder requestBuilder = new Request.Builder();
              InputStream in = requestSocket.getInputStream();
              RequestLine requestLine = RequestLine.fromString(readLine(in));
              requestBuilder.setRequestLine(requestLine);

              while (true) {
                String headerLine = readLine(in);
                if (headerLine.equals("")) {
                  break;
                }

                int split = headerLine.indexOf(':');
                String name = headerLine.substring(0, split).trim();
                String valueSection = headerLine.substring(split + 1);
                List<String> values = new ArrayList<String>();
                String[] valueParts = valueSection.split(",");
                for (String value : valueParts) {
                  values.add(value.trim());
                }
                requestBuilder.setHeader(name, values);
              }
              requestBuilder.setBody(requestSocket.getInputStream());
              Request request = requestBuilder.build();

              // Assign request to appropriate method.
              String method = request.getRequestLine().getMethod();
              try {
                if (method.equalsIgnoreCase(Constants.METHOD_GET)) {
                  Response response = new Response(requestSocket.getOutputStream());
                  onGet(request, response);
                } else if (method.equalsIgnoreCase(Constants.METHOD_POST)) {
                  Response response = new Response(requestSocket.getOutputStream());
                  onPost(request, response);
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

  public void close() throws IOException {
    socket.close();
  }

  private static String readLine(InputStream stream) throws IOException {
    StringBuilder headerLineBuilder = new StringBuilder();
    int b = -1;

    byte lastByte = -1;
    // Wow! Such efficient!
    while ((b = stream.read()) != -1) {
      if (lastByte == CARRIAGE_RETURN_BYTE && b == LINE_FEED_BYTE) {
        break;
      }
      if (lastByte > -1) {
        headerLineBuilder.append((char) lastByte);
      }
      lastByte = (byte) b;
    }
    return headerLineBuilder.toString();
  }

  // TODO add other methods.
  protected void onGet(Request request, Response response) throws IOException {
  }

  protected void onPost(Request request, Response response) throws IOException {
  }
}
