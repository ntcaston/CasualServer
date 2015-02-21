package spikedog.casual.server;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicLong;

public class TerminatingInputStream extends InputStream {
  private final InputStream delegate;
  private final long terminatingBytes;
  private final AtomicLong readBytes = new AtomicLong();
  
  /**
   * @param terminatingBytes The number of bytes which may be read before the stream returns -1 for
   *     read calls.
   */
  public TerminatingInputStream(InputStream delegate, long terminatingBytes) {
    this.delegate = delegate;
    this.terminatingBytes = terminatingBytes;
  }

  @Override
  public int read() throws IOException {
    if (readBytes.get() >= terminatingBytes) {
      return -1;
    }
    readBytes.incrementAndGet();
    return delegate.read();
  }
  
  @Override
  public int read(byte[] b) throws IOException {
    if (readBytes.get() >= terminatingBytes) {
      return -1;
    }
    
    int remainingBytes = (int) (terminatingBytes - readBytes.get());
    if (b.length > remainingBytes) {
      return read(b, 0, remainingBytes);
    }
    
    int readBytes = delegate.read(b);
    this.readBytes.addAndGet(readBytes);
    return readBytes;
  }
  
  @Override
  public int read(byte[] b, int off, int len) throws IOException {
    if (readBytes.get() >= terminatingBytes) {
      return -1;
    }

    int remainingBytes = (int) (terminatingBytes - readBytes.get());
    len = Math.min(remainingBytes, len);
    int readBytes = delegate.read(b, off, len);
    this.readBytes.addAndGet(readBytes);
    return readBytes;
  }

  @Override
  public int available() throws IOException {
    return delegate.available();
  }
  
  @Override
  public synchronized void mark(int readlimit) {
    delegate.mark(readlimit);
  }
  
  @Override
  public boolean markSupported() {
    return delegate.markSupported();
  }
  
  @Override
  public synchronized void reset() throws IOException {
    delegate.reset();
  }
  
  @Override
  public long skip(long n) throws IOException {
    return delegate.skip(n);
  }
}
