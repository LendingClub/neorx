package io.macgyver.neorx.rest;

import org.junit.BeforeClass;
import org.slf4j.bridge.SLF4JBridgeHandler;

public class NeoRxUnitTest {

	@BeforeClass
	public static void installSLF4jBridge() {
		if (!SLF4JBridgeHandler.isInstalled()) {
			org.slf4j.bridge.SLF4JBridgeHandler.removeHandlersForRootLogger();
			SLF4JBridgeHandler.install();
		}
	}
}
