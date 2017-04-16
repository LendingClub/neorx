package org.lendingclub.neorx;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.lendingclub.neorx.NeoRxBoltClientImpl.convertParameterValueType;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.assertj.core.api.Assertions;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.lendingclub.neorx.NeoRxClient.Builder;
import org.lendingclub.neorx.mock.MockNeoRxClient;
import org.neo4j.driver.v1.AuthToken;
import org.neo4j.driver.v1.AuthTokens;
import org.neo4j.driver.v1.Config;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.GraphDatabase;
import org.neo4j.driver.v1.exceptions.ClientException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.MissingNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.NumericNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Lists;

import io.reactivex.Observable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;

public class NeoRxClientTest {

	static ObjectMapper mapper = new ObjectMapper();
	static Logger logger = LoggerFactory.getLogger(NeoRxClientTest.class);
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

		client.execCypher("create (a:JUnitFoo) set a.name='a' return a;");
		client.execCypher("create (a:JUnitFoo) set a.name='b' return a;");

		client.execCypher("create (a:JUnitBar) set a.name='b' return a;");
		client.execCypher("create (a:JUnitBar) set a.name='b' return a;");

		client.execCypher("match (f:JunitFoo {name:'a'}), (b:JUnitBar {name:'a'}) merge (a)-[r:USES]->(b)");

	}

	@Test
	public void testInvalidReturn() {
		try {
			client.execCypher("match (a:JUnitFooBar) return x");
		} catch (NeoRxException e) {
			ClientException ce = (ClientException) e.getCause();
			Assertions.assertThat(e.getMessage()).isEqualTo(ce.getMessage());
		}
	}

	@Test
	public void testWrapped2() {

		// return values are "wrapped"
		client.execCypher("match (m:Movie {title:'The Matrix'})--(x) return m,x").forEach(it -> {
			Assertions.assertThat(it.path("m").path("tagline").asText()).isEqualTo("Welcome to the Real World");
			Assertions.assertThat(it.path("m").get("released").isIntegralNumber()).isTrue();
		});
	}

	@Test
	public void testUnwrapper() {
		// single return value is unwrapped
		client.execCypher("match (m:Movie {title:'The Matrix'})--(x) return m").forEach(it -> {
			Assertions.assertThat(it.path("tagline").asText()).isEqualTo("Welcome to the Real World");
			Assertions.assertThat(it.get("released").isIntegralNumber()).isTrue();
		});

		// single return value is unwrapped
		client.execCypher("match (m:Movie {title:'The Matrix'})return m.title").forEach(it -> {
			Assertions.assertThat(it.asText()).isEqualTo("The Matrix");
		});
		// single return value is unwrapped
		client.execCypher("match (m:Movie {title:'The Matrix'})return m.title as foo").forEach(it -> {
			Assertions.assertThat(it.asText()).isEqualTo("The Matrix");
		});
	}

	@Test
	public void testConversion() {
		Assertions.assertThat(convertParameterValueType(null)).isNull();
		Assertions.assertThat(convertParameterValueType(new String[0])).isInstanceOf(String[].class);
		Assertions.assertThat(convertParameterValueType("foo")).isEqualTo("foo");
		Assertions.assertThat(convertParameterValueType(this)).isSameAs(this);
		Assertions.assertThat(convertParameterValueType(mapper.createObjectNode().put("foo", "bar")))
				.isInstanceOf(Map.class);

		Map<String, Object> m = (Map<String, Object>) convertParameterValueType(
				mapper.createObjectNode().set("foo", mapper.createArrayNode().add("fizz").add("buzz")));

		List<String> list = (List<String>) m.get("foo");
		Assertions.assertThat(list).contains("fizz", "buzz");
	}

	@Test
	public void testStringArray() {
		client.execCypher("create (a:JUnitStringArray) set a.arr={arr} return a", "arr", new String[] { "a", "b" })
				.forEach(it -> {
					logger.info("{}", it);
				});
		client.execCypher("create (a:JUnitStringArray) set a.arr={arr} return a", "arr", Lists.newArrayList("a", "b"))
				.forEach(it -> {
					logger.info("{}", it);
				});

		ObjectNode props = mapper.createObjectNode();
		props.set("arr", mapper.createArrayNode().add("a").add("b"));

		client.execCypher("create (a:JUnitStringArray) set a.arr={arr} return a", props).forEach(it -> {
			logger.info("{}", it);
		});
	}

	@Test
	public void testObjectNodeProps() {
		client.execCypher("match (a:JUnitFoo) detach delete a;");
		ObjectNode n = mapper.createObjectNode();

		n.put("foo", "bar");
		n.put("fizz", 123.4);
		n.set("buzz", null);
		client.execCypher("create (a:JUnitFoo) set a.nullCorrect=null, a+={props} return a", "props", n, "null", null)
				.forEach(it -> {
					logger.info("{}", it);
				});

	}

	protected NeoRxClient getClient() {
		return this.client;
	}

	@Test
	public void testEmptyResult() {
		// since we only return a single entity, we "unwrap" the value to make
		// it easier to process

		List<JsonNode> r = getClient().execCypher("match (m:Person) where m.name='not found' return m").toList()
				.blockingGet();

		Assert.assertNotNull(r);
		Assertions.assertThat(r).isEmpty();

		r = getClient().execCypher("match (m:Person) where m.name={name} return m.name,m.born", "name", "invalid")
				.toList().blockingGet();

		Assert.assertNotNull(r);
		Assertions.assertThat(r).isEmpty();

		Assert.assertFalse(
				getClient().execCypher("match (m:Person) where m.name={name} return m.name,m.born", "name", "invalid")
						.blockingIterable().iterator().hasNext());

		Assert.assertTrue(
				getClient().execCypher("match (m:Person) where m.name={name} return m.name,m.born", "name", "invalid")
						.count().blockingGet() == 0);

		Assertions.assertThat(
				getClient().execCypher("match (m:Person) where m.name='not found' return m").isEmpty().blockingGet())
				.isTrue();

	}

	@Test
	public void testUnwrapped() {
		// since we only return a single entity, we "unwrap" the value to make
		// it easier to process

		getClient().execCypher("match (m:Person) where m.born>{born} return m", "born", 1960)
				.subscribe(new Consumer<JsonNode>() {

					@Override
					public void accept(JsonNode t1) {
						Assertions.assertThat(t1.fieldNames()).contains("born", "name");
						Assertions.assertThat(t1.fieldNames()).doesNotContain("m");
					}
				});

	}

	public static Function<JsonNode, Observable<String>> jsonNodeToString() {
		return new Function<JsonNode, Observable<String>>() {

			@Override
			public Observable<String> apply(JsonNode t1) {
				if (t1 == null) {
					return Observable.just(null);
				} else if (t1 instanceof NullNode) {
					return Observable.just(null);
				} else {
					return Observable.just(t1.asText());
				}
			}

		};

	}

	@Test
	public void testUnwrappedBlockingTransform() {

		// find all the people in the graph born after 1960 and return a list of
		// their names

		List<String> n = getClient().execCypher("match (m:Person) where m.born>{born} return m.name", "born", 1960)
				.flatMap(jsonNodeToString()).toList().blockingGet();

		assertThat(n).contains("Meg Ryan");

	}

	@Test
	public void testUnwrappedNull() {
		// since we only return a single entity, we "unwrap" the value to make
		// it easier to process

		getClient().execCypher("match (m:Person) where m.born>{born} return null as x", "born", 1960)
				.subscribe(new Consumer<JsonNode>() {

					@Override
					public void accept(JsonNode t1) {

						assertNotNull(t1);

						assertTrue(t1.isNull());
					}
				});
	}

	@Test
	public void JUnitMultiProps() {
		String id = UUID.randomUUID().toString();

		ObjectNode props = new ObjectMapper().createObjectNode().put("city", "San Francisco").put("state", "CA")
				.put("mayor", "Ed Lee");
		getClient().execCypher("match (mp:JUnitMultiProp) delete mp");
		getClient().execCypher("MERGE (mp:JUnitMultiProp {city:{city}}) set mp={props} return mp", "city",
				props.path("city").asText(), "props", props);

		Map<String, String> sm = new HashMap<>();
		sm.put("city", "Oakland");
		sm.put("state", "CA");
		sm.put("mayor", "Libby Schaaf");

		getClient().execCypher("MERGE (mp:JUnitMultiProp {city:{city}}) set mp+={props} return mp", "city",
				sm.get("city").toString(), "props", sm);

		Assertions.assertThat(getClient().execCypher("match (mp:JUnitMultiProp {city:'San Francisco'}) return mp")
				.blockingFirst().get("mayor").asText()).isEqualTo("Ed Lee");

		Assertions.assertThat(getClient().execCypher("match (mp:JUnitMultiProp {city:'Oakland'}) return mp")
				.blockingFirst().get("mayor").asText()).isEqualTo("Libby Schaaf");

	}

	@Test
	public void testWrapped() {

		getClient().execCypher("match (m:Person) where m.born>{born} return m.born, m", "born", 1960)
				.subscribe(new Consumer<JsonNode>() {

					@Override
					public void accept(JsonNode r) {

						assertThat(r.get("m.born")).isNotNull().isInstanceOf(NumericNode.class);
						assertTrue(r.get("m.born").isNumber());
						assertThat(r.get("m")).isNotNull().isInstanceOf(ObjectNode.class);
						Assertions.assertThat(r.get("notfound")).isNull();
					}
				});

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

		Assert.assertNull(getClient().execCypher("create (x:JUnit {name:{name}})", "name", id).blockingFirst(null));
		Assertions
				.assertThat(getClient().execCypher("create (x:JUnit {name:{name}})", "name", id).toList().blockingGet())
				.isEmpty();
		Assertions
				.assertThat(getClient().execCypher("create (x:JUnit {name:{name}})", "name", id).toList().blockingGet())
				.isEmpty();
	}

	@Test
	public void testCreateWithReturn() {
		String id = UUID.randomUUID().toString();
		JsonNode n = getClient().execCypher("create (x:JUnit {name:{name}}) return x", "name", id).blockingFirst();
		Assert.assertEquals(id, n.path("name").asText());
	}

	@Test
	public void testX() {
		List<JsonNode> n = getClient().execCypherAsList("match (m:Person) where m.name={name} return m", "name",
				"Carrie-Anne Moss");

		Assertions.assertThat(n.get(0).path("born").asInt()).isEqualTo(1967);

		n = getClient().execCypherAsList("match (m:Person) where m.name={name} return m.born", "name",
				"Carrie-Anne Moss");

		Assertions.assertThat(n.get(0).asInt()).isEqualTo(1967);

		n = getClient().execCypherAsList("match (m:Person) where m.name={name} return m.name,m.born", "name",
				"Carrie-Anne Moss");
		Assertions.assertThat(n.get(0).path("m.born").asInt()).isEqualTo(1967);
		Assertions.assertThat(n.get(0).path("m.name").asText()).isEqualTo("Carrie-Anne Moss");
	}

	@Test
	public void testPrimitiveValues() throws JsonProcessingException, IOException {
		ObjectMapper m = new ObjectMapper();

		JsonNode n = m.createObjectNode().set("xyz", NullNode.getInstance());
		getClient().execCypher(
				"create (a:JUnit) set a={p}, a.javaNull={javaNull},a.nullVal={nullVal}, a.intVal={intVal},a.stringVal={stringVal} return a",
				"p", n, "stringVal", m.readTree("\"foo\""), "intVal", m.readTree("3"), "nullVal",
				NullNode.getInstance(), "javaNull", null);
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

		JsonNode n = getClient().execCypher("create (a:JUnit) set a.array1={array1},a.array2={array2} return a",
				"array1", an, "array2", an2).blockingFirst();

		Assertions.assertThat(n.path("array1").get(0).asText()).isEqualTo("foo");
		Assertions.assertThat(n.path("array1").get(1).asText()).isEqualTo("bar");
		Assertions.assertThat(n.path("array2").get(0).asInt()).isEqualTo(1);
		Assertions.assertThat(n.path("array2").get(1).asInt()).isEqualTo(2);
	}

	@Test
	public void testInvalidReturnVar() {
		try {
			getClient().execCypher("match (a:JUnitFooBar) return b");
		} catch (NeoRxException e) {
			Assertions.assertThat(e).isInstanceOf(NeoRxException.class).hasMessageContaining("not defined");

		}
	}

	@Test
	public void testUnwrapMadness() {
		getClient().execCypher("create (a:JUnitWrap) set a.name='a', a.count=1");
		getClient().execCypher("create (a:JUnitWrap) ");
		getClient().execCypher("create (a:JUnitWrap) set a.name='c'");

		List<JsonNode> n = getClient().execCypherAsList("match (a:JUnitWrap) return a order by a.name");
		Assertions.assertThat(n.get(0).path("count").asInt()).isEqualTo(1);
		Assertions.assertThat(n.get(1).path("name").asText()).isEqualTo("c");
		Assertions.assertThat(n.get(2).size()).isEqualTo(0);
	}

	@Test
	public void testParams() {
		try {
			getClient().execCypher("match (a:Foo) return a", "unbalanced");
			Assertions.failBecauseExceptionWasNotThrown(NeoRxException.class);
		} catch (Exception e) {
			Assertions.assertThat(e).isExactlyInstanceOf(NeoRxException.class)
					.hasCauseInstanceOf(ClientException.class);
		}
	}

	@Test
	public void testDriver() {
		Assertions.assertThat(getClient().getDriver()).isNotNull();
	}

	@Test
	public void testJsonNodeParam() throws IOException, JsonProcessingException {
		Assertions.assertThat(getClient().execCypher("match (m:Movie {title:{title}}) return m", "title", "Unforgiven")
				.toList().blockingGet()).hasSize(1);
		Assertions.assertThat(getClient()
				.execCypher("match (m:Movie {title:{title}}) return m", "title", mapper.readTree("\"Unforgiven\""))
				.toList().blockingGet()).hasSize(1);

		Assertions.assertThat(getClient().execCypher("match (m:Movie {released:{year}}) return m", "year", 1992)
				.toList().blockingGet()).hasSize(4);
		Assertions.assertThat(
				getClient().execCypher("match (m:Movie {released:{year}}) return m", "year", mapper.readTree("1992"))
						.toList().blockingGet())
				.hasSize(4);

		Assertions.assertThat(getClient().execCypher("match (m:Movie) where m.released<{year} return m", "year", 1976.2)
				.toList().blockingGet().size()).isEqualTo(2);
		Assertions.assertThat(getClient()
				.execCypher("match (m:Movie) where m.released<{year} return m", "year", mapper.readTree("1976.2"))
				.toList().blockingGet().size()).isEqualTo(2);

		String id = UUID.randomUUID().toString();
		getClient().execCypher("create (f:JUnitFoo {id:{id}, active:{active}}) return f", "id", id, "active", true);

		Assertions.assertThat(getClient()
				.execCypher("match (f:JUnitFoo {id:{id},active:{active}}) return f", "id", id, "active", false).toList()
				.blockingGet()).hasSize(0);
		Assertions.assertThat(getClient()
				.execCypher("match (f:JUnitFoo {id:{id},active:{active}}) return f", "id", id, "active", true).toList()
				.blockingGet()).hasSize(1);
		Assertions.assertThat(getClient().execCypher("match (f:JUnitFoo {id:{id},active:{active}}) return f", "id", id,
				"active", mapper.readTree("false")).toList().blockingGet()).hasSize(0);
		Assertions.assertThat(getClient().execCypher("match (f:JUnitFoo {id:{id},active:{active}}) return f", "id", id,
				"active", mapper.readTree("true")).toList().blockingGet()).hasSize(1);

	}

	@Test
	public void testNullValue() throws IOException, JsonProcessingException {

		String id = UUID.randomUUID().toString();
		getClient().execCypher("create (f:JUnitFoo {id:{id}, val:{val}, fizz:null}) set f.foo=null return f", "id", id,
				"val", mapper.createObjectNode().path("notfound"));

		JsonNode n = getClient().execCypher("match (f:JUnitFoo {id:{id}}) return f", "id", id).blockingFirst();
		System.out.println(n);
		Assertions.assertThat(Lists.newArrayList(n.fieldNames())).hasSize(1);

	}

	@Test
	public void testConvertValueType() throws IOException, JsonProcessingException {
		Assertions.assertThat(NeoRxBoltClientImpl.convertParameterValueType(null)).isNull();
		Assertions.assertThat(NeoRxBoltClientImpl.convertParameterValueType("123")).isEqualTo("123");
		Assertions.assertThat(NeoRxBoltClientImpl.convertParameterValueType(123)).isEqualTo(123);
		Assertions.assertThat(NeoRxBoltClientImpl.convertParameterValueType(this)).isSameAs(this);
		Assertions.assertThat(NeoRxBoltClientImpl.convertParameterValueType(mapper.createArrayNode().add("a")))
				.isInstanceOf(List.class);
		Assertions.assertThat(NeoRxBoltClientImpl.convertParameterValueType(mapper.createArrayNode().add(1).add(2)))
				.isInstanceOf(List.class);
		Assertions.assertThat(NeoRxBoltClientImpl.convertParameterValueType(mapper.createObjectNode()))
				.isInstanceOf(Map.class);
		Assertions.assertThat(NeoRxBoltClientImpl.convertParameterValueType(mapper.createObjectNode().path("foo")))
				.isNull();
		Assertions.assertThat(NeoRxBoltClientImpl.convertParameterValueType(NullNode.getInstance())).isNull();
		Assertions.assertThat(NeoRxBoltClientImpl.convertParameterValueType(MissingNode.getInstance())).isNull();
		Assertions.assertThat(NeoRxBoltClientImpl.convertParameterValueType(mapper.readTree("123"))).isEqualTo(123);
		Assertions.assertThat(NeoRxBoltClientImpl.convertParameterValueType(mapper.readTree("123.45")))
				.isEqualTo(123.45);
	}

	@Test
	public void testCollect1() throws IOException, JsonProcessingException {
		getClient().execCypher("match (a:JUnitPerson) detach delete a");
		getClient().execCypher("create (p:JUnitPerson {name: \"Mark\"})\n"
				+ "create (e1:JUnitEvent {name: \"Event1\", timestamp: 1234})\n"
				+ "create (e2:JUnitEvent {name: \"Event2\", timestamp: 4567})\n" + " \n" + "create (p)-[:EVENT]->(e1)\n"
				+ "create (p)-[:EVENT]->(e2)\n");

		JsonNode n = getClient().execCypher("MATCH (p:JUnitPerson)-[:EVENT]->(e)\n"
				+ " RETURN p, COLLECT({eventName: e.name, eventTimestamp: e.timestamp}) as x").blockingFirst();
		Assertions.assertThat(n.path("p").path("name").asText()).isEqualTo("Mark");
		n.path("x").forEach(it -> {
			if (it.path("eventName").asText().equals("Event1")) {
				Assertions.assertThat(it.path("eventTimestamp").asInt()).isEqualTo(1234);
			}
			if (it.path("eventName").asText().equals("Event2")) {
				Assertions.assertThat(it.path("eventTimestamp").asInt()).isEqualTo(4567);
			}

		});
		Assertions.assertThat(n.path("x").size()).isEqualTo(2);

	}

	@Test
	public void testCollect() throws IOException {

		String id = UUID.randomUUID().toString();
		getClient().execCypher("create (a:JUnitSingleton {id:{id}}) return a", "id", id);

		JsonNode n = getClient().execCypher(
				"match (a:JUnitSingleton {id:{id}}) return a, collect({x:[{w:true,p:'hello'}],y:2,z:null}) as b", "id",
				id).blockingLast();

		System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(n));

		Assertions.assertThat(n.path("a").path("id").asText()).isEqualTo(id);
		Assertions.assertThat(n.path("b").isArray()).isTrue();
		Assertions.assertThat(n.path("b").size()).isEqualTo(1);
		Assertions.assertThat(n.path("b").path(0).path("y").asInt()).isEqualTo(2);
		Assertions.assertThat(n.path("b").path(0).path("z").isNull()).isTrue();
		Assertions.assertThat(n.path("b").path(0).path("x").get(0).path("p").asText()).isEqualTo("hello");
		Assertions.assertThat(n.path("b").path(0).path("x").get(0).path("w").asBoolean()).isTrue();

	}

	@Test
	public void testBuilder() {
		Assertions.assertThat(NeoRxClient.builder()).isInstanceOf(Builder.class);

		NeoRxClient.Builder b = NeoRxClient.builder();
		Assertions.assertThat(b.authToken).isEqualTo(AuthTokens.none());
		Assertions.assertThat(b.withCredentials("scott", "tiger").authToken).isNotNull();

		AuthToken token = AuthTokens.basic("abc", "def");

		Assertions.assertThat(b.withAuthToken(token).authToken).isSameAs(token);

		Assertions.assertThat(b.driver).isNull();
		Driver d = GraphDatabase.driver("bolt://localhost:1234");
		Assertions.assertThat(b.withDriver(d).driver).isSameAs(d);

		b = NeoRxClient.builder();
		Assertions.assertThat(b.mock).isFalse();
		Assertions.assertThat(b.withMockClient(true).mock).isTrue();

	}

	@Test
	public void testMock() {
		NeoRxClient.Builder b = NeoRxClient.builder();
		Assertions.assertThat(b.mock).isFalse();
		Assertions.assertThat(b.withMockClient(true).mock).isTrue();

		NeoRxClient c = b.build();
		Assertions.assertThat(c).isInstanceOf(MockNeoRxClient.class);
	}

	@Test
	public void testExecCypherAsListJsonArg() {
		String id = UUID.randomUUID().toString();
		ObjectNode n = mapper.createObjectNode().put("id", id);
		Assertions.assertThat(getClient().execCypherAsList("create (a:JUnitFoo) set a.id={id} return a", n).get(0)
				.path("id").asText()).isEqualTo(id);
	}

	@Test
	public void testCannotConnect() {
		try {
			NeoRxClient.builder().withUrl("bol://127.0.0.1:12345").build().execCypher("match (a:Foo) return a limit 10");
			Assertions.failBecauseExceptionWasNotThrown(NeoRxException.class);
		} catch (Exception e) {
			e.printStackTrace();
			Assertions.assertThat(e).isInstanceOf(NeoRxException.class).hasCauseInstanceOf(ClientException.class);
		}
	}
	@Test
	public void testSyntaxError() {
		try {
			getClient().execCypher("match (a:Foo return a");
			Assertions.failBecauseExceptionWasNotThrown(NeoRxException.class);
		} catch (Exception e) {
			Assertions.assertThat(e).isInstanceOf(NeoRxException.class).hasCauseInstanceOf(ClientException.class);
		}
	}

}
