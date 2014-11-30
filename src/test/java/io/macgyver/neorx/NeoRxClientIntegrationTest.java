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
package io.macgyver.neorx;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import rx.functions.Action1;

public class NeoRxClientIntegrationTest extends Neo4jIntegrationTest {

	Logger logger = LoggerFactory.getLogger(NeoRxClientIntegrationTest.class);
	@Test
	public void testX() {
		NeoRxClient client = new NeoRxClient();
		
		
		Action1<Row> act = new Action1<Row>() {

			@Override
			public void call(Row t1) {
				logger.info("ROW: "+t1.getField("m.abc"));
				
			}
		};
		client.execCypher("match (m:Test) return m,m.abc").subscribe(act);
		
		
		
		client.execCypher("create (m:Test {abc:1})");
	
	

	}
}
