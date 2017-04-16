package org.lendingclub.neorx;

import org.assertj.core.api.Assertions;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.neo4j.driver.v1.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

public class NeoRxClientFacadeTest {
	static ObjectMapper mapper = new ObjectMapper();
	static Logger logger = LoggerFactory.getLogger(NeoRxClientFacadeTest.class);
	static NeoRxBoltClientImpl client;

	@After
	public void tearDown() {
		if (client == null) {
			return;
		}

		client.execCypher("MATCH (n) RETURN distinct labels(n) as labels").forEach(x -> {

			if (x.isArray()) {
				x.forEach(val -> {
					String label = val.asText();
					if (label.startsWith("JUnit")) {
						logger.debug("deleting nodes of type: {}", label);
						client.execCypher("match (n:" + label + ") detach delete n");
					}

				});
			}

		});
	}

	@AfterClass
	public static void tearDownMovieGraph() {
		if (client != null) {
			client.execCypher("match (p:Person) detach delete p");
			client.execCypher("match (p:Movie) detach delete p");
		}
	}

	@BeforeClass
	public static void setup() {

		NeoRxClient c = new NeoRxClient.Builder().withConfig(Config.build().withLeakedSessionsLogging().toConfig())
				.build();

		if (!c.checkConnection()) {
			logger.warn("neo4j unavailable for testing");
			return;
		}

		client = (NeoRxBoltClientImpl) c;

		MovieGraph mg = new MovieGraph(c);
		mg.replaceMovieGraph();

	}

	@Before
	public void checkEnv() {
		Assume.assumeTrue(client != null);
	}
	
	@Test
	public void testIt() {
		NeoRxClientFacade f = new NeoRxClientFacade(client);
		
		Assertions.assertThat(f.getDriver()).isNotNull();
		
		Assertions.assertThat(f.checkConnection()).isTrue();
		
		Assertions.assertThat(f.execCypher("create (a:JUnitFoo) return a").blockingFirst()).isNotNull();
	}
}
