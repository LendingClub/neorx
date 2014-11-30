package io.macgyver.neorx.impl;

import static org.assertj.core.api.Assertions.assertThat;
import io.macgyver.neorx.Row;

import java.io.IOException;
import java.util.NoSuchElementException;

import org.assertj.core.api.Assertions;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class RowIteratorTest {

	@Test
	public void createTest() throws IOException {

		String x = "{\"results\":[{\"columns\":[\"m\",\"m.abc\"],\"data\":[{\"row\":[{\"abc\":123},123]},{\"row\":[{\"def\":123},null]}]}],\"errors\":[]}";

		ObjectNode n = (ObjectNode) new ObjectMapper().readTree(x);

		ResultMetaDataImpl md = new ResultMetaDataImpl(n.get("results").get(0));
		RowIterator ri = new RowIterator(n.get("results").get(0).get("data"),
				md);

		Assertions.assertThat(ri.hasNext()).isTrue();

		Row x1 = ri.next();
		Assertions.assertThat(x1).isInstanceOf(Row.class);
		Assertions.assertThat(x1.getString("m.abc")).isEqualTo("123");
		Assertions.assertThat(x1.getField("m").get("abc").asText()).isEqualTo(
				"123");

	}

	@Test
	public void testX() throws IOException {
		String x = "{\"results\":[\n" + "    \n" + "    {\n"
				+ "        \"columns\":[\"a\",\"age\",\"name\"],\n"
				+ "        \"data\":[\n"
				+ "            {\"row\":[1,39,\"Rob\"]},\n"
				+ "            {\"row\":[4,9,\"Oliver\"]}\n" + "            \n"
				+ "            \n" + "        ]\n" + "        \n" + "    }\n"
				+ "    \n" + "]}";

		ObjectNode n = (ObjectNode) new ObjectMapper().readTree(x);
		ResultMetaDataImpl md = new ResultMetaDataImpl(n.get("results").get(0));
		RowIterator ri = new RowIterator(n.get("results").get(0).get("data"),
				md);

		Row r = ri.next();
		assertThat(r.getField("name").asText()).isEqualTo("Rob");
		assertThat(r.getString("name")).isEqualTo("Rob");
		assertThat(r.getString("age")).isEqualTo("39");
		assertThat(r.getField("age").asInt()).isEqualTo(39);
		r = ri.next();

		assertThat(r.getField("name").asText()).isEqualTo("Oliver");
		assertThat(r.getString("name")).isEqualTo("Oliver");
		assertThat(r.getString("age")).isEqualTo("9");
		assertThat(r.getField("age").asInt()).isEqualTo(9);

		assertThat(ri.hasNext()).isFalse();

	}

	@Test
	public void testOverrunResults() throws IOException {
		String x = "{\"results\":[\n" + "    \n" + "    {\n"
				+ "        \"columns\":[\"a\",\"age\",\"name\"],\n"
				+ "        \"data\":[\n"
				+ "            {\"row\":[1,39,\"Rob\"]},\n"
				+ "            {\"row\":[4,9,\"Oliver\"]}\n" + "            \n"
				+ "            \n" + "        ]\n" + "        \n" + "    }\n"
				+ "    \n" + "]}";

		ObjectNode n = (ObjectNode) new ObjectMapper().readTree(x);
		ResultMetaDataImpl md = new ResultMetaDataImpl(n.get("results").get(0));
		RowIterator ri = new RowIterator(n.get("results").get(0).get("data"),
				md);

		ri.next();
		ri.next();
		try {
			ri.next();
			Assertions
					.failBecauseExceptionWasNotThrown(NoSuchElementException.class);
		} catch (NoSuchElementException e) {

		}

	}
}
