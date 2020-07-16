# WoT-TD-Java

[![Project Status: WIP – Initial development is in progress, but there has not yet been a stable, usable release suitable for the public.](https://www.repostatus.org/badges/latest/wip.svg)](https://www.repostatus.org/#wip)
![build](https://github.com/Interactions-HSG/wot-td-java/workflows/build/badge.svg)
[![](https://www.code-inspector.com/project/8927/score/svg)](https://frontend.code-inspector.com/public/project/8927/wot-td-java/dashboard)
[![codecov](https://codecov.io/gh/Interactions-HSG/wot-td-java/branch/dev/graph/badge.svg)](https://codecov.io/gh/Interactions-HSG/wot-td-java)

A Java library for the [W3C Web of Things (WoT) Thing Description (TD)](https://www.w3.org/TR/wot-thing-description/).

This library is a work-in-progress. What you can do with the current version:
- read/write TDs in RDF; the current version works with [Turtle](https://www.w3.org/TR/turtle/)
- use property and action affordances with the data schemas defined by the [W3C Recommendation](https://www.w3.org/TR/wot-thing-description/#sec-data-schema-vocabulary-definition)
    - JSON Schema keywords are mapped to IRIs using the [JSON Schema in RDF vocabulary](https://www.w3.org/2019/wot/json-schema)
    - not all terms (and not all default values) are currently supported
- use composite data schemas (e.g., arrays of nested objects with semantic annotations) 

Coming soon:
- an HTTP client able to compose HTTP requests based on TDs
- support for event affordances
- support for all the terms defined for data schemas (and default values)

## Prerequisites
* Java 8

## Reading TDs

We can parse a TD from a string like so: 

```java
ThingDescription td = TDGraphReader.readFromString(description);
```

Or from a URL:

```java
ThingDescription td = TDGraphReader.readFromURL(url);
```

## Creating and Writing TDs

The library provides a fluent API for constructing TDs. This feature can be useful, for instance, to expose TDs for resources hosted on origin servers, or to expose TDs via intermediaries in order to integrate legacy devices.

For instance, we can construct a TD for a lamp as follows:

```java
ThingDescription td = (new ThingDescription.Builder("My Lamp Thing"))
    .addThingURI("http://example.org/lamp123")
    .addSemanticType("https://saref.etsi.org/core/LightSwitch")
    .addAction(toggle)
    .build();
```

The above code snippet creates a `ThingDescription` for a lamp with the title `My Lamp Thing` ([mandatory property](https://www.w3.org/TR/wot-thing-description/#thing)) and the semantic type `saref:LightSwitch` (see [SAREF ontology](https://saref.etsi.org/)). The lamp exposes a `toggle` action, which can be defined in a similar manner:

```java
ActionAffordance toggle = new ActionAffordance.Builder(toggleForm)
    .addTitle("Toggle")
    .addSemanticType("https://saref.etsi.org/core/ToggleCommand")
    .addInputSchema(new ObjectSchema.Builder()
        .addSemanticType("https://saref.etsi.org/core/OnOffState")
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
String description = new TDGraphWriter(td)
        .setNamespace("td", "https://www.w3.org/2019/wot/td#")
        .setNamespace("htv", "http://www.w3.org/2011/http#")
        .setNamespace("hctl", "https://www.w3.org/2019/wot/hypermedia#")
        .setNamespace("wotsec", "https://www.w3.org/2019/wot/security#")
        .setNamespace("dct", "http://purl.org/dc/terms/")
        .setNamespace("js", "https://www.w3.org/2019/wot/json-schema#")
        .setNamespace("saref", "https://saref.etsi.org/core/")
        .write();
```

The generated TD is:

```
@prefix td: <https://www.w3.org/2019/wot/td#> .
@prefix htv: <http://www.w3.org/2011/http#> .
@prefix hctl: <https://www.w3.org/2019/wot/hypermedia#> .
@prefix wotsec: <https://www.w3.org/2019/wot/security#> .
@prefix js: <https://www.w3.org/2019/wot/json-schema#> .
@prefix xsd: <http://www.w3.org/2001/XMLSchema#> .
@prefix dct: <http://purl.org/dc/terms/> .
@prefix saref: <https://saref.etsi.org/core/> .

<http://example.org/lamp123> a td:Thing, saref:LightSwitch;
  dct:title "My Lamp Thing";
  td:hasSecurityConfiguration [ a wotsec:NoSecurityScheme ];
  td:hasActionAffordance [ a td:ActionAffordance, saref:ToggleCommand;
      dct:title "Toggle";
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
        ];
    ].
```

In the above listing, notice the `TDGraphWriter` added for us a number of default values specified by the W3C WoT TD recommendation (e.g., `application/json` for our form's content type). 

