package spikedog.casual.server.testutils;

import java.io.InputStream;
import java.util.Scanner;

public class Streams {
  private Streams() {}

  public static String stringFromStream(InputStream in) {
    Scanner scanner = null;
    try {
      scanner = new Scanner(in).useDelimiter("\\A");
      return scanner.hasNext() ? scanner.next() : null;
    } finally {
      if (scanner != null) {
        scanner.close();
      }
    }
  }
}
