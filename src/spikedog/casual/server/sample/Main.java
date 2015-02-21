package spikedog.casual.server.sample;


public class Main {
  public static void main(String[] args) {
    // TODO branch on args
    if (false) {
      new HelloWorld(8080).run();
    } else {
      new FileHost("html", 8080).run();
    }
  }
}
