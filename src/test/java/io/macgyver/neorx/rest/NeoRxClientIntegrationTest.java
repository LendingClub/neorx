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

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.node.ObjectNode;

public class NeoRxClientIntegrationTest extends RxNeo4jIntegrationTest {

	

	Logger logger = LoggerFactory.getLogger(NeoRxClientIntegrationTest.class);
	@Test
	public void testX() {
		
		
		NeoRxClient c = getClient();
		
		for (ObjectNode r: c.execCypher("match (m:Person) where m.born>{born} return m.born, m","born",1960).toBlocking().toIterable()) {
			System.out.println(r);
		}
		
		
		
		
		/*
		
		List<ObjectNode> list = Lists.newArrayList(c.execCypher("match m return m").flatMap(Transforms.extractObjectNode("m")).distinct().toBlocking().toIterable());
		
		
		System.out.println(list);
		
		*/
		
		
		
	
	

	}
}
