package org.lendingclub.neorx;

import java.util.Map;

import org.assertj.core.api.Assertions;
import org.assertj.core.util.Lists;
import org.junit.Test;
import org.neo4j.driver.internal.InternalNode;
import org.neo4j.driver.v1.Value;
import org.neo4j.driver.v1.Values;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.IntNode;
import com.fasterxml.jackson.databind.node.LongNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Maps;

public class NeoRxBoltClientImplTest {

	
	@Test
	public void testConvert() {
		
		ObjectNode node = new ObjectMapper().createObjectNode().put("a",1);
		
		Assertions.assertThat(NeoRxBoltClientImpl.convertResultToJson(node)).isSameAs(node);
		Assertions.assertThat(NeoRxBoltClientImpl.convertResultToJson(null).isNull()).isTrue();
		Assertions.assertThat(NeoRxBoltClientImpl.convertResultToJson("foo").isTextual()).isTrue();
		Assertions.assertThat(NeoRxBoltClientImpl.convertResultToJson(123.45).isNumber()).isTrue();
		Assertions.assertThat(NeoRxBoltClientImpl.convertParameterValueType(new LongNode(98765))).isInstanceOf(Long.class).isEqualTo(98765L);
		Assertions.assertThat(NeoRxBoltClientImpl.convertParameterValueType(new IntNode(12345))).isInstanceOf(Integer.class).isEqualTo(12345);
		Map<String,Value> m = Maps.newHashMap();
		m.put("a", Values.value(123));
		m.put("fizz", Values.value("buzz"));
		InternalNode n = new InternalNode(123,Lists.newArrayList("Foo"),m);
		
		JsonNode converted = NeoRxBoltClientImpl.convertResultToJson(n);
		Assertions.assertThat(converted.path("a").asInt()).isEqualTo(123);
		Assertions.assertThat(converted.path("fizz").asText()).isEqualTo("buzz");
	}
	
	@Test
	public void testCheckConnection() {
		Assertions.assertThat(NeoRxClient.builder().withUrl("bolt://localhost:34763").build().checkConnection()).isFalse();
		Assertions.assertThat(NeoRxClient.builder().withUrl("bolt://somerandomhost:34763").build().checkConnection()).isFalse();
	}
	
}
