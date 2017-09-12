package org.lendingclub.neorx;

import org.neo4j.driver.v1.Driver;

import com.fasterxml.jackson.databind.JsonNode;

import io.reactivex.Observable;

class NeoRxClientFacade extends NeoRxClient {

	NeoRxClient client;

	protected NeoRxClientFacade(NeoRxClient client) {
		this.client = client;
	}
	@Override
	public boolean checkConnection() {
		return client.checkConnection();
	}

	@Override
	public Observable<JsonNode> execCypher(String cypher, Object... params) {
		return client.execCypher(cypher, params);
	}

	@Override
	public Driver getDriver() {
		return client.getDriver();
	}
	@Override
	public CypherStats getStats() {
		return super.getStats();
	}
	
	
	
}
