package io.macgyver.neorx.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;

import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

public class NeoRxFunctionsTest {

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
}
