package org.lendingclub.neorx;

import java.util.Map;

import org.assertj.core.api.Assertions;
import org.assertj.core.util.Lists;
import org.junit.Test;
import org.neo4j.driver.internal.InternalNode;
import org.neo4j.driver.v1.Value;
import org.neo4j.driver.v1.Values;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Maps;

public class NeoRxBoltClientImplTest {

	
	@Test
	public void testConvert() {
		Assertions.assertThat(NeoRxBoltClientImpl.convertResultToJson(null).isNull()).isTrue();
		Assertions.assertThat(NeoRxBoltClientImpl.convertResultToJson("foo").isTextual()).isTrue();
		Assertions.assertThat(NeoRxBoltClientImpl.convertResultToJson(123.45).isNumber()).isTrue();
	
		Map<String,Value> m = Maps.newHashMap();
		m.put("a", Values.value(123));
		m.put("fizz", Values.value("buzz"));
		InternalNode n = new InternalNode(123,Lists.newArrayList("Foo"),m);
		
		JsonNode converted = NeoRxBoltClientImpl.convertResultToJson(n);
		Assertions.assertThat(converted.path("a").asInt()).isEqualTo(123);
		Assertions.assertThat(converted.path("fizz").asText()).isEqualTo("buzz");
	}
}
