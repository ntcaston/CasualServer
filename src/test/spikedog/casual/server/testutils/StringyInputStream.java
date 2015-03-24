package spikedog.casual.server.testutils;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicInteger;

public final class StringyInputStream extends InputStream {
  private final byte[] bytes;
  private final AtomicInteger position = new AtomicInteger();

  public StringyInputStream(String value) {
    bytes = value.getBytes();
  }

  @Override
  public int read() throws IOException {
    byte[] bytes = new byte[1];
    int val = read(bytes, 0, 1);
    return val < 0 ? val : bytes[0];
  }

  @Override
  public int read(byte[] b) throws IOException {
    return read(b, 0, b.length);
  }

  @Override
  public int read(byte[] b, int off, int len) throws IOException {
    int start = position.get();
    int end = Math.min(start + len, bytes.length);
    int readCount = end - start;
    System.arraycopy(bytes, start, b, off, readCount);
    position.addAndGet(readCount);
    return readCount <= 0 ? -1 : readCount;
  }
}