# NeoRx

[![Build Status](http://ci.macgyver.io/buildStatus/icon?job=neorx-ci)](http://ci.macgyver.io/job/neorx-ci/)

Neo4j REST Client implemented using RxJava, Jackson, and OkHttp.

[RxJava](https://github.com/ReactiveX/RxJava) provides a powerful fluent API for processing result data

[Jackson](https://github.com/FasterXML/jackson) provides a fluent API for processing JSON data

[OkHttp](http://square.github.io/okhttp/) provides a fluent HTTP client


## Recipes

Instantiate the (thread-safe) client:
```java
	// to http://localhost:7474
	NeoRxClient neoRxClient = new NeoRxClient(); 
	
	// to https://neo4j.example.com:7473
	NeoRxClient neoRxClient1 = new NeoRxClient("https://neo4j.example.com:7473");

	// to https://neo4j.example.com:7473 with certificate validation disabled
	NeoRxClient neoRxClient2 = new NeoRxClient("https://neo4j.example.com:7473",false);

	// with basic auth
	NeoRxClient neoRxClient3 = new NeoRxClient("http://localhost:7474","myusername","mypassword");
	
```


Find all of the people in the graph who were born after 1960 and
return their nodes (with properties) as a List&lt;JsonNode&gt;:
```java
		List<JsonNode> people = neoRxClient
		  .execCypher("match (m:Person) where m.born>{born} return m", 
		  "born", 1960)
		  .toList()
		  .toBlocking().first();
```

Find all of the people in the graph who were born after 1960 and
return their names as a List&lt;String&gt;:
```java
		List<String> names = neoRxClient
		  .execCypher("match (m:Person) where m.born>{born} return m.name", 
		  "born", 1960)
		  .flatMap(NeoRxFunctions.jsonNodeToString())
		  .toList()
		  .toBlocking().first();
```
