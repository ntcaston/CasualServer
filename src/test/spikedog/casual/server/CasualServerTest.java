package spikedog.casual.server;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import spikedog.casual.server.internal.SocketConfigResolver;
import spikedog.casual.server.testutils.Streams;
import spikedog.casual.server.testutils.StringyInputStream;
import spikedog.casual.server.testutils.StringyOutputStream;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import javax.net.ServerSocketFactory;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * TODO: This test is quite badly written but was hacked together in the interest of just getting
 * the thing tested. Should really fix this at some point.
 */
public class CasualServerTest {
  private final int testPort = 1234;

  private TestServer server;
  private FakeServerSocketFactory fakeServerSocketFactory;

  @Before
  public void setUp() throws Exception {
    fakeServerSocketFactory = new FakeServerSocketFactory();
    server = new TestServer(
        testPort,
        new CurrentThreadExecutorService(),
        new SocketConfigResolver(null),
        fakeServerSocketFactory);
  }

  @After
  public void tearDown() throws Exception {
    server.stop();
  }

  @Test
  public void testBasicGet() throws Exception {
    FakeServerSocket serverSocket = prepareServerSocket(fakeServerSocketFactory, testPort);
    startServer(server);

    String requestString =
        "GET / HTTP/1.1\r\n"
        + "Host: foo\r\n"
        + "\r\n"
        + "content";
    makeFakeRequest(serverSocket, requestString);

    Thread.sleep(500);

    Request get = server.getLastGet();
    assertNotNull(get);
    assertEquals(get.getRequestLine().getMethod(), "GET");
    assertEquals(get.getRequestLine().getHttpVersion(), "HTTP/1.1");
    assertEquals(get.getRequestLine().getUri(), "/");
    assertEquals(get.getFirstHeaderValue("hoSt"), "foo");
    assertEquals(Streams.stringFromStream(get.getBody()), "content");
  }

  @Test
  public void testBasicPost() throws Exception {
    FakeServerSocket serverSocket = prepareServerSocket(fakeServerSocketFactory, testPort);
    startServer(server);

    String requestString =
        "POST / HTTP/1.1\r\n"
        + "time: never,ever\r\n"
        + "\r\n"
        + "foo";
    makeFakeRequest(serverSocket, requestString);

    Thread.sleep(500);

    Request post = server.getLastPost();
    assertNotNull(post);
    assertEquals(post.getRequestLine().getMethod(), "POST");
    assertEquals(post.getRequestLine().getHttpVersion(), "HTTP/1.1");
    assertEquals(post.getRequestLine().getUri(), "/");
    assertEquals(post.getFirstHeaderValue("time"), "never");
    assertEquals(Streams.stringFromStream(post.getBody()), "foo");
  }

  @Test
  public void testBasicPut() throws Exception {
    FakeServerSocket serverSocket = prepareServerSocket(fakeServerSocketFactory, testPort);
    startServer(server);

    String requestString =
        "PUT /create HTTP/1.1\r\n"
        + "time: never,ever\r\n"
        + "content-encoding: gzip\r\n"
        + "\r\n"
        + "foo\nbar";
    makeFakeRequest(serverSocket, requestString);

    Thread.sleep(500);

    Request put = server.getLastPut();
    assertNotNull(put);
    assertEquals(put.getRequestLine().getMethod(), "PUT");
    assertEquals(put.getRequestLine().getHttpVersion(), "HTTP/1.1");
    assertEquals(put.getRequestLine().getUri(), "/create");
    assertEquals(put.getFirstHeaderValue("time"), "never");
    assertEquals(put.getFirstHeaderValue("content-encodING"), "gzip");
    assertEquals(Streams.stringFromStream(put.getBody()), "foo\nbar");
  }

  @Test
  public void testUnsupported() throws Exception {
    FakeServerSocket serverSocket = prepareServerSocket(fakeServerSocketFactory, testPort);
    startServer(server);

    String requestString =
        "WAT /crazytown HTTP/1.1\r\n"
        + "time: now\r\n"
        + "\r\n"
        + "foo";
    StringyOutputStream out = makeFakeRequest(serverSocket, requestString);

    Thread.sleep(500);

    Request request = server.getLastUnsupported();
    assertNotNull(request);
    assertEquals(request.getRequestLine().getMethod(), "WAT");
    assertEquals(request.getRequestLine().getHttpVersion(), "HTTP/1.1");
    assertEquals(request.getRequestLine().getUri(), "/crazytown");
    assertEquals(request.getFirstHeaderValue("time"), "now");
    assertEquals(Streams.stringFromStream(request.getBody()), "foo");

    // Just assert HTTP version and status code. Don't care so much about reason phrase.
    String expectedOutStart = "HTTP/1.1 405";
    assertEquals(expectedOutStart, out.getString().substring(0, expectedOutStart.length()));
  }

