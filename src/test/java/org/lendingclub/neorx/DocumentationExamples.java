package org.lendingclub.neorx;

import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;

import io.reactivex.functions.Consumer;

@SuppressWarnings("unused")
public class DocumentationExamples {

	NeoRxClient client;
	
	
	public void d1() {
		NeoRxClient client = new NeoRxClient.Builder().build();

	}

	public void d2() {
		NeoRxClient client = new NeoRxClient.Builder().withUrl("bolt://neo4j.example.com:7686").build();

	}

	public void d3() {
		NeoRxClient client = new NeoRxClient.Builder().withUrl("bolt://neo4j.example.com:7686").build();
		client.execCypher("match (p:Person) where p.born>1980 return p").subscribe(it -> System.out.println(it));
	}

	public void d4() {
	
		client.execCypher("match (p:Person) where p.born>1980 return p").subscribe(new Consumer<JsonNode>() {
			public void accept(JsonNode it) throws Exception {
				System.out.println(it.path("p.name").asText() + " - " + it.path("p.born").asInt());
			}
		});
	}
	
	public void d5() {

		List<JsonNode> people = client.execCypher("match (p:Person) where p.born>1980 return p")
				.toList().blockingGet();
	}
}
