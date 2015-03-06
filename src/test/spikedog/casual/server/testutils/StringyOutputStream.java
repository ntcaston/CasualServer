package spikedog.casual.server.testutils;

import java.io.IOException;
import java.io.OutputStream;

public class StringyOutputStream extends OutputStream {
  private final StringBuilder stringBuilder = new StringBuilder();

  public String getString() {
    return stringBuilder.toString();
  }

  @Override
  public void write(int b) throws IOException {
    write(new byte[]{(byte) b});
  }

  @Override
  public void write(byte[] b) throws IOException {
    write(b, 0, b.length);
  }

  @Override
  public void write(byte[] b, int off, int len) throws IOException {
    stringBuilder.append(new String(b, off, len));
  };
}
