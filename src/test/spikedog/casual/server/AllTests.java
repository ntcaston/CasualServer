package spikedog.casual.server;

import spikedog.casual.server.internal.InternalTestSuite;
import spikedog.casual.server.toolkit.ToolkitTestSuite;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
  ServerTestSuite.class,
  InternalTestSuite.class,
  ToolkitTestSuite.class
})
public class AllTests {
}