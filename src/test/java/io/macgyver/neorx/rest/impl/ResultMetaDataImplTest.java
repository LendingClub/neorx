package io.macgyver.neorx.rest.impl;

import io.macgyver.neorx.rest.NeoRxException;
import io.macgyver.neorx.rest.impl.ResultMetaDataImpl;

import org.assertj.core.api.Assertions;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class ResultMetaDataImplTest {

	static ObjectMapper mapper = new ObjectMapper();
	
	@Test
	public void testResultSetMetaData() {
		ObjectNode d = mapper.createObjectNode();
		
		ArrayNode c = mapper.createArrayNode();
		
		
		c.add("col1");
		c.add("col2");
		c.add("v.col1");
		
		d.set("columns", c);
		
		ResultMetaDataImpl rmd = new ResultMetaDataImpl(d);
		Assertions.assertThat(rmd.getFieldNames()).hasSize(3);
		
		Assertions.assertThat(rmd.getFieldNames()).containsExactly("col1","col2","v.col1");
		Assertions.assertThat(rmd.getField("v.col1")).isEqualTo(2);
		
		
		
	}
	
	@Test 
	public void testNull() {
		
		try {
		ResultMetaDataImpl m = new ResultMetaDataImpl(mapper.createObjectNode().set("columns", mapper.createArrayNode()));
		m.getField("test");
		}
		catch (NeoRxException e) {
			Assertions.assertThat(e).hasMessageContaining("no such field: test");
		}
		
	}
}
