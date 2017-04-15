# NeoRx

[![Circle CI](https://circleci.com/gh/LendingClub/neorx.svg?style=svg)](https://circleci.com/gh/LendingClub/neorx)
[ ![Download](https://api.bintray.com/packages/lendingclub/OSS/NeoRx/images/download.svg) ](https://bintray.com/lendingclub/OSS/NeoRx/_latestVersion)

NeoRx is a Neo4j client implemented using the Neo4j BOLT driver.  The goal is to provide a simple,fluent, json-native API that focuses on ease-of-use.

The underlying implementation is based on:

[neo4j-java-driver](https://github.com/neo4j/neo4j-java-driver) - Neo4j's Java Driver

[RxJava](https://github.com/ReactiveX/RxJava) - a powerful fluent API for processing results in a functional/reactive pattern

[Jackson](https://github.com/FasterXML/jackson) - the most fluent Java API for processing JSON 

The examples below should make this very clear.



## Recipes

Instantiate the (thread-safe) client:
```java
// to bolt://localhost:7687
NeoRxClient client = new NeoRxClient.Builder().build();
    
// to bolt://neo4j.example.com:7687
NeoRxClient client = new NeoRxClient.Builder()
  .withUrl("bolt://neo4j.example.com:7686")
  .build(); 
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
client.execCypher("match (p:Person) where p.born>1980 return p").subscribe(new Consumer<JsonNode>() {
  public void accept(JsonNode it) throws Exception {
    System.out.println(it.path("p.name").asText() + " - " + it.path("p.born").asInt());
  }
});
```


If you just want a list:
```
List<JsonNode> people = 
  client.execCypher("match (p:Person) where p.born>1980 return p")
  .toList()
  .blockingGet();
```

And the same operation, through a convenience method:
```
List<JsonNode> people = client
  .execCypherAsList("match (p:Person) where p.born>1980 return p");
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

## Obtaining


NeoRx is available from JCenter from the following coordinates:

```xml
<dependency>
  <groupId>org.lendingclub.neorx</groupId>
  <artifactId>neorx</artifactId>
  <version>2.0.3</version>
</dependency>
```

or with Gradle:

```groovy
compile "org.lendinglcub.neorx:neorx:2.0.3"
```

Note: Artifcacts are not *yet* published to maven central.  We are working on that.

## Changes
### 2.0.3 
* Gradle 3.5
* Expose MockNeoRxClient in builder

### 2.0.2
* fix handling of complex nested return types from COLLECT(), etc.

### 2.0.1
* fix handling of JsonNode types passed as parameters

### 2.0.0
* swtich from REST to BOLT protocol
* RxJava 2.x
* package rename from io.macgyver.neorx to org.lendingclub.neorx

### 1.3.4

* properly close OkHttp response body
* upgrade to RxJava 1.1.9
* upgrade to Jackson 2.8.1
* upgrade to OkHttp 3.4.1


### 1.2.0

* upgrade to RxJava 1.1.0
* upgrade to OkHttp 2.7.2
* upgrade to Gradle 2.10
