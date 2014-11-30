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

import io.macgyver.neorx.rest.NeoRxClient;

import org.junit.Assume;
import org.junit.Before;
import org.slf4j.LoggerFactory;

public class RxNeo4jIntegrationTest {

	protected org.slf4j.Logger logger = LoggerFactory.getLogger(getClass());
	static Boolean available=null;
	private NeoRxClient client;
	
	
	public String getNeo4jRestUrl() {
		String val = System.getProperty("neo4j.url","http://localhost:7474");
		return val;
	}
	
	public synchronized NeoRxClient getClient() {
		
		if (client==null) {
			client = new NeoRxClient(getNeo4jRestUrl()); 
			MovieGraph g = new MovieGraph(client);
			g.replaceMovieGraph();
		}
		return client;
	}
	@Before
	public synchronized void checkIfNeo4jIsAvailable() {
		
		if (available==null) {
			
			available = getClient().checkConnection();
		}
		if (!available) {
			logger.warn("neo4j is not available -- integration tests will not be run");
		}
		Assume.assumeTrue(available);
	}
}