  private static FakeServerSocket prepareServerSocket(FakeServerSocketFactory factory, int port)
      throws IOException {
    FakeServerSocket result = new FakeServerSocket();
    factory.expectPort(port, result);
    return result;
  }

  private static void startServer(final CasualServer server) {
    new Thread(new Runnable() {
      @Override
      public void run() {
        try {
          server.start();
        } catch (IOException e) {
          e.printStackTrace();
          fail();
        }
      }

    }).start();
  }

  private static StringyOutputStream makeFakeRequest(
      FakeServerSocket fakeServerSocket, String requestString) {
    InputStream inputStream = new StringyInputStream(requestString);
    StringyOutputStream out = new StringyOutputStream();
    FakeSocket fakeSocket = new FakeSocket(inputStream, out);
    fakeServerSocket.setSocket(fakeSocket);
    return out;
  }

  private static final class TestServer extends CasualServer {
    private Request lastGet;
    private Request lastPost;
    private Request lastPut;
    private Request lastUnsupported;

    public TestServer(
        int port,
        ExecutorService requestExecutor,
        SocketConfigResolver configResolver,
        ServerSocketFactory socketFactory) {
      super(port, requestExecutor, configResolver, socketFactory);
    }

    public Request getLastGet() {
      return lastGet;
    }

    public Request getLastPost() {
      return lastPost;
    }

    public Request getLastPut() {
      return lastPut;
    }

    public Request getLastUnsupported() {
      return lastUnsupported;
    }

    @Override
    protected void onGet(Request request, Response response) throws IOException {
      assertNotNull(response);
      lastGet = request;
    }

    @Override
    protected void onPost(Request request, Response response) throws IOException {
      assertNotNull(response);
      lastPost = request;
    }

    @Override
    protected void onPut(Request request, Response response) throws IOException {
      assertNotNull(response);
      lastPut = request;
    }

    @Override
    protected void onUnsupportedMethod(Request request, Response response) throws IOException {
      // Invoke super. Test applies to default behaviour of unsupported requests.
      super.onUnsupportedMethod(request, response);
      assertNotNull(response);
      lastUnsupported = request;
    }
  }

  // TODO: All this class overriding is a bit loose. Should fix this up at some point.

  private static final class CurrentThreadExecutorService extends AbstractExecutorService {
    @Override
    public void execute(Runnable command) {
      command.run();
    }

    @Override
    public void shutdown() {}

    @Override
    public List<Runnable> shutdownNow() {
      return null;
    }

    @Override
    public boolean isShutdown() {
      return false;
    }

    @Override
    public boolean isTerminated() {
      return false;
    }

    @Override
    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
      return false;
    }
  }

  private static final class FakeServerSocketFactory extends ServerSocketFactory {
    private int expectedPort;
    private ServerSocket resultSocket;

    public void expectPort(int expectedPort, ServerSocket resultSocket) {
      this.expectedPort = expectedPort;
      this.resultSocket = resultSocket;
    }

    @Override
    public ServerSocket createServerSocket(int port) throws IOException {
      assertEquals("Created socket on unexpected port", expectedPort, port);
      return resultSocket;
    }

    @Override
    public ServerSocket createServerSocket(int port, int backlog) throws IOException {
      return null;
    }

    @Override
    public ServerSocket createServerSocket(int port, int backlog, InetAddress ifAddress)
        throws IOException {
      return null;
    }
  }

  private static final class FakeServerSocket extends ServerSocket {
    private CountDownLatch acceptLatch = new CountDownLatch(1);
    private Socket socket;

    public FakeServerSocket() throws IOException {
      super();
    }

    public void setSocket(Socket socket) {
      this.socket = socket;
      acceptLatch.countDown();
    }

    @Override
    public synchronized Socket accept() throws IOException {
      try {
        acceptLatch.await();
      } catch (InterruptedException e) {
        e.printStackTrace();
        fail();
      }
      acceptLatch = new CountDownLatch(1);
      Socket val = socket;
      socket = null;
      return val;
    }
  }

  private static final class FakeSocket extends Socket {
    private final InputStream in;
    private final OutputStream out;

    public FakeSocket(InputStream in, OutputStream out) {
      this.in = in;
      this.out = out;
    }

    @Override
    public InputStream getInputStream() throws IOException {
      return in;
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
      return out;
    }
  }
}
