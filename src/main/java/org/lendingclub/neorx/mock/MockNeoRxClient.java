package org.lendingclub.neorx.mock;

import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

import org.lendingclub.neorx.NeoRxClient;
import org.neo4j.driver.v1.Driver;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.reactivex.Observable;

public class MockNeoRxClient extends NeoRxClient {

	Deque<List<JsonNode>> queue = new LinkedList<>();
	
 
	public MockNeoRxClient enqueue(JsonNode... n) {
		
		List<JsonNode> list = new ArrayList<>();
		for (JsonNode node: n) {
			list.add(node);
		}
		queue.add(list);
		return this;
	}
	@Override
	public boolean checkConnection() {
		return true;
	}

	
	@Override
	public Observable<JsonNode> execCypher(String cypher, Object... params) {
		return Observable.fromIterable(queue.removeFirst());
	}

	

	public Driver getDriver() {
		throw new UnsupportedOperationException();
	}
}
