# NeoRx

[![Build Status](http://ci.macgyver.io/buildStatus/icon?job=neorx-ci)](http://ci.macgyver.io/job/neorx-ci/)

Neo4j REST Client implemented using RxJava.


## Recipes

Find all of the people in the graph who were born after 1960 and
return their nodes (with properties) as a List&lt;JsonNode&gt;:
```java
		List<JsonNode> people = new NeoRxClient()
		  .execCypher("match (m:Person) where m.born>{born} return m", 
		  "born", 1960)
		  .toList()
		  .toBlocking().first();
```

Find all of the people in the graph who were born after 1960 and
return their names as a List&lt;String&gt;:
```java
		List<String> names = new NeoRxClient()
		  .execCypher("match (m:Person) where m.born>{born} return m.name", 
		  "born", 1960)
		  .flatMap(NeoRxFunctions.jsonNodeToString())
		  .toList()
		.toBlocking().first();
```
