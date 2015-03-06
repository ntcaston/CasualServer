package spikedog.casual.server;

import spikedog.casual.server.internal.InternalTestSuite;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ ServerTestSuite.class, InternalTestSuite.class })
public class AllTests {
}