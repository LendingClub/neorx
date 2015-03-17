package io.macgyver.neorx.rest.impl;

import java.util.Iterator;

import io.macgyver.neorx.rest.NeoRxException;

import rx.Observable;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class NonStreamingResultImpl  {

	ArrayNode data;
	ResultMetaDataImpl metaData;
	
	public NonStreamingResultImpl(ObjectNode n) {
		ObjectNode r = (ObjectNode) n.path("results").get(0);
		if (r==null) {
			throw new NeoRxException("malformed response: expected 'results' object node");
		}
		data = (ArrayNode) r.get("data");
		metaData = new ResultMetaDataImpl(r);
		
	
	}
	
	
	public ResultMetaDataImpl getResultMetaData() {
		return metaData;
	}
	public Iterable<JsonNode> iterableRows() {
		return new Iterable<JsonNode>() {
			
			@Override
			public Iterator<JsonNode> iterator() {
				
				
				return new RowIterator(data,metaData);
			}
		};
		
	}
	public int size() {
		return data.path("data").size();
	}
	public Observable<JsonNode> rows() {
		return Observable.from(iterableRows());
	}
}
