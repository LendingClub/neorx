# NeoRx

[![Circle CI](https://circleci.com/gh/LendingClub/neorx.svg?style=svg)](https://circleci.com/gh/LendingClub/neorx)
[![Download](https://img.shields.io/maven-central/v/io.macgyver.neorx/neorx.svg)](http://search.maven.org/#search%7Cga%7C1%7Ca%3A%22neorx%22)
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
  <version>1.1.1</version>
</dependency>
```

or if you are lucky enough to be using Gradle:

```groovy
compile "io.macgyver.neorx:neorx:1.1.1"
```


## Recipes

Instantiate the (thread-safe) client:
```java
// to http://localhost:7474
NeoRxClient client = new NeoRxClientBuilder().build(); 
	
// to https://neo4j.example.com:7473
client = new NeoRxClientBuilder.url("https://neo4j.example.com:7473").build();

// to https://neo4j.example.com:7473 with certificate validation disabled
client = new NeoRxClientBuilder().url("https://neo4j.example.com:7473").withCertificateValidation(false).build();

// with basic auth
client = new NeoRxClientBuilder().url("http://localhost:7474").credentials("myusername","mypassword").build();
	
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

Sometimes you want to be able to set properties in bulk without specifying them in cypher.  
This will upsert the node and update the values of ```foo``` and ```bar```:
```
ObjectNode p = mapper.createObjectNode().put("foo","123").put("bar","456");
client.execCypher("MERGE (x:Dummy {name: "something" }) set x += {props} return x","props",p); 
```

## Changes

### 1.2.0

* upgrade to RxJava 1.1.0
* upgrade to OkHttp 2.7.2
* upgrade to Gradle 2.10