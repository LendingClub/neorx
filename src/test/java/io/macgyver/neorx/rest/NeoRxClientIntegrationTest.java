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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Test;

import rx.functions.Action1;
import rx.observables.BlockingObservable;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.NumericNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class NeoRxClientIntegrationTest extends AbstractIntegrationTest {

	@Test
	public void testEmptyResult() {
		// since we only return a single entity, we "unwrap" the value to make
		// it easier to process

		List<JsonNode> r = getClient()
				.execCypher(
						"match (m:Person) where m.name='not found' return m")
				.toList().toBlocking().first();

		Assert.assertNotNull(r);
		Assertions.assertThat(r).isEmpty();

		r = getClient()
				.execCypher(
						"match (m:Person) where m.name={name} return m.name,m.born",
						"name", "invalid").toList().toBlocking().first();

		Assert.assertNotNull(r);
		Assertions.assertThat(r).isEmpty();

		BlockingObservable<JsonNode> bo = getClient().execCypher(
				"match (m:Person) where m.name={name} return m.name,m.born",
				"name", "invalid").toBlocking();
		Assert.assertFalse(bo.getIterator().hasNext());

		Assert.assertTrue(getClient()
				.execCypher(
						"match (m:Person) where m.name={name} return m.name,m.born",
						"name", "invalid").count().toBlocking().first() == 0);

		Assert.assertNull(getClient()
				.execCypher(
						"match (m:Person) where m.name='not found' return m")
				.toBlocking().firstOrDefault(null));

	}

	@Test
	public void testUnwrapped() {
		// since we only return a single entity, we "unwrap" the value to make
		// it easier to process

		getClient().execCypher("match (m:Person) where m.born>{born} return m",
				"born", 1960).subscribe(new Action1<JsonNode>() {

			@Override
			public void call(JsonNode t1) {
				Assertions.assertThat(t1.fieldNames()).contains("born", "name");
				Assertions.assertThat(t1.fieldNames()).doesNotContain("m");
			}
		});
	}

	@Test
	public void testUnwrappedBlockingTransform() {

		// find all the people in the graph born after 1960 and return a list of
		// their names

		List<String> n = getClient()
				.execCypher(
						"match (m:Person) where m.born>{born} return m.name",
						"born", 1960)
				.flatMap(NeoRxFunctions.jsonNodeToString()).toList()
				.toBlocking().first();

		assertThat(n).contains("Meg Ryan");

	}

	@Test
	public void testUnwrappedNull() {
		// since we only return a single entity, we "unwrap" the value to make
		// it easier to process

		getClient().execCypher(
				"match (m:Person) where m.born>{born} return null as m",
				"born", 1960).subscribe(new Action1<JsonNode>() {

			@Override
			public void call(JsonNode t1) {
				assertNotNull(t1);

				assertTrue(t1.isNull());
			}
		});
	}

	@Test
	public void testCreateParameters() {
		ObjectNode n = getClient().createParameters("abc", null);
		Assertions.assertThat(n.get("abc").isNull()).isTrue();
	}

	@Test
	public void testMultiProps() {
		String id = UUID.randomUUID().toString();

		ObjectNode props = new ObjectMapper().createObjectNode()
				.put("city", "San Francisco").put("state", "CA")
				.put("mayor", "Ed Lee");
		getClient().execCypher("match (mp:TestMultiProp) delete mp");
		getClient()
				.execCypher(
						"MERGE (mp:TestMultiProp {city:{city}}) set mp={props} return mp",
						"city", props.path("city").asText(), "props", props);

		

		Map<String,String> sm = new HashMap<>();
		sm.put("city", "Oakland");
		sm.put("state", "CA");
		sm.put("mayor", "Libby Schaaf");
		
		getClient()
				.execCypher(
						"MERGE (mp:TestMultiProp {city:{city}}) set mp+={props} return mp",
						"city", sm.get("city").toString(), "props", sm);

		Assertions
				.assertThat(
						getClient()
								.execCypher(
										"match (mp:TestMultiProp {city:'San Francisco'}) return mp")
								.toBlocking().first().get("mayor").asText())
				.isEqualTo("Ed Lee");
		
		
		Assertions
		.assertThat(
				getClient()
						.execCypher(
								"match (mp:TestMultiProp {city:'Oakland'}) return mp")
						.toBlocking().first().get("mayor").asText())
		.isEqualTo("Libby Schaaf");

	}

	@Test
	public void testWrapped() {

		getClient().execCypher(
				"match (m:Person) where m.born>{born} return m.born, m",
				"born", 1960).subscribe(new Action1<JsonNode>() {

			@Override
			public void call(JsonNode r) {

				assertThat(r.get("m.born")).isNotNull().isInstanceOf(
						NumericNode.class);
				assertTrue(r.get("m.born").isNumber());
				assertThat(r.get("m")).isNotNull().isInstanceOf(
						ObjectNode.class);
				Assertions.assertThat(r.get("notfound")).isNull();
			}
		});

	}

	@Test
	public void testInvalidUrl() {
		NeoRxClient c = new NeoRxClientBuilder().url("http://blahblah.example.com").build();
		try {
			c.execCypher("match m");
			Assertions.fail("should throw");
		} catch (Exception e) {

			Assertions.assertThat(e)
					.hasRootCauseInstanceOf(UnknownHostException.class)
					.isInstanceOf(NeoRxException.class);
		}
	}

	@Test
	public void testCertificateVerifiction() {
		NeoRxClient c = null;
		

		c = new NeoRxClientBuilder().url("https://localhost:7473").withCertificateValidation(true).build();
		
		
		c= new NeoRxClientBuilder().url("https://localhost:7473").withCertificateValidation(false).build();
	

		
	}

	@Test
	public void testInvalidCypher() {

		try {
			getClient().execCypher("match m");
			Assertions.fail("should throw");
		} catch (Exception e) {
			Assertions.assertThat(e).isInstanceOf(NeoRxException.class);

		}
	}

	@Test
	public void testCreateWithoutReturn() {
		String id = UUID.randomUUID().toString();

		Assert.assertNull(getClient()
				.execCypher("create (x:UnitTest {name:{name}})", "name", id)
				.toBlocking().firstOrDefault(null));
		Assertions.assertThat(
				getClient()
						.execCypher("create (x:UnitTest {name:{name}})",
								"name", id).count().toBlocking().first())
				.isEqualTo(0);
		Assertions.assertThat(
				getClient()
						.execCypher("create (x:UnitTest {name:{name}})",
								"name", id).toList().toBlocking().first())
				.isEmpty();
	}

	@Test
	public void testCreateWithReturn() {
		String id = UUID.randomUUID().toString();
		JsonNode n = getClient()
				.execCypher("create (x:UnitTest {name:{name}}) return x",
						"name", id).toBlocking().first();
		Assert.assertEquals(id, n.path("name").asText());
	}

	@Test
	public void testX() {
		List<JsonNode> n = getClient().execCypherAsList(
				"match (m:Person) where m.name={name} return m", "name",
				"Carrie-Anne Moss");

		Assertions.assertThat(n.get(0).path("born").asInt()).isEqualTo(1967);

		n = getClient().execCypherAsList(
				"match (m:Person) where m.name={name} return m.born", "name",
				"Carrie-Anne Moss");
		Assertions.assertThat(n.get(0).asInt()).isEqualTo(1967);

		n = getClient().execCypherAsList(
				"match (m:Person) where m.name={name} return m.name,m.born",
				"name", "Carrie-Anne Moss");
		Assertions.assertThat(n.get(0).path("m.born").asInt()).isEqualTo(1967);
		Assertions.assertThat(n.get(0).path("m.name").asText()).isEqualTo(
				"Carrie-Anne Moss");
	}
	
	@Test
	public void testPrimitiveValues() throws JsonProcessingException, IOException {
		ObjectMapper m = new ObjectMapper();
		
		JsonNode n = m.createObjectNode().set("xyz", NullNode.getInstance());
		getClient().execCypher("create (a:JUnit) set a={p}, a.javaNull={javaNull},a.nullVal={nullVal}, a.intVal={intVal},a.stringVal={stringVal} return a","p",n,"stringVal",m.readTree("\"foo\""),"intVal",m.readTree("3"),"nullVal",NullNode.getInstance(),"javaNull",null);
	}
	
	@Test
	public void testArray() throws JsonProcessingException, IOException {
		ObjectMapper m = new ObjectMapper();
		
		ArrayNode an = m.createArrayNode();		
		an.add("foo");
		an.add("bar");

		ArrayNode an2 = m.createArrayNode();		
		an2.add(1);
		an2.add(2);
		
		JsonNode n = getClient().execCypher("create (a:JUnit) set a.array1={array1},a.array2={array2} return a","array1",an,"array2",an2).toBlocking().first();
		
		Assertions.assertThat(n.path("array1").get(0).asText()).isEqualTo("foo");
		Assertions.assertThat(n.path("array1").get(1).asText()).isEqualTo("bar");
		Assertions.assertThat(n.path("array2").get(0).asInt()).isEqualTo(1);
		Assertions.assertThat(n.path("array2").get(1).asInt()).isEqualTo(2);
	}
}
