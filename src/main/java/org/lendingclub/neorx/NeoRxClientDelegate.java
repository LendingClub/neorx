package org.lendingclub.neorx;

import org.neo4j.driver.v1.Driver;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.reactivex.Observable;

class NeoRxClientDelegate extends NeoRxClient {

	NeoRxClient client;

	@Override
	public boolean checkConnection() {
		return client.checkConnection();
	}

	@Override
	public Observable<JsonNode> execCypher(String cypher, Object... params) {
		return client.execCypher(cypher, params);
	}

	@Override
	public Observable<JsonNode> execCypher(String cypher, ObjectNode params) {
		return client.execCypher(cypher, params);
	}

	@Override
	public Driver getDriver() {
		return client.getDriver();
	}
	
	
}
