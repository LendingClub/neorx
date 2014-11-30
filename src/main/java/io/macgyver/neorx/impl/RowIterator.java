package io.macgyver.neorx.impl;

import io.macgyver.neorx.Row;

import java.util.Iterator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class RowIterator implements Iterator<Row> {

	Iterator<JsonNode> iterator;
	ResultMetaDataImpl metaData;
	
	public RowIterator(ObjectNode results) {
		metaData = new ResultMetaDataImpl(results);
		iterator = ((ArrayNode)results.get("data")).elements();
	}
	@Override
	public boolean hasNext() {
		return iterator.hasNext();
	}

	@Override
	public Row next() {
		JsonNode n = iterator.next();
		return new RowImpl(metaData,n);
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}

}
