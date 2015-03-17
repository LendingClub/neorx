package io.macgyver.neorx.rest;

import io.macgyver.neorx.rest.impl.GuavaPreconditions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import rx.Observable;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class MockNeoRxClient extends NeoRxClient {

	Deque<Object> fakeResponseStack = new LinkedList<Object>();

	@Override
	protected ObjectNode execRawCypher(String cypher, ObjectNode params) {

		if (fakeResponseStack.isEmpty()) {
			throw new IllegalStateException("mock response stack is empty");
		}
		Object x= fakeResponseStack.pop();
		if (x instanceof ObjectNode) {
			return (ObjectNode) x;
		}
		else if (x instanceof MockResponseBuilder) {
			return ((MockResponseBuilder)x).build();
		}
		else {
			throw new IllegalStateException();
		}

	}

	public MockResponseBuilder enqueue() {
		MockResponseBuilder b = new MockResponseBuilder();
		b.mockClient = this;
		fakeResponseStack.push(b);
		return b;
	}
	public MockNeoRxClient enqueue(ObjectNode n) {
		GuavaPreconditions.checkNotNull(n);
		fakeResponseStack.push(n);
		return this;
	}

	public MockNeoRxClient enqueue(String s) {
		try {
			ObjectNode fakeResponse = (ObjectNode) mapper.readTree(s);
			fakeResponseStack.push(fakeResponse);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return this;
	}



	public static class MockResponseBuilder {
		Logger logger = LoggerFactory.getLogger(MockResponseBuilder.class);
		private List<String> columns = new ArrayList<String>();
		private List<Map<String, Object>> dataList = new ArrayList<Map<String,Object>>();
		private MockNeoRxClient mockClient;
		
		public MockNeoRxClient endResponse() {
			if (mockClient==null) {
				throw new IllegalStateException("endResponse() can only be called after enqueue()");
			}
			MockNeoRxClient c = mockClient;
			mockClient = null;
			return c;
		}
		public MockResponseBuilder row(Object... tuples) {
			Map<String, Object> t = new HashMap<String, Object>();
			for (int i = 0; i < tuples.length; i += 2) {
				if (!columns.contains(tuples[i].toString())) {
					columns.add(tuples[i].toString());
				}
				t.put(tuples[i].toString(), tuples[i + 1]);
				
			}
			dataList.add(t);
			return this;
		
		}

		public ObjectNode build() {
			ObjectNode r = mapper.createObjectNode();
			ArrayNode results = mapper.createArrayNode();
			r.set("results",results);
			ObjectNode singleResult = mapper.createObjectNode();
			results.add(singleResult);
			ArrayNode cols = mapper.createArrayNode();
			for (String col: columns) {
				cols.add(col);
			}
			singleResult.set("columns",cols);
			
			ArrayNode data = mapper.createArrayNode();
			singleResult.set("data", data);
			
			for (Map<String,Object> ds: dataList) {
		
				ObjectNode row = mapper.createObjectNode();
				ArrayNode dd = mapper.createArrayNode();
				row.set("row", dd);
				data.add(row);
				for (String c: columns) {
					Object val = ds.get(c);
					if (val==null) {
						dd.add((String)null);
					}
					else if (val instanceof String) {
						dd.add((String)val);
					}
					else if (val instanceof Integer) {
						dd.add((Integer)val);
					}
					else if (val instanceof Double) {
						dd.add((Double)val);
					}
					else if (val instanceof Long) {
						dd.add((Long)val);
					}
					else if (val instanceof Boolean) {
						dd.add((Boolean)val);
					}
					else {
						throw new IllegalArgumentException("type not supported: column="+c+" class="+val.getClass().getName());
						
					}
					
				}
			}
			r.set("errors", mapper.createArrayNode());
			return r;
		}
	}
}
