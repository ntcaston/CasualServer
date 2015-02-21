package spikedog.casual.server;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * Server class intended for handling HTTP requests. Intended for subclassing.
 */
public class CasualServer {
  private static final byte CARRIAGE_RETURN_BYTE = (byte) '\r';
  private static final byte LINE_FEED_BYTE = (byte) '\n';
  private final int port;
  
  protected CasualServer(int port) {
    this.port = port;
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

  // TODO deal with threads and such.
  @SuppressWarnings("resource")
  public final void run() {
    ServerSocket socket;
    try {
      socket = new ServerSocket(port);
    } catch (Exception e) {
      e.printStackTrace();
      return;
    }

    while (true) {
      try {
        Socket remote = socket.accept();
        
        // TODO make configurable. Socket factory.
        remote.setSoTimeout(10000);
        Request.Builder requestBuilder = new Request.Builder();
        InputStream in = remote.getInputStream();
        RequestLine requestLine = RequestLine.fromString(readLine(in));
        requestBuilder.setRequestLine(requestLine);
        String headerLine = null;
        Boolean canHaveMessageBody = null;
        
        while (true) {
          
          headerLine = readLine(in);
          if (headerLine.equals("")) {
            break;
          }
          
          int split = headerLine.indexOf(':');
          String name = headerLine.substring(0, split).toLowerCase().trim();
          String valueSection = headerLine.substring(split + 1);
          List<String> values = new ArrayList<String>();
          String[] valueParts = valueSection.split(",");
          for (String value : valueParts) {
            values.add(value.trim());
          }
          
          if (name.equals("content-length")) {
            int contentLength = values.isEmpty() ? 0 : Integer.parseInt(values.get(0));
            if (contentLength == 0 && canHaveMessageBody == null) {
              canHaveMessageBody = false;
            }
            if (canHaveMessageBody == null) {
              canHaveMessageBody = true;
            }
          } else if (name.equals("transfer-encoding") && canHaveMessageBody == null) {
            canHaveMessageBody = true;
          }
          requestBuilder.setHeader(name, values);
        }

        if (canHaveMessageBody != null && canHaveMessageBody) {
          requestBuilder.setContent(remote.getInputStream());
        }
        
        Request request = requestBuilder.build();
        
        String method = request.getRequestLine().getMethod();
        try {
          if (method.equalsIgnoreCase("GET")) {
            Response response = new Response(remote.getOutputStream());
            onGet(request, response);
          } else if (method.equalsIgnoreCase("POST")) {
            Response response = new Response(remote.getOutputStream());
            onPost(request, response);
          }
        } catch (Exception e) {
          System.err.println("Error handling request for " + request.getRequestLine());
          e.printStackTrace();
        }

        remote.close();
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }
  
  // TODO add other methods.
  protected void onGet(Request request, Response response) throws IOException { 
  }
  
  protected void onPost(Request request, Response response) throws IOException {
  }
}
