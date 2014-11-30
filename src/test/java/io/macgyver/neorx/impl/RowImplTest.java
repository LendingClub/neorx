package io.macgyver.neorx.impl;

import io.macgyver.neorx.Row;

import org.assertj.core.api.Assertions;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class RowImplTest {

	ObjectMapper mapper = new ObjectMapper();

	Row createTestRow() {

		ObjectNode d = mapper.createObjectNode();

		ArrayNode c = mapper.createArrayNode();

		c.add("col1");
		c.add("col2");
		c.add("v.col1");
		c.add("x");
		d.set("columns", c);

		ArrayNode data = mapper.createArrayNode();

		ResultMetaDataImpl rmd = new ResultMetaDataImpl(d);

		ObjectNode row = mapper.createObjectNode();
		ArrayNode vals = mapper.createArrayNode();
		vals.add("a");
		vals.add("b");
		vals.add("a");
		
		ObjectNode x = mapper.createObjectNode();
		x.put("v",1);
		vals.add(x);
		row.set("row", vals);
		RowImpl ri = new RowImpl(rmd, row);
		return ri;
	}

	@Test
	public void testX() {
		Row r = createTestRow();
		Assertions.assertThat(r.getMetaData()).isNotNull();
		Assertions.assertThat(r.getField("col2").asText()).isEqualTo("b");
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testInvalidColumn() {
		createTestRow().getField("notFound");
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testNullArg() {
		createTestRow().getField(null);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testInvalidColumn2() {
		createTestRow().getString("notFound");
	}
	
	@Test
	public void testNode() {
		Assertions.assertThat(createTestRow().getField("x")).isInstanceOf(ObjectNode.class);
		Assertions.assertThat(createTestRow().getField("x").get("v").asInt()).isEqualTo(1);
	}
}
