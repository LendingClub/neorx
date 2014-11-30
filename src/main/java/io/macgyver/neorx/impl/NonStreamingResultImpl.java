package io.macgyver.neorx.impl;

import java.util.Iterator;


import io.macgyver.neorx.ResultMetaData;
import io.macgyver.neorx.Row;
import rx.Observable;

import com.fasterxml.jackson.databind.node.ObjectNode;

public class NonStreamingResultImpl  {

	ObjectNode data;
	ResultMetaDataImpl metaData;
	
	public NonStreamingResultImpl(ObjectNode n) {
		this.data = n;
		metaData = new ResultMetaDataImpl(data);
	}
	
	
	public ResultMetaData getResultMetaData() {
		return metaData;
	}
	public Iterable<Row> iterableRows() {
		return new Iterable<Row>() {
			
			@Override
			public Iterator<Row> iterator() {
				return new RowIterator(data);
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
