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

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import org.assertj.core.api.Assertions;
import org.assertj.core.util.Lists;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Splitter;
import com.google.common.io.BaseEncoding;
import com.squareup.okhttp.mockwebserver.MockResponse;
import com.squareup.okhttp.mockwebserver.MockWebServer;
import com.squareup.okhttp.mockwebserver.RecordedRequest;

public class NeoRxClientTest extends NeoRxUnitTest {

	public MockWebServer mockServer = new MockWebServer();



	@Test
	public void testListParam() {
		NeoRxClient c = new NeoRxClient();

		ObjectNode n = c.createParameters("abc", Lists.newArrayList("x"));

		assertThat(n.get("abc").get(0).asText()).isEqualTo("x");
		assertThat(n.get("abc").isArray()).isTrue();
	}

	@Test
	public void testIntParam() {
		NeoRxClient c = new NeoRxClient();

		Assert.assertEquals(JsonNodeType.NUMBER,
				c.createParameters("abc", Integer.MAX_VALUE).get("abc")
						.getNodeType());
	}

	@Test
	public void testLongParam() {
		NeoRxClient c = new NeoRxClient();

		Assert.assertEquals(JsonNodeType.NUMBER,
				c.createParameters("abc", Long.MAX_VALUE).get("abc")
						.getNodeType());

	}

	@Test
	public void testPayloadFormat() {
		NeoRxClient c = new NeoRxClient();
		String query = "match (n:MyNode) where n.val={a} return n";
		ObjectNode n = c.formatPayload(query, c.createParameters("a", 1));

		
		Assertions.assertThat(
				n.path("statements").get(0).path("statement").asText())
				.isEqualTo(query);
		Assertions.assertThat(
				n.path("statements").get(0).path("parameters").size())
				.isEqualTo(1);
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

	@Test
	public void testCreateParamsWithOddArgs() {

		try {
			NeoRxClient c = new MockNeoRxClient();

			ObjectNode n = c.createParameters("abc", "def", "xxx");
		} catch (Exception e) {

			Assertions.assertThat(e).isExactlyInstanceOf(
					IllegalArgumentException.class);
		}

	}

	@Test
	public void testTrailingSlashInUrl() throws InterruptedException,
			IOException {
		String response = "{\"results\":[{\"columns\":[\"p\"],\"data\":[{\"row\":[{\"name\":\"Carrie-Anne Moss\",\"born\":1967}]}]}],\"errors\":[]}";

		mockServer.enqueue(new MockResponse().setBody(response));

		new NeoRxClient(mockServer.getUrl("/prefix/").toString())
				.execCypher("match (p:Person) return p").toList().toBlocking()
				.first();

		assertThat(mockServer.takeRequest().getPath()).isEqualTo(
				"/prefix/db/data/transaction/commit");

	}

	@Test
	public void teestBasicRequestWithMockServer() throws InterruptedException,
			IOException {
		String response = "{\"results\":[{\"columns\":[\"p\"],\"data\":[{\"row\":[{\"name\":\"Carrie-Anne Moss\",\"born\":1967}]}]}],\"errors\":[]}";

		mockServer.enqueue(new MockResponse().setBody(response));
		NeoRxClient c = new NeoRxClient(mockServer.getUrl("/").toString());

		List<JsonNode> x = c.execCypher("match (p:Person) return p").toList()
				.toBlocking().first();
		assertThat(x.get(0).path("name").asText())
				.isEqualTo("Carrie-Anne Moss");

		RecordedRequest rr = mockServer.takeRequest();
		assertThat(rr.getHeader("Content-type")).containsIgnoringCase(
				"application/json");
		assertThat(rr.getPath()).isEqualTo("/db/data/transaction/commit");
		JsonNode rbody = new ObjectMapper().readTree(rr.getUtf8Body());
		assertThat(rbody.path("statements").size()).isEqualTo(1);
		assertThat(rbody.path("statements").get(0).path("statement").asText())
				.isEqualTo("match (p:Person) return p");
	}

	@Test
	public void testBasicRequest() {

		String response = "{\"results\":[{\"columns\":[\"p\"],\"data\":[{\"row\":[{\"name\":\"Carrie-Anne Moss\",\"born\":1967}]}]}],\"errors\":[]}";

		List<JsonNode> x = new MockNeoRxClient().enqueue(response).
				execCypher(null).toList().toBlocking().first();

		Assertions.assertThat(x).hasSize(1);
		Assertions.assertThat(x.get(0).get("name").asText()).isEqualTo(
				"Carrie-Anne Moss");

	}

	@Test
	public void testCreateParamsWithInvalidArg() {
		try {
			NeoRxClient c = new MockNeoRxClient();

			Object x = new Object() {
			};

			ObjectNode n = c.createParameters("def", x);
		} catch (Exception e) {

			Assertions.assertThat(e)
					.isExactlyInstanceOf(IllegalArgumentException.class)
					.hasMessageContaining("type not supported");
		}
	}

	@Test
	public void testCheckConnection() {
		NeoRxClient c = new NeoRxClientBuilder().withUrl("http://invalid." + UUID.randomUUID().toString()
						+ ".com").build();
			
		
		Assertions.assertThat(c.checkConnection()).isFalse();

		c = new MockNeoRxClient() {
			public String getUrl() {
				// throw new RuntimeException("simulated failure");
				return "foo://bar";
			}
		};
		Assertions.assertThat(c.checkConnection()).isFalse();
	}

	@Test
	public void testCertValidationDefaults() {
		
		Assertions.assertThat(
				new NeoRxClientBuilder().build().isCeritificateValidationEnabled()).isTrue();
		
		Assertions.assertThat(
				new NeoRxClient().isCeritificateValidationEnabled()).isTrue();
		
		
		Assertions.assertThat(
				new NeoRxClient("http://localhost:7474")
						.isCeritificateValidationEnabled()).isTrue();
		Assertions.assertThat(
				new NeoRxClient("http://localhost:7474", true)
						.isCeritificateValidationEnabled()).isTrue();
		Assertions.assertThat(
				new NeoRxClient("http://localhost:7474", false)
						.isCeritificateValidationEnabled()).isFalse();
		Assertions.assertThat(
				new NeoRxClient("http://localhost:7474", true)
						.isCeritificateValidationEnabled()).isTrue();
		Assertions.assertThat(
				new NeoRxClient("http://localhost:7474", "user", "pass", false)
						.isCeritificateValidationEnabled()).isFalse();
		Assertions.assertThat(
				new NeoRxClient("http://localhost:7474", "user", "pass", true)
						.isCeritificateValidationEnabled()).isTrue();
	}
	
	@Test
	public void testBasicAuth() throws InterruptedException {
		String response = "{\"results\":[{\"columns\":[\"p\"],\"data\":[{\"row\":[{\"name\":\"Carrie-Anne Moss\",\"born\":1967}]}]}],\"errors\":[]}";

		mockServer.enqueue(new MockResponse().setBody(response));
		NeoRxClient c = new NeoRxClient(mockServer.getUrl("/").toString(), "scott", "tiger");
		
		c.execCypher("match (p:Foo) return p");
		
		RecordedRequest rr = mockServer.takeRequest();
		
		
		List<String> l = Splitter.on(" ").splitToList(rr.getHeader("Authorization"));
		
		Assertions.assertThat(l.get(0)).isEqualTo("Basic");
		Assertions.assertThat(new String(BaseEncoding.base64().decode(l.get(1)))).isEqualTo("scott:tiger");
	}
}
