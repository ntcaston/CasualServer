package spikedog.casual.server;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
  RequestLineTest.class,
  RequestTest.class,
  ResponseTest.class,
  StatusLineTest.class
})
public class ServerTestSuite {
}