package io.macgyver.neorx.rest.impl;


import java.util.Iterator;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Preconditions;

public class RowIterator implements Iterator<ObjectNode> {

	static ObjectMapper mapper = new ObjectMapper();
	Iterator<JsonNode> iterator;
	ResultMetaDataImpl metaData;
	
	public RowIterator(JsonNode results, ResultMetaDataImpl md) {
		Preconditions.checkNotNull(results);
		Preconditions.checkNotNull(md);
		Preconditions.checkNotNull(results instanceof ArrayNode);
		metaData = md;
		iterator = results.elements();
	}
	@Override
	public boolean hasNext() {
		return iterator.hasNext();
	}

	@Override
	public ObjectNode next() {
		return transform(iterator.next());
		
		
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}

	ObjectNode transform(JsonNode input) {
		JsonNode row = input.path("row");
		ObjectNode output = mapper.createObjectNode();
		for (Map.Entry<String,Integer> entry: metaData.columnMap.entrySet()) {
	
			JsonNode n = row.get(entry.getValue());
		
			output.set(entry.getKey(), n);
		}
		return output;
	}
}
