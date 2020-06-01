# WoT-TD-Java

[![Project Status: WIP – Initial development is in progress, but there has not yet been a stable, usable release suitable for the public.](https://www.repostatus.org/badges/latest/wip.svg)](https://www.repostatus.org/#wip)
![build](https://github.com/Interactions-HSG/wot-td-java/workflows/build/badge.svg)
[![codecov](https://codecov.io/gh/Interactions-HSG/wot-td-java/branch/dev/graph/badge.svg)](https://codecov.io/gh/Interactions-HSG/wot-td-java)

A Java library for the [W3C Web of Things (WoT) Thing Description (TD)](https://www.w3.org/TR/wot-thing-description/).

This library is a work-in-progress. What you can do with the current version:
- read/write TDs in RDF; the current version works with [Turtle](https://www.w3.org/TR/turtle/)
- use action affordances with the data schemas defined by the [W3C Recommendation](https://www.w3.org/TR/wot-thing-description/#sec-data-schema-vocabulary-definition)
    - JSON Schema keywords are mapped to IRIs using the [JSON Schema in RDF vocabulary](https://www.w3.org/2019/wot/json-schema)
    - not all terms (and not all default values) are currently supported
- use composite data schemas (e.g., arrays of nested objects with semantic annotations) 

Coming soon:
- an HTTP client able to compose HTTP requests based on TDs
- support for *property affordances* and *event affordances*
- support for all the terms defined for data schemas (and default values)


## Prerequisites
* Java 8

## Creating and Writing TDs

The library provides a fluent API for constructing TDs. For instance, we can construct a TD for a lamp as follows:

```java
ThingDescription td = (new ThingDescription.Builder("My Lamp Thing"))
    .addThingURI("http://example.org/lamp123")
    .addSemanticType("https://w3id.org/saref#LightSwitch")
    .addAction(toggle)
    .build();
```

The above code snippet creates a `ThingDescription` for a lamp with the title `My Lamp Thing` ([mandatory property](https://www.w3.org/TR/wot-thing-description/#thing)) and the semantic type `saref:LightSwitch` (see [SAREF ontology](https://sites.google.com/site/smartappliancesproject/ontologies/reference-ontology)). The lamp exposes a `toggle` action, which can be defined in a similar manner:

```java
ActionAffordance toggle = new ActionAffordance.Builder(toggleForm)
    .addTitle("Toggle")
    .addSemanticType("https://w3id.org/saref#ToggleCommand")
    .addInputSchema(new ObjectSchema.Builder()
        .addSemanticType("https://w3id.org/saref#OnOffState")
        .addProperty("status", new BooleanSchema.Builder()
            .build())
        .addRequiredProperties("status")
        .build())
    .build();
```

Our `toggle` action has the semantic type `saref:ToggleCommand` and takes as input an [ObjectSchema](https://www.w3.org/TR/wot-thing-description/#objectschema) that represents a `saref:OnOffState`. The object schema requires a `status` property of type [BooleanSchema](https://www.w3.org/TR/wot-thing-description/#booleanschema).

The `toggle` action is exposed via a [Form](https://www.w3.org/TR/wot-thing-description/#form), which is a type of hypermedia control. To create a form, we have to specify at least the method to be used and a target URI:

```java
Form toggleForm = new Form("PUT", "http://mylamp.example.org/toggle");
```

We can serialize our TD in Turtle like so (support for other formats is to be added): 

```java
String description = TDGraphWriter.write(td);
```

The generated TD is:

```
@prefix htv: <http://www.w3.org/2011/http#> .
@prefix js: <https://www.w3.org/2019/wot/json-schema#> .
@prefix saref: <https://w3id.org/saref#> .
@prefix td: <http://www.w3.org/ns/td#> .
@prefix xsd: <http://www.w3.org/2001/XMLSchema#> .

<http://example.org/lamp123> a td:Thing, saref:LightSwitch;
  td:security "nosec_sc";
  td:title "My Lamp Thing" ;
  td:interaction [ a td:ActionAffordance, saref:ToggleCommand;
      td:title "Toggle";
      td:form [
          htv:methodName "PUT";
          td:contentType "application/json";
          td:href <http://mylamp.example.org/toggle>;
          td:op "invokeaction"
        ];
      td:input [ a saref:OnOffState, js:ObjectSchema;
          js:properties [ a js:BooleanSchema;
              js:propertyName "status"
            ];
          js:required "status"
        ];
    ].
```

In the above listing, notice the `TDGraphWriter` added for us a number of default values specified by the W3C WoT TD recommendation (e.g., `application/json` for our form's content type). 

## Parsing TDs

We can parse a TD from a string like so: 

```java
ThingDescription td = TDGraphReader.readFromString(description);
```