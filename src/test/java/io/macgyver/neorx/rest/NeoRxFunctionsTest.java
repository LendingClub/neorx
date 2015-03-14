package io.macgyver.neorx.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Test;

import rx.Observable;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.TextNode;

public class NeoRxFunctionsTest extends NeoRxUnitTest {

	@Test
	public void testExtractField() {

		assertThat(
				NeoRxFunctions
						.extractField("foo")
						.call(new ObjectMapper().createObjectNode().put("foo",
								"bar")).toBlocking().first().asText())
				.isEqualTo("bar");
		
		Assert.assertNotNull("should return a value",NeoRxFunctions
				.extractField("foo")
				.call(new ObjectMapper().createObjectNode().put("abc",
						"bar")).toBlocking().first());
			
				assertTrue("should return a JsonNull",NeoRxFunctions
						.extractField("foo")
						.call(new ObjectMapper().createObjectNode().put("abc",
								"bar")).toBlocking().first().isNull());
	}
	
	@Test
	public void testX() throws JsonProcessingException, IOException{
		Assertions.assertThat(NeoRxFunctions.jsonNodeToString().call(null).toBlocking().first()).isNull();
		Assertions.assertThat(NeoRxFunctions.jsonNodeToString().call(NullNode.getInstance()).toBlocking().first()).isNull();
		
		Assertions.assertThat(NeoRxFunctions.jsonNodeToString().call(TextNode.valueOf("test")).toBlocking().first()).isEqualTo("test");
	}
}
