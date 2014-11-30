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

import io.macgyver.neorx.NeoRxClient;

import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.databind.node.ObjectNode;

public class NeoRxClientTest {

	

	@Test
	public void testIntParam() {
		NeoRxClient c = new NeoRxClient();

		ObjectNode n = c.createParameters("abc", 1);

	}
	@Test
	public void testLongParam() {
		NeoRxClient c = new NeoRxClient();

		ObjectNode n = c.createParameters("abc", 1L);

	}
	
	@Test
	public void testPayloadFormat() {
		NeoRxClient c = new NeoRxClient();
		String query = "match (n:MyNode) where n.val={a} return n";
		ObjectNode n = c.formatPayload(query,c.createParameters("a",1));
		
		Assertions.assertThat(n.get("query").asText()).isEqualTo(query);
		Assertions.assertThat(n.get("params").get("a").asInt()).isEqualTo(1);
	}
	
	@Test
	public void testDoubleParam() {
		NeoRxClient c = new NeoRxClient();

		ObjectNode n = c.createParameters("abc", 1.5d);

	}
	@Test
	public void testBooleanParam() {
		NeoRxClient c = new NeoRxClient();

		ObjectNode n = c.createParameters("abc", true);

	}
	@Test
	public void testParams() {
		NeoRxClient c = new NeoRxClient();

		ObjectNode n = c.createParameters("abc", "def", "xxx", "yyy");

		org.junit.Assert.assertEquals(2, n.size());
		Assert.assertEquals(0, c.createParameters().size());

		Assert.assertEquals("def", c.createParameters("abc", "def").path("abc")
				.asText());

	}

	@Test(expected = IllegalArgumentException.class)
	public void testCreateParamsWithOddArgs() {
		NeoRxClient c = new NeoRxClient();

		ObjectNode n = c.createParameters("abc", "def", "xxx");
	
	}
}
