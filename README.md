# WoT-TD-Java

[![Project Status: WIP – Initial development is in progress, but there has not yet been a stable, usable release suitable for the public.](https://www.repostatus.org/badges/latest/active.svg)](https://www.repostatus.org/#active)
![build](https://github.com/Interactions-HSG/wot-td-java/workflows/build/badge.svg)
[![](https://www.code-inspector.com/project/8927/score/svg)](https://frontend.code-inspector.com/public/project/8927/wot-td-java/dashboard)
[![codecov](https://codecov.io/github/Interactions-HSG/wot-td-java/branch/master/graph/badge.svg?token=PGWQ70IFCI)](https://codecov.io/github/Interactions-HSG/wot-td-java)

WoT-TD-Java is a Java library for the [W3C Web of Things (WoT) Thing Description (TD)](https://www.w3.org/TR/wot-thing-description/).

What you can do with the current version:
- read/write TDs in RDF; this library uses [RDF4J](https://rdf4j.org/) and supports primariy [Turtle](https://www.w3.org/TR/turtle/)
- use property and action affordances with the data schemas defined by the [W3C Recommendation](https://www.w3.org/TR/wot-thing-description/#sec-data-schema-vocabulary-definition)
    - JSON Schema keywords are mapped to IRIs using the [JSON Schema in RDF vocabulary](https://www.w3.org/2019/wot/json-schema)
    - note: not all terms (and not all default values) are currently supported
    - you can use composite data schemas (e.g., arrays of nested objects with semantic annotations)
- create HTTP and CoAP requests from a given TD, and parse HTTP and CoAP responses based on a given TD

**Table of Contents**
- [Getting Started](#getting-started)
- [Retrieving and Parsing WoT TDs](#reading-tds)
- [Creating and Writing WoT TDs](#creating-and-writing-tds)
- [Executing HTTP Requests](#executing-http-requests)
- [Executing CoAP Requests](#executing-coap-requests)
- [Working with Semantic Payloads](#working-with-semantic-payloads)


## Getting Started

You can easily add WoT-TD-Java to your project with [JitPack](https://jitpack.io/).

### Add the JitPack repository to your build file

Gradle:

```groovy
allprojects {
  repositories {
    ...
    maven { url 'https://jitpack.io' }
  }
}
```

Maven:
```
<repositories>
  <repository>
    <id>jitpack.io</id>
    <url>https://jitpack.io</url>
  </repository>
</repositories>
```

### Add a dependency to WoT-TD-Java

Gradle:
```groovy
implementation 'com.github.Interactions-HSG:wot-td-java:v0.1.2'
```

Maven:
```
<dependency>
  <groupId>com.github.Interactions-HSG</groupId>
  <artifactId>wot-td-java</artifactId>
  <version>v0.1.2</version>
</dependency>
```

## Retrieving and Parsing WoT TDs

To retrieve and parse a TD from a URL:

```java
ThingDescription td = TDGraphReader.readFromURL(TDFormat.RDF_TURTLE, url);
```

Or from a local file:
```java
ThingDescription td = TDGraphReader.readFromFile(TDFormat.RDF_TURTLE, filePath);
```

Or just parse it from a string:
```java
ThingDescription td = TDGraphReader.readFromString(TDFormat.RDF_TURTLE, description);
```


## Creating and Writing WoT TDs

The library provides a fluent API for constructing TDs programmatically. This feature can be useful,
for instance, to expose TDs for resources hosted on origin servers, or to expose TDs via intermediaries
in order to integrate legacy devices.

We can construct a TD for a lamp as follows:

```java
ThingDescription td = (new ThingDescription.Builder("My Lamp Thing"))
    .addThingURI("http://example.org/lamp123")
    .addSemanticType("https://saref.etsi.org/core/LightSwitch")
    .addAction(toggle)
    .build();
```

The above code snippet creates a `ThingDescription` for a lamp with the title `My Lamp Thing` ([mandatory property](https://www.w3.org/TR/wot-thing-description/#thing)) and the semantic type `saref:LightSwitch` (see [SAREF ontology](https://saref.etsi.org/)). The lamp exposes a `toggle` action, which can be defined in a similar manner:

```java
ActionAffordance toggle = new ActionAffordance.Builder("toggle", toggleForm)
    .addTitle("Toggle")
    .addSemanticType("https://saref.etsi.org/core/ToggleCommand")
    .addComment("This action changes the state of my lamp.")
    .addInputSchema(new ObjectSchema.Builder()
        .addSemanticType("https://saref.etsi.org/core/OnOffState")
        .addProperty("status", new BooleanSchema.Builder()
            .build())
        .addRequiredProperties("status")
        .build())
    .build();
```

Our `toggle` action has the semantic type `saref:ToggleCommand` and takes as input an [ObjectSchema](https://www.w3.org/TR/wot-thing-description/#objectschema) that represents a `saref:OnOffState`. The object schema requires a `status` property of type [BooleanSchema](https://www.w3.org/TR/wot-thing-description/#booleanschema).

The `toggle` action is exposed via a [Form](https://www.w3.org/TR/wot-thing-description/#form), which
is a type of hypermedia control. To create a form, we have to specify a target URI, the method to be
used, etc.:

```java
Form toggleForm = new Form.Builder("http://mylamp.example.org/toggle")
        .setMethodName("PUT")
        .build();
```

We can serialize our TD in Turtle like so (support for other formats is to be added):

```java
String description = new TDGraphWriter(td)
        .setNamespace("td", "https://www.w3.org/2019/wot/td#")
        .setNamespace("htv", "http://www.w3.org/2011/http#")
        .setNamespace("hctl", "https://www.w3.org/2019/wot/hypermedia#")
        .setNamespace("wotsec", "https://www.w3.org/2019/wot/security#")
        .setNamespace("dct", "http://purl.org/dc/terms/")
        .setNamespace("js", "https://www.w3.org/2019/wot/json-schema#")
        .setNamespace("rdfs", "http://www.w3.org/2000/01/rdf-schema#")
        .setNamespace("saref", "https://saref.etsi.org/core/")
        .write();
```

The generated TD is:

```turtle
@prefix td: <https://www.w3.org/2019/wot/td#> .
@prefix htv: <http://www.w3.org/2011/http#> .
@prefix hctl: <https://www.w3.org/2019/wot/hypermedia#> .
@prefix wotsec: <https://www.w3.org/2019/wot/security#> .
@prefix dct: <http://purl.org/dc/terms/> .
@prefix js: <https://www.w3.org/2019/wot/json-schema#> .
@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> .
@prefix saref: <https://saref.etsi.org/core/> .

<http://example.org/lamp123> a td:Thing, saref:LightSwitch;
  td:title "My Lamp Thing";
  td:hasSecurityConfiguration [ a wotsec:NoSecurityScheme
    ];
  td:hasActionAffordance [ a td:ActionAffordance, saref:ToggleCommand;
      td:name "toggle";
      td:title "Toggle";
      rdfs:comment "This action changes the state of my lamp.";
      td:hasForm [
          htv:methodName "PUT";
          hctl:hasTarget <http://mylamp.example.org/toggle>;
          hctl:forContentType "application/json";
          hctl:hasOperationType td:invokeAction
        ];
      td:hasInputSchema [ a js:ObjectSchema, saref:OnOffState;
          js:properties [ a js:BooleanSchema;
              js:propertyName "status"
            ];
          js:required "status"
        ]
    ] .
```

In the above listing, notice the `TDGraphWriter` added for us a number of default values specified by the W3C WoT TD recommendation (e.g., `application/json` for our form's content type).

## Executing HTTP Requests

WoT-TD-Java comes with a built-in HTTP client that can be used to execute HTTP requests based on a given TD.
This feature is currently supported for property and action affordances.

First, we need to retrieve an affordance from the TD. For instance, we can retrieve an action affordance
based on a semantic type:

```java
String affordanceType = "https://saref.etsi.org/core/ToggleCommand";
Optional<ActionAffordance> action = td.getFirstActionBySemanticType(affordanceType);
```

We can also retrieve an action by name:

```java
Optional<ActionAffordance> action = td.getActionByName("toggle");
```

Or we can retrive the list of all available actions:

```java
List<ActionAffordance> actions = td.getActions();
```

Retrieving property affordances works in a similar manner.

Once an affordance is found, we then need to retrieve a form for exploiting the affordance. We can retrieve
the first available form like so:

```java
if (affordanceOpt.isPresent()) {
  Optional<Form> form = action.get().getFirstForm();

  (...)
}
```

Or we can retrieve a form for a given [operation type](https://www.w3.org/TR/wot-thing-description/#form)
as defined by the W3C WoT TD Recommendation:

```java
Optional<Form> form = action.get().getFirstFormForOperationType(TD.invokeAction);
```

If a form is found, we can use it to create and execute an HTTP request for a given operation type:

```java
if (form.isPresent()) {
  TDHttpRequest request = new TDHttpRequest(form.get(),TD.invokeAction);
  TDHttpResponse response = request.execute();

  System.out.println("Received response with status code: " + response.getStatusCode());
}
```

In our above TD for a lamp, the `toggle` action affordance does not use a payload. To construct
requests with payloads, see [Working with Semantic Payloads](#working-with-semantic-payloads).

### Authenticating HTTP requests

At the moment, WoT-TD-Java only supports the `APIKeySecurityScheme` (see [Security Vocabulary Definitions](https://www.w3.org/TR/wot-thing-description/#sec-security-vocabulary-definition)).

To set an API key on a request, we first need to retrieve the API key security scheme from the TD.
This step is necessary because the security scheme will typically contain information specific to the
interface described by the TD (e.g., a specific HTTP header field).

```java
Optional<SecurityScheme> securityScheme = td.getFirstSecuritySchemeByType(WoTSec.APIKeySecurityScheme);
```

If the security scheme is found, we can use it to set the security token:

```java
if (securityScheme.isPresent()) {
  request.setAPIKey((APIKeySecurityScheme) securityScheme.get(), token);
}
```

WoT-TD-Java will use the security scheme to add the security token in the right place (e.g., using
the correct HTTP header field).

## Executing CoAP Requests

Executing CoAP requests is similar to executing HTTP requests. First we need to retrieve a form for
a given affordance in a TD, and then we can create and execute a CoAP request.

For instance:

```java
Optional<PropertyAffordance> humidity = td.getPropertyByName("humidity");

if (humidity.isPresent()) {
  Optional<Form> form = humidity.get().getFirstFormForOperationType(TD.readProperty);

  if(form.isPresent()) {
    TDCoapRequest request = new TDCoapRequest(form.get(),TD.readProperty);
    TDCoapResponse response=request.execute();

    System.out.println("Received response with status code (raw code in decimal): " + response.getStatusCode());
    System.out.println("Received response with status code (code name): " + response.getStatusCodeName());
  }
}
```

Similar to HTTP requests and responses, CoAP requests and responses can also carry payloads.

Authentication is not currently supported for CoAP requests.

## Working with Semantic Payloads

WoT-TD-Java provides support for adding payloads to HTTP and CoAP requests, and for extracting payloads
from HTTP and CoAP responses. Furthermore, if the payloads carry semantic annotations, we can work with
semantic data to decouple from interface-specific details (e.g., JSON schemas).

WoT-TD-Java implements all [Data Schema Vocabulary Definitions](https://www.w3.org/TR/wot-thing-description/#sec-data-schema-vocabulary-definition)
specified in the W3C WoT TD Recommendation.

### Adding payloads to requests

A payload can be an object (`ObjectSchema`), an array (`ArraySchema`), a
primitive data type (`BooleanSchema`, `NumberSchema`, `IntegerSchema`, `StringSchema`), or null (`NullSchema`).

For instance, a device may provide an action affordance to register its user, where the action
affordance is specified by the device's TD as follows:

```turtle
[ a td:ActionAffordance, onto:LogIn;
  td:name "logIn";
  td:title "Log In";
  td:hasForm [
    htv:methodName "POST";
    hctl:hasTarget <https://api.interactions.ics.unisg.ch/xarm/user>;
    hctl:forContentType "application/json";
    hctl:hasOperationType td:invokeAction
  ];
  td:hasInputSchema [
    a js:ObjectSchema, foaf:Agent;
    js:properties
      [ a js:StringSchema, foaf:Name;
        js:propertyName "name"
      ],
      [ a js:StringSchema, foaf:Mbox;
        js:propertyName "email"
      ];
    js:required "name", "email"
  ]
] ;
```

This action affordance has an input schema of type `js:ObjectSchema`. The object contains two properties
(`name` and `email`) to identify the user. Each of these properties carries two semantic annotations:
- `js:StringSchema`, which reflects the primitive data types of these properties;
- annotations using the [FOAF vocabulary](http://xmlns.com/foaf/spec/) (`foaf:Name` and `foaf:Mbox`, respectively).

Such semantic descriptions allow us to program clients against semantic models rather than interface-specific
data formats. We can use the semantic annotations to tell WoT-TD-Java how to construct a payload that follows
the specified schema. To do so, we will need to create a map between the annotations and our data.

```java
Map<String, Object> payload = new HashMap<>();
payload.put("http://xmlns.com/foaf/0.1/Name", "Andrei Ciortea");
payload.put("http://xmlns.com/foaf/0.1/Mbox", "andrei.ciortea@unisg.ch");
```

Note here that:
- we need to use the fully qualified URIs for the semantic annotations
- we are using the FOAF annotations, which allow WoT-TD-Java to discriminate between the two properties; if instead
we would use `js:StringSchema`, we would have no guarantee on the order in which the values are inserted
  into the constructed payload

After we construct our payload, we need to set the payload on the request with the data schema definition
from the TD:

 ```java
// Retrieve the input data schema from the action affordance
Optional<DataSchema> inputSchema = action.getInputSchema();

if (inputSchema.isPresent()){
  request.setObjectPayload((ObjectSchema) inputSchema.get(), payload);
}
```

WoT-TD-Java will use the input data schema definition and the provided data to construct the request payload.

Setting payloads with other data schema definitions and/or setting payloads for CoAP requests works
in a similar manner.

### Extracting payloads from responses

We can extract payloads from responses similar to how we set payloads for requests.

Given the following property affordance for reading a humidity value using a CoAP device:

```turtle
[ a td:PropertyAffordance, miro:Humidity, js:ObjectSchema;
  td:name "humidity";
  td:title "Humidity";
  td:hasForm [
      cov:methodName "GET";
      hctl:hasTarget <coap://130.82.171.10:5683/humidity>;
      hctl:forContentType "application/json";
      hctl:hasOperationType td:observeProperty;
      hctl:forSubProtocol cov:observe
    ],
    [
      cov:methodName "GET";
      hctl:hasTarget <coap://130.82.171.10:5683/humidity>;
      hctl:forContentType "application/json";
      hctl:hasOperationType td:readProperty
    ];
    td:isObservable true;
    js:properties
      [ a js:NumberSchema, miro:HumidityValue;
        js:propertyName "value";
        js:minimum 1.5E1;
        js:maximum 4.0E1
      ]
] ;
```

We first extract a form for reading the property:

```java
Optional<Form> form = humidity.getFirstFormForOperationType(TD.readProperty);
```

We then use the form to execute a CoAP request:

```java
if (form.isPresent()) {
  TDCoapRequest request=new TDCoapRequest(form.get(),TD.readProperty);
  TDCoapResponse response = request.execute();

  (...)
}
```

After receiving the CoAP response, we can extract the `ObjectSchema` payload as follows:

```java
Map<String, Object> payload = response.getPayloadAsObject((ObjectSchema) humidity.getDataSchema());
payload.get("https://interactions.ics.unisg.ch/mirogate#HumidityValue");
```

The values for primitive data types are wrapped in equivalent Java objects. In this example, the humidity
value is retrieved as a `Double`.
