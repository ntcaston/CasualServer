package spikedog.casual.server.internal;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
  SocketConfigResolverTest.class,
  StreamRequestBuilderTest.class
})
public class InternalTestSuite {
}