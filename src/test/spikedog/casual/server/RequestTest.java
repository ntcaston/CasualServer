package spikedog.casual.server;

import static org.junit.Assert.assertEquals;

import spikedog.casual.server.util.Constants;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.Test;

public class RequestTest {

  @Test
  public void testRequestLine() {
    // Set one request line.
    Request.Builder builder = new Request.Builder();
    RequestLine requestLine =
        new RequestLine(Constants.METHOD_GET, "/foo", Constants.VERISON_HTTP_1_1);
    builder.setRequestLine(requestLine);
    Request request = builder.build();
    assertEquals(requestLine, request.getRequestLine());

    // Set request line multiple times.
    builder = new Request.Builder();
    builder.setRequestLine(requestLine);
    requestLine =
        new RequestLine(Constants.METHOD_GET, "/bar", Constants.VERISON_HTTP_1_1);
    builder.setRequestLine(requestLine);
    request = builder.build();
    assertEquals(requestLine, request.getRequestLine());
  }

  @Test
  public void testHeaders() {
    // Simple header.
    Request.Builder builder = new Request.Builder();
    String headerName = "X-Foo";
    String headerValue = "Something Profound";
    List<String> headerValuesList = new ArrayList<String>(1);
    headerValuesList.add(headerValue);
    builder.setHeader(headerName, headerValuesList);
    Request request = builder.build();

    assertEquals(headerValue, request.getFirstHeaderValue(headerName));
    assertEquals(headerValuesList, request.getHeaderValues(headerName));
    Map<String, List<String>> headerMap = request.getAllHeaders();
    assertEquals(1, headerMap.size());
    assertEquals(headerValuesList, headerMap.get(headerName.toLowerCase()));

    // Add multi-value header.
    String newHeaderName = "X-Bar";
    String newVal1 = "AbC";
    String newVal2 = "dEf";
    List<String> newHeaderValuesList = new ArrayList<String>(2);
    newHeaderValuesList.add(newVal1);
    newHeaderValuesList.add(newVal2);
    builder.setHeader(newHeaderName, newHeaderValuesList);
    request = builder.build();
    assertEquals(newVal1, request.getFirstHeaderValue(newHeaderName));
    assertEquals(newHeaderValuesList, request.getHeaderValues(newHeaderName));
    headerMap = request.getAllHeaders();
    assertEquals(2, headerMap.size());
    assertEquals(headerValuesList, headerMap.get(headerName.toLowerCase()));
    assertEquals(newHeaderValuesList, headerMap.get(newHeaderName.toLowerCase()));
  }

  @Test
  public void testToString() {
    Request.Builder builder = new Request.Builder();
    RequestLine requestLine =
        new RequestLine(Constants.METHOD_GET, "/foo", Constants.VERISON_HTTP_1_1);
    builder.setRequestLine(requestLine);
    List<String> values = new ArrayList<>();
    values.add("a value");
    builder.setHeader("Header", values);
    Request request = builder.build();
    String expectedString =
        "GET /foo HTTP/1.1\n"
        + "Header: a value";
    assertEquals(expectedString.toLowerCase(), request.toString().toLowerCase());
  }
}
