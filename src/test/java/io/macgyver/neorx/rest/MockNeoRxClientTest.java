package io.macgyver.neorx.rest;

import java.io.IOException;
import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class MockNeoRxClientTest {

	ObjectMapper mapper = new ObjectMapper();
	
	@Test
	public void testEmptyStack() throws IOException, JsonProcessingException{
		
		MockNeoRxClient x = new MockNeoRxClient();
	
		try {
			x.execCypher("match (t:Test) return t");
		}
		catch (Exception e) {
			Assertions.assertThat(e).isInstanceOf(IllegalStateException.class).hasMessageContaining("mock response stack is empty");
		}
		
		String raw = "{\"results\":[{\"columns\":[\"p\"],\"data\":[{\"row\":[{\"name\":\"Carrie-Anne Moss\",\"born\":1967}]}]}],\"errors\":[]}";

	
		x.enqueue(raw);
		
		JsonNode response = x.execCypher("match (t:Test, params) return t").toBlocking().first();
		
		
		
		
		try {
			x.execCypher("match (t:Test) return t");
		}
		catch (Exception e) {
			Assertions.assertThat(e).isInstanceOf(IllegalStateException.class).hasMessageContaining("mock response stack is empty");
		}
		
	}
	
	@Test
	public void testBuilder() {
		MockNeoRxClient.MockResponseBuilder b = new MockNeoRxClient.MockResponseBuilder();
		b.row("a","1","b",2);
		ObjectNode x = b.build();
		
		
	}
	@Test
	public void testBuilder2() {
		MockNeoRxClient c = new MockNeoRxClient();
		c.enqueue().row("row",0,"name","Jerry").row("row",1,"name","Phil").endResponse();
		
		List<JsonNode> n = c.execCypher("").toList().toBlocking().first();
		
		Assertions.assertThat(n.size()).isEqualTo(2);
		Assertions.assertThat(n.get(0).get("name").asText()).isEqualTo("Jerry");
		Assertions.assertThat(n.get(1).get("name").asText()).isEqualTo("Phil");
		
	}
	
	@Test
	public void testMisuse() {
		try {
		new MockNeoRxClient.MockResponseBuilder().endResponse();
		}
		catch (IllegalStateException e) {
			Assertions.assertThat(e).hasMessageContaining("can only be called");
		}
	}
}
