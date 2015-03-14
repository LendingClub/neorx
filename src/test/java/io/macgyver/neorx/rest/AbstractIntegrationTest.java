/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.macgyver.neorx.rest;

import org.junit.Assume;
import org.junit.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractIntegrationTest extends NeoRxUnitTest {


	Logger logger = LoggerFactory.getLogger(AbstractIntegrationTest.class);
	static Boolean available = null;
	private static NeoRxClient client;

	public String getNeo4jRestUrl() {
		String val = System.getProperty("neo4j.url", "http://localhost:7474");
		return val;
	}

	public synchronized NeoRxClient getClient() {

		if (client == null) {
			client = new NeoRxClient(getNeo4jRestUrl());
			MovieGraph g = new MovieGraph(client);
			g.replaceMovieGraph();
		}
		return client;
	}

	@Before
	public synchronized void checkIfNeo4jIsAvailable() {
		try {

			if (available == null) {

				logger.info("checking to see if neo4j is available for tests");
				available = getClient().checkConnection();

			}

		} catch (RuntimeException e) {
			logger.info("neo4j not available: "+ e.toString());
		}

		if (available == null || !available) {
			logger.info("neo4j is not available -- integration tests will not be run");
			available = false;
		}
		Assume.assumeTrue(available);
	}
}
