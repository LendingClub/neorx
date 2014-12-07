package io.macgyver.neorx.rest;

import java.io.IOException;

import rx.Observable;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class FakeNeoRxClient extends NeoRxClient {

	ObjectNode fakeResponse;
	@Override
	protected ObjectNode execRawCypher(String cypher, ObjectNode params) {
	
		if (fakeResponse==null) {
			throw new IllegalStateException("must set fake response");
		}
		return fakeResponse;
		
	}

	
	public void setResponse(ObjectNode n) {
		this.fakeResponse = n;
	}
	public void setResponse(String s) {
		try {
		fakeResponse = (ObjectNode) mapper.readTree(s);
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static FakeNeoRxClient withResponse(ObjectNode json) {
		FakeNeoRxClient x = new FakeNeoRxClient();
		x.setResponse(json);
		return x;
	}
	public static FakeNeoRxClient withResponse(String json) {
		FakeNeoRxClient x = new FakeNeoRxClient();
		x.setResponse(json);
		return x;
	}
}
