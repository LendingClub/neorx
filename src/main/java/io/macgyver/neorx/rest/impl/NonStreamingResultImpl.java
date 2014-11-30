package io.macgyver.neorx.rest.impl;

import java.util.Iterator;

import io.macgyver.neorx.rest.ResultMetaData;
import io.macgyver.neorx.rest.Row;
import rx.Observable;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class NonStreamingResultImpl  {

	ArrayNode data;
	ResultMetaDataImpl metaData;
	
	public NonStreamingResultImpl(ObjectNode n) {
		ObjectNode r = (ObjectNode) n.path("results").get(0);
		if (r==null) {
			System.out.println(n);
		}
		data = (ArrayNode) r.get("data");
		metaData = new ResultMetaDataImpl(r);
		
	
	}
	
	
	public ResultMetaData getResultMetaData() {
		return metaData;
	}
	public Iterable<Row> iterableRows() {
		return new Iterable<Row>() {
			
			@Override
			public Iterator<Row> iterator() {
				
				
				return new RowIterator(data,metaData);
			}
		};
		
	}
	public int size() {
		return data.path("data").size();
	}
	public Observable<Row> rows() {
		return Observable.from(iterableRows());
	}
}
