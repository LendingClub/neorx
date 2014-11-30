package io.macgyver.neorx.rest.impl;

import io.macgyver.neorx.rest.Row;

import java.util.Iterator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.google.common.base.Preconditions;

public class RowIterator implements Iterator<Row> {

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
	public Row next() {
		JsonNode n = iterator.next();
	
		return new RowImpl(metaData,n);
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}

}
