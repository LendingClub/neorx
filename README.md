

dummy change
# NeoRx
[![Gitter](https://badges.gitter.im/Join Chat.svg)](https://gitter.im/if6was9/NeoRx?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)

[![Build Status](http://ci.macgyver.io/buildStatus/icon?job=neorx-ci)](http://ci.macgyver.io/job/neorx-ci/)

Neo4j REST Client implemented using:

[RxJava](https://github.com/ReactiveX/RxJava) - a powerful fluent API for processing results in a functional/reactive pattern

[Jackson](https://github.com/FasterXML/jackson) - the most fluent Java API for processing JSON 

[OkHttp](http://square.github.io/okhttp/) - a fluent HTTP client

The Neo4J Cypher REST API combines the power of the Cypher query language with a JSON-native API that is very easy to process. 

The examples below should make this very clear.

## Obtaining

NeoRx is available from maven central:

```xml
<dependency>
  <groupId>io.macgyver.neorx</groupId>
  <artifactId>neorx</artifactId>
  <version>1.0.4</version>
</dependency>
```

or if you are lucky enough to be using Gradle:

```groovy
compile "io.macgyver.neorx:neorx:1.0.4"
```


## Recipes

Instantiate the (thread-safe) client:
```java
// to http://localhost:7474
NeoRxClient client = new NeoRxClient(); 
	
// to https://neo4j.example.com:7473
client = new NeoRxClient("https://neo4j.example.com:7473");

// to https://neo4j.example.com:7473 with certificate validation disabled
client = new NeoRxClient("https://neo4j.example.com:7473",false);

// with basic auth
client = new NeoRxClient("http://localhost:7474","myusername","mypassword");
	
```

Find all of the people in the graph who are born after 1980 and print the results using a Java 8 Lambda:
```java
client.execCypher("match (p:Person) where p.born>1980 return p")
	.subscribe(it -> System.out.println(it));

Output:

{"name":"Jonathan Lipnicki","born":1990}
{"name":"Natalie Portman","born":1981}
{"name":"Emile Hirsch","born":1985}
{"name":"Rain","born":1982}
```

Same query, but print just the name:
```java
	client.execCypher("match (p:Person) where p.born>1960 return p")
	  .subscribe(
	  	it -> System.out.println(it.path("name").asText()
	  	);
	
Output:

Jonathan Lipnicki
Natalie Portman
Emile Hirsch
Rain
```

And now return the attributes individually:
```java
client.execCypher("match (p:Person) where p.born>1980 return p.name, p.born")
	.subscribe(
		it -> System.out.println(it.path("p.name").asText()+" - "+it.path("p.born").asInt())
	);
	
Output:

Jonathan Lipnicki - 1990
Natalie Portman - 1981
Emile Hirsch - 1985
Rain - 1982
```
If you are stuck with Java 7:

```java
neoRxClient.execCypher("match (p:Person) where p.born>1980 return p")
.subscribe(
	new Action1<JsonNode>() {

		@Override
		public void call(JsonNode t1) {
			System.out.println("Name: "+t1.path("name").asText();
		}
	});
```


If you just want a list:
```
List<JsonNode> people = client.execCypher("match (p:Person) where p.born>1980 return p")
	.toList()
	.toBlocking()
	.first();
```

And the same operation, through a convenience method:
```
List<JsonNode> people = client
	.execCypherAsList("match (p:Person) where p.born>1980 return p");
```		  

This example shows the use of an Rx function to transform the result from JsonNode to String:

```java
List<String> names = neoRxClient
	.execCypher("match (p:Person) where p.born>1980 return p.name")
	.flatMap(NeoRxFunctions.jsonNodeToString())
	.toList()
	.toBlocking().first();
```

Now, let's parameterize the cypher quey:
```
  List<JsonNode> people = client
  .execCypherAsList("match (p:Person) where p.born>{year} return p","year",1980);
```
