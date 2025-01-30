package ch.unisg.ics.interactions.wot.td.io;

import ch.unisg.ics.interactions.wot.td.ThingDescription;
import ch.unisg.ics.interactions.wot.td.ThingDescription.TDFormat;
import ch.unisg.ics.interactions.wot.td.affordances.ActionAffordance;
import ch.unisg.ics.interactions.wot.td.affordances.EventAffordance;
import ch.unisg.ics.interactions.wot.td.affordances.Form;
import ch.unisg.ics.interactions.wot.td.affordances.PropertyAffordance;
import ch.unisg.ics.interactions.wot.td.schemas.DataSchema;
import ch.unisg.ics.interactions.wot.td.schemas.IntegerSchema;
import ch.unisg.ics.interactions.wot.td.schemas.NumberSchema;
import ch.unisg.ics.interactions.wot.td.schemas.ObjectSchema;
import ch.unisg.ics.interactions.wot.td.security.*;
import ch.unisg.ics.interactions.wot.td.security.TokenBasedSecurityScheme.TokenLocation;
import ch.unisg.ics.interactions.wot.td.security.SecurityScheme;
import ch.unisg.ics.interactions.wot.td.vocabularies.COV;
import ch.unisg.ics.interactions.wot.td.vocabularies.TD;
import ch.unisg.ics.interactions.wot.td.vocabularies.WoTSec;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.junit.Test;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

public class TDGraphReaderTest {

  private static final String PREFIXES =
    "@prefix td: <https://www.w3.org/2019/wot/td#> .\n" +
      "@prefix htv: <http://www.w3.org/2011/http#> .\n" +
      "@prefix cov: <http://www.example.org/coap-binding#> .\n" +
      "@prefix hctl: <https://www.w3.org/2019/wot/hypermedia#> .\n" +
      "@prefix dct: <http://purl.org/dc/terms/> .\n" +
      "@prefix wotsec: <https://www.w3.org/2019/wot/security#> .\n" +
      "@prefix js: <https://www.w3.org/2019/wot/json-schema#> .\n" +
      "@prefix saref: <https://saref.etsi.org/core/> .\n" +
      "@prefix ex: <https://example.org#> .\n" +
      "@prefix xsd: <http://www.w3.org/2001/XMLSchema#> .\n";

  private static final String TEST_SIMPLE_TD =
    "@prefix td: <https://www.w3.org/2019/wot/td#> .\n" +
      "@prefix htv: <http://www.w3.org/2011/http#> .\n" +
      "@prefix hctl: <https://www.w3.org/2019/wot/hypermedia#> .\n" +
      "@prefix dct: <http://purl.org/dc/terms/> .\n" +
      "@prefix wotsec: <https://www.w3.org/2019/wot/security#> .\n" +
      "@prefix js: <https://www.w3.org/2019/wot/json-schema#> .\n" +
      "\n" +
      "<http://example.org/#thing> a td:Thing ;\n" +
      "    td:title \"My Thing\" ;\n" +
      "    td:hasSecurityConfiguration [ a wotsec:NoSecurityScheme ] ;\n" +
      "    td:baseURI <http://example.org/> ;\n" +
      "    td:hasPropertyAffordance [\n" +
      "        a td:PropertyAffordance, js:NumberSchema ;\n" +
      "        td:name \"my_property\" ;\n" +
      "        td:title \"My Property\" ;\n" +
      "        td:isObservable true ;\n" +
      "        td:hasForm [\n" +
      "            htv:methodName \"PUT\" ;\n" +
      "            hctl:hasTarget <http://example.org/property> ;\n" +
      "            hctl:forContentType \"application/json\";\n" +
      "            hctl:hasOperationType td:writeProperty;\n" +
      "        ] ;\n" +
      "        td:hasForm [\n" +
      "            htv:methodName \"GET\" ;\n" +
      "            hctl:hasTarget <http://example.org/property> ;\n" +
      "            hctl:forContentType \"application/json\";\n" +
      "            hctl:hasOperationType td:readProperty;\n" +
      "            hctl:forSubProtocol \"websub\";\n" +
      "        ] ;\n" +
      "    ] ;\n" +
      "    td:hasActionAffordance [\n" +
      "        a td:ActionAffordance ;\n" +
      "        td:name \"my_action\" ;\n" +
      "        td:title \"My Action\" ;\n" +
      "        td:hasForm [\n" +
      "            htv:methodName \"PUT\" ;\n" +
      "            hctl:hasTarget <http://example.org/action> ;\n" +
      "            hctl:forContentType \"application/json\";\n" +
      "            hctl:hasOperationType td:invokeAction;\n" +
      "        ] ;\n" +
      "        td:hasInputSchema [\n" +
      "            a js:ObjectSchema ;\n" +
      "            js:properties [\n" +
      "                a js:NumberSchema ;\n" +
      "                js:propertyName \"number_value\";\n" +
      "                js:maximum 100.05 ;\n" +
      "                js:minimum -100.05 ;\n" +
      "            ] ;\n" +
      "            js:required \"number_value\" ;\n" +
      "        ] ;\n" +
      "        td:hasOutputSchema [\n" +
      "            a js:ObjectSchema ;\n" +
      "            js:properties [\n" +
      "                a js:BooleanSchema ;\n" +
      "                js:propertyName \"boolean_value\";\n" +
      "            ] ;\n" +
      "            js:required \"boolean_value\" ;\n" +
      "        ]\n" +
      "    ] ;\n" +
      "    td:hasEventAffordance [\n" +
      "        a td:EventAffordance ;\n" +
      "        td:name \"my_event\" ;\n" +
      "        td:title \"My Event\" ;\n" +
      "        td:hasForm [\n" +
      "            htv:methodName \"PUT\" ;\n" +
      "            hctl:hasTarget <http://example.org/event> ;\n" +
      "            hctl:forContentType \"application/json\";\n" +
      "            hctl:hasOperationType td:subscribeEvent, td:unsubscribeEvent;\n" +
      "        ] ;\n" +
      "        td:hasSubscriptionSchema [\n" +
      "            a js:ObjectSchema ;\n" +
      "            js:properties [\n" +
      "                a js:StringSchema ;\n" +
      "                js:propertyName \"string_value\";\n" +
      "            ] ;\n" +
      "            js:required \"string_value\" ;\n" +
      "        ] ;\n" +
      "        td:hasNotificationSchema [\n" +
      "            a js:ObjectSchema ;\n" +
      "            js:properties [\n" +
      "                a js:IntegerSchema ;\n" +
      "                js:propertyName \"integer_value\";\n" +
      "            ] ;\n" +
      "            js:required \"integer_value\" ;\n" +
      "        ] ;\n" +
      "        td:hasCancellationSchema [\n" +
      "            a js:ObjectSchema ;\n" +
      "            js:properties [\n" +
      "                a js:BooleanSchema ;\n" +
      "                js:propertyName \"boolean_value\";\n" +
      "            ] ;\n" +
      "            js:required \"boolean_value\" ;\n" +
      "        ]\n" +
      "    ] ." ;

  private static final String TEST_SIMPLE_TD_JSONLD = "[ {\n" +
      "  \"@id\" : \"_:node1ea75dfphx111\",\n" +
      "  \"@type\" : [ \"https://www.w3.org/2019/wot/security#NoSecurityScheme\" ]\n" +
      "}, {\n" +
      "  \"@id\" : \"_:node1ea75dfphx112\",\n" +
      "  \"@type\" : [ \"https://www.w3.org/2019/wot/td#ActionAffordance\" ],\n" +
      "  \"https://www.w3.org/2019/wot/td#title\" : [ {\n" +
      "    \"@value\" : \"My Action\"\n" +
      "  } ],\n" +
      "  \"https://www.w3.org/2019/wot/td#hasForm\" : [ {\n" +
      "    \"@id\" : \"_:node1ea75dfphx113\"\n" +
      "  } ],\n" +
      "  \"https://www.w3.org/2019/wot/td#hasInputSchema\" : [ {\n" +
      "    \"@id\" : \"_:node1ea75dfphx114\"\n" +
      "  } ],\n" +
      "  \"https://www.w3.org/2019/wot/td#hasOutputSchema\" : [ {\n" +
      "    \"@id\" : \"_:node1ea75dfphx116\"\n" +
      "  } ]\n" +
      "}, {\n" +
      "  \"@id\" : \"_:node1ea75dfphx113\",\n" +
      "  \"http://www.w3.org/2011/http#methodName\" : [ {\n" +
      "    \"@value\" : \"PUT\"\n" +
      "  } ],\n" +
      "  \"https://www.w3.org/2019/wot/hypermedia#forContentType\" : [ {\n" +
      "    \"@value\" : \"application/json\"\n" +
      "  } ],\n" +
      "  \"https://www.w3.org/2019/wot/hypermedia#hasOperationType\" : [ {\n" +
      "    \"@id\" : \"https://www.w3.org/2019/wot/td#invokeAction\"\n" +
      "  } ],\n" +
      "  \"https://www.w3.org/2019/wot/hypermedia#hasTarget\" : [ {\n" +
      "    \"@id\" : \"http://example.org/action/\"\n" +
      "  } ]\n" +
      "}, {\n" +
      "  \"@id\" : \"_:node1ea75dfphx114\",\n" +
      "  \"@type\" : [ \"https://www.w3.org/2019/wot/json-schema#ObjectSchema\" ],\n" +
      "  \"https://www.w3.org/2019/wot/json-schema#properties\" : [ {\n" +
      "    \"@id\" : \"_:node1ea75dfphx115\"\n" +
      "  } ],\n" +
      "  \"https://www.w3.org/2019/wot/json-schema#required\" : [ {\n" +
      "    \"@value\" : \"number_value\"\n" +
      "  } ]\n" +
      "}, {\n" +
      "  \"@id\" : \"_:node1ea75dfphx115\",\n" +
      "  \"@type\" : [ \"https://www.w3.org/2019/wot/json-schema#NumberSchema\" ],\n" +
      "  \"https://www.w3.org/2019/wot/json-schema#maximum\" : [ {\n" +
      "    \"@type\" : \"http://www.w3.org/2001/XMLSchema#decimal\",\n" +
      "    \"@value\" : \"100.05\"\n" +
      "  } ],\n" +
      "  \"https://www.w3.org/2019/wot/json-schema#minimum\" : [ {\n" +
      "    \"@type\" : \"http://www.w3.org/2001/XMLSchema#decimal\",\n" +
      "    \"@value\" : \"-100.05\"\n" +
      "  } ],\n" +
      "  \"https://www.w3.org/2019/wot/json-schema#propertyName\" : [ {\n" +
      "    \"@value\" : \"number_value\"\n" +
      "  } ]\n" +
      "}, {\n" +
      "  \"@id\" : \"_:node1ea75dfphx116\",\n" +
      "  \"@type\" : [ \"https://www.w3.org/2019/wot/json-schema#ObjectSchema\" ],\n" +
      "  \"https://www.w3.org/2019/wot/json-schema#properties\" : [ {\n" +
      "    \"@id\" : \"_:node1ea75dfphx117\"\n" +
      "  } ],\n" +
      "  \"https://www.w3.org/2019/wot/json-schema#required\" : [ {\n" +
      "    \"@value\" : \"boolean_value\"\n" +
      "  } ]\n" +
      "}, {\n" +
      "  \"@id\" : \"_:node1ea75dfphx117\",\n" +
      "  \"@type\" : [ \"https://www.w3.org/2019/wot/json-schema#BooleanSchema\" ],\n" +
      "  \"https://www.w3.org/2019/wot/json-schema#propertyName\" : [ {\n" +
      "    \"@value\" : \"boolean_value\"\n" +
      "  } ]\n" +
      "}, {\n" +
      "  \"@id\" : \"http://example.org/#thing\",\n" +
      "  \"@type\" : [ \"https://www.w3.org/2019/wot/td#Thing\" ],\n" +
      "  \"https://www.w3.org/2019/wot/td#title\" : [ {\n" +
      "    \"@value\" : \"My Thing\"\n" +
      "  } ],\n" +
      "  \"https://www.w3.org/2019/wot/td#hasActionAffordance\" : [ {\n" +
      "    \"@id\" : \"_:node1ea75dfphx112\"\n" +
      "  } ],\n" +
      "  \"https://www.w3.org/2019/wot/td#baseURI\" : [ {\n" +
      "    \"@id\" : \"http://example.org/\"\n" +
      "  } ],\n" +
      "  \"https://www.w3.org/2019/wot/td#hasSecurityConfiguration\" : [ {\n" +
      "    \"@id\" : \"_:node1ea75dfphx111\"\n" +
      "  } ]\n" +
      "} ]";

  private static final String TEST_IO_HEAD =
    "@prefix td: <https://www.w3.org/2019/wot/td#> .\n" +
      "@prefix htv: <http://www.w3.org/2011/http#> .\n" +
      "@prefix hctl: <https://www.w3.org/2019/wot/hypermedia#> .\n" +
      "@prefix dct: <http://purl.org/dc/terms/> .\n" +
      "@prefix wotsec: <https://www.w3.org/2019/wot/security#> .\n" +
      "@prefix js: <https://www.w3.org/2019/wot/json-schema#> .\n" +
      "\n" +
      "<http://example.org/#thing> a td:Thing ;\n" +
      "    td:title \"My Thing\" ;\n" +
      "    td:hasSecurityConfiguration [ a wotsec:NoSecurityScheme ] ;\n" +
      "    td:baseURI <http://example.org/> ;\n" +
      "    td:hasActionAffordance [\n" +
      "        a td:ActionAffordance ;\n" +
      "        td:name \"my_action\" ;\n" +
      "        td:title \"My Action\" ;\n" +
      "        td:hasForm [\n" +
      "            htv:methodName \"PUT\" ;\n" +
      "            hctl:hasTarget <http://example.org/action> ;\n" +
      "            hctl:forContentType \"application/json\";\n" +
      "            hctl:hasOperationType td:invokeAction;\n" +
      "        ] ;\n";

  private static final String TEST_IO_TAIL = "    ] .";

  @Test
  public void testReadTitle() {
    TDGraphReader reader = new TDGraphReader(RDFFormat.JSONLD, TEST_SIMPLE_TD_JSONLD);
    assertEquals("My Thing", reader.readThingTitle());
  }

  @Test
  public void testReadThingTypes() {
    TDGraphReader reader = new TDGraphReader(RDFFormat.TURTLE, TEST_SIMPLE_TD);

    assertEquals(1, reader.readThingTypes().size());
    assertTrue(reader.readThingTypes().contains(TD.Thing));
  }

  @Test
  public void testReadBaseURI() {
    TDGraphReader reader = new TDGraphReader(RDFFormat.TURTLE, TEST_SIMPLE_TD);

    assertEquals("http://example.org/", reader.readBaseURI().get());
  }

  //Test security schemes
  @Test
  public void testReadOneSecurityScheme() {
    TDGraphReader reader = new TDGraphReader(RDFFormat.TURTLE, TEST_SIMPLE_TD);

    Map<String, SecurityScheme> schemes = reader.readSecuritySchemes();
    assertEquals(1, schemes.size());

    List<String> nosecSchemes = getSecurityNamesforSchemeName(SecurityScheme.NOSEC, schemes);
    assertEquals(1, nosecSchemes.size());

    assertTrue(reader.readSecuritySchemes().values().stream().anyMatch(scheme ->
      scheme.getSemanticTypes().contains(WoTSec.NoSecurityScheme)));
  }


  @Test
  public void testReadAPIKeySecurityScheme() {
    String testTD = PREFIXES +
      "\n" +
      "<http://example.org/#thing> a td:Thing ;\n" +
      "    dct:title \"My Thing\" ;\n" +
      "    td:hasSecurityConfiguration [ a wotsec:APIKeySecurityScheme, ex:Type ;\n" +
      "        wotsec:in \"header\" ;\n" +
      "        wotsec:name \"X-API-Key\" ;\n" +
      "  ] .";

    TDGraphReader reader = new TDGraphReader(RDFFormat.TURTLE, testTD);

    Map<String, SecurityScheme> schemes = reader.readSecuritySchemes();
    assertEquals(1, schemes.size());

    List<String> apikeySchemes = getSecurityNamesforSchemeName(SecurityScheme.APIKEY, schemes);
    assertEquals(1, apikeySchemes.size());

    SecurityScheme scheme = schemes.get(apikeySchemes.get(0));
    assertTrue(scheme instanceof APIKeySecurityScheme);
    assertEquals(2, scheme.getSemanticTypes().size());
    assertTrue(scheme.getSemanticTypes().contains(WoTSec.APIKeySecurityScheme));
    assertTrue(scheme.getSemanticTypes().contains("https://example.org#Type"));
    assertEquals(TokenLocation.HEADER,
      ((APIKeySecurityScheme) scheme).getTokenLocation());
    assertTrue(((APIKeySecurityScheme) scheme).getTokenName().isPresent());
    assertEquals("X-API-Key", ((APIKeySecurityScheme) scheme).getTokenName().get());
  }

  @Test
  public void testAPIKeySecuritySchemeDefaultValues() {
    String testTD = PREFIXES +
      "\n" +
      "<http://example.org/#thing> a td:Thing ;\n" +
      "    dct:title \"My Thing\" ;\n" +
      "    td:hasSecurityConfiguration [ a wotsec:APIKeySecurityScheme ] .";

    TDGraphReader reader = new TDGraphReader(RDFFormat.TURTLE, testTD);

    Map<String, SecurityScheme> schemes = reader.readSecuritySchemes();
    assertEquals(1, schemes.size());

    List<String> apikeySchemes = getSecurityNamesforSchemeName(SecurityScheme.APIKEY, schemes);
    assertEquals(1, apikeySchemes.size());

    SecurityScheme scheme = schemes.get(apikeySchemes.get(0));
    assertTrue(scheme instanceof APIKeySecurityScheme);
    assertEquals(1, scheme.getSemanticTypes().size());
    assertTrue(scheme.getSemanticTypes().contains(WoTSec.APIKeySecurityScheme));
    assertEquals(TokenLocation.QUERY,
      ((APIKeySecurityScheme) scheme).getTokenLocation());
    assertFalse(((APIKeySecurityScheme) scheme).getTokenName().isPresent());
  }

  @Test
  public void testAPIKeySecuritySchemeInvalidTokenLocation() {
    String testTD = PREFIXES +
      "\n" +
      "<http://example.org/#thing> a td:Thing ;\n" +
      "    dct:title \"My Thing\" ;\n" +
      "    td:hasSecurityConfiguration [ a wotsec:APIKeySecurityScheme ;\n" +
      "        wotsec:in \"bla\" ;\n" +
      "  ] .";

    Exception exception = assertThrows(InvalidTDException.class, () -> {
      new TDGraphReader(RDFFormat.TURTLE, testTD).readSecuritySchemes();
    });
    String expectedMessage = "Invalid security scheme configuration";
    String actualMessage = exception.getMessage();
    assertTrue(actualMessage.contains(expectedMessage));
  }

  @Test
  public void testReadBasicSecurityScheme() {
    String testTD = PREFIXES +
      "\n" +
      "<http://example.org/#thing> a td:Thing ;\n" +
      "    dct:title \"My Thing\" ;\n" +
      "    td:hasSecurityConfiguration [ a wotsec:BasicSecurityScheme, ex:Type ;\n" +
      "        wotsec:in \"header\" ;\n" +
      "        wotsec:name \"Authorization\" ;\n" +
      "  ] .";

    TDGraphReader reader = new TDGraphReader(RDFFormat.TURTLE, testTD);

    Map<String, SecurityScheme> schemes = reader.readSecuritySchemes();
    assertEquals(1, schemes.size());

    List<String> basicSchemes = getSecurityNamesforSchemeName(SecurityScheme.BASIC, schemes);
    assertEquals(1, basicSchemes.size());

    SecurityScheme scheme = schemes.get(basicSchemes.get(0));
    assertTrue(scheme instanceof BasicSecurityScheme);
    assertEquals(2, scheme.getSemanticTypes().size());
    assertTrue(scheme.getSemanticTypes().contains(WoTSec.BasicSecurityScheme));
    assertTrue(scheme.getSemanticTypes().contains("https://example.org#Type"));
    assertEquals(TokenLocation.HEADER,
      ((BasicSecurityScheme) scheme).getTokenLocation());
    assertTrue(((BasicSecurityScheme) scheme).getTokenName().isPresent());
    assertEquals("Authorization", ((BasicSecurityScheme) scheme).getTokenName().get());
  }

  @Test
  public void testBasicSecuritySchemeDefaultValues() {
    String testTD = PREFIXES +
      "\n" +
      "<http://example.org/#thing> a td:Thing ;\n" +
      "    dct:title \"My Thing\" ;\n" +
      "    td:hasSecurityConfiguration [ a wotsec:BasicSecurityScheme ] .";

    TDGraphReader reader = new TDGraphReader(RDFFormat.TURTLE, testTD);

    Map<String, SecurityScheme> schemes = reader.readSecuritySchemes();
    assertEquals(1, schemes.size());

    List<String> basicSchemes = getSecurityNamesforSchemeName(SecurityScheme.BASIC, schemes);
    assertEquals(1, basicSchemes.size());

    SecurityScheme scheme = schemes.get(basicSchemes.get(0));
    assertTrue(scheme instanceof BasicSecurityScheme);
    assertEquals(1, scheme.getSemanticTypes().size());
    assertTrue(scheme.getSemanticTypes().contains(WoTSec.BasicSecurityScheme));
    assertEquals(TokenLocation.HEADER,
      ((BasicSecurityScheme) scheme).getTokenLocation());
    assertFalse(((BasicSecurityScheme) scheme).getTokenName().isPresent());
  }

  @Test
  public void testReadDigestSecurityScheme() {
    String testTD = PREFIXES +
      "\n" +
      "<http://example.org/#thing> a td:Thing ;\n" +
      "    dct:title \"My Thing\" ;\n" +
      "    td:hasSecurityConfiguration [ a wotsec:DigestSecurityScheme, ex:Type ;\n" +
      "        wotsec:in \"header\" ;\n" +
      "        wotsec:name \"nonce\" ;\n" +
      "        wotsec:qop \"auth-int\" ;\n" +
      "    ] .\n";

    TDGraphReader reader = new TDGraphReader(RDFFormat.TURTLE, testTD);

    Map<String, SecurityScheme> schemes = reader.readSecuritySchemes();
    assertEquals(1, schemes.size());

    List<String> digestSchemes = getSecurityNamesforSchemeName(SecurityScheme.DIGEST, schemes);
    assertEquals(1, digestSchemes.size());

    SecurityScheme scheme = schemes.get(digestSchemes.get(0));
    assertTrue(scheme instanceof DigestSecurityScheme);
    assertEquals(2, scheme.getSemanticTypes().size());
    assertTrue(scheme.getSemanticTypes().contains(WoTSec.DigestSecurityScheme));
    assertTrue(scheme.getSemanticTypes().contains("https://example.org#Type"));
    assertEquals(TokenLocation.HEADER,
      ((DigestSecurityScheme) scheme).getTokenLocation());
    assertEquals(DigestSecurityScheme.QualityOfProtection.AUTH_INT,
      ((DigestSecurityScheme) scheme).getQoP());
    assertTrue(((DigestSecurityScheme) scheme).getTokenName().isPresent());
    assertEquals("nonce", ((DigestSecurityScheme) scheme).getTokenName().get());
  }

  @Test
  public void testDigestSecuritySchemeDefaultValues() {
    String testTD = PREFIXES +
      "\n" +
      "<http://example.org/#thing> a td:Thing ;\n" +
      "    dct:title \"My Thing\" ;\n" +
      "    td:hasSecurityConfiguration [ a wotsec:DigestSecurityScheme ] .";

    TDGraphReader reader = new TDGraphReader(RDFFormat.TURTLE, testTD);

    Map<String, SecurityScheme> schemes = reader.readSecuritySchemes();
    assertEquals(1, schemes.size());

    List<String> digestSchemes = getSecurityNamesforSchemeName(SecurityScheme.DIGEST, schemes);
    assertEquals(1, digestSchemes.size());

    SecurityScheme scheme = schemes.get(digestSchemes.get(0));
    assertTrue(scheme instanceof DigestSecurityScheme);
    assertEquals(1, scheme.getSemanticTypes().size());
    assertTrue(scheme.getSemanticTypes().contains(WoTSec.DigestSecurityScheme));
    assertEquals(TokenLocation.HEADER,
      ((DigestSecurityScheme) scheme).getTokenLocation());
    assertEquals(DigestSecurityScheme.QualityOfProtection.AUTH,
      ((DigestSecurityScheme) scheme).getQoP());
    assertFalse(((DigestSecurityScheme) scheme).getTokenName().isPresent());
  }

  @Test
  public void testDigestSecuritySchemeInvalidQualityOfProtection() {
    String testTD = PREFIXES +
      "\n" +
      "<http://example.org/#thing> a td:Thing ;\n" +
      "    dct:title \"My Thing\" ;\n" +
      "    td:hasSecurityConfiguration [ a wotsec:DigestSecurityScheme ;\n" +
      "        wotsec:qop \"bla\" ;\n" +
      "  ] .";

    Exception exception = assertThrows(InvalidTDException.class, () -> {
      new TDGraphReader(RDFFormat.TURTLE, testTD).readSecuritySchemes();
    });
    String expectedMessage = "Invalid security scheme configuration";
    String actualMessage = exception.getMessage();
    assertTrue(actualMessage.contains(expectedMessage));
  }

  @Test
  public void testReadBearerSecurityScheme() {
    String testTD = PREFIXES +
      "\n" +
      "<http://example.org/#thing> a td:Thing ;\n" +
      "    dct:title \"My Thing\" ;\n" +
      "    td:hasSecurityConfiguration [ a wotsec:BearerSecurityScheme, ex:Type ;\n" +
      "        wotsec:in \"header\" ;\n" +
      "        wotsec:name \"Authorization\" ;\n" +
      "        wotsec:authorization <http://server.example.com> ;\n" +
      "        wotsec:alg \"ECDSA 256\" ;\n" +
      "        wotsec:format \"cwt\" ;\n" +
      "    ] .\n";

    TDGraphReader reader = new TDGraphReader(RDFFormat.TURTLE, testTD);

    Map<String, SecurityScheme> schemes = reader.readSecuritySchemes();
    assertEquals(1, schemes.size());

    List<String> bearerSchemes = getSecurityNamesforSchemeName(SecurityScheme.BEARER, schemes);
    assertEquals(1, bearerSchemes.size());

    SecurityScheme scheme = schemes.get(bearerSchemes.get(0));
    assertTrue(scheme instanceof BearerSecurityScheme);
    assertEquals(2, scheme.getSemanticTypes().size());
    assertTrue(scheme.getSemanticTypes().contains(WoTSec.BearerSecurityScheme));
    assertTrue(scheme.getSemanticTypes().contains("https://example.org#Type"));

    BearerSecurityScheme bearerScheme = (BearerSecurityScheme) scheme;
    assertEquals(TokenLocation.HEADER, bearerScheme.getTokenLocation());
    assertEquals("ECDSA 256", bearerScheme.getAlg());
    assertEquals("cwt", bearerScheme.getFormat());
    assertTrue(bearerScheme.getAuthorization().isPresent());
    assertEquals("http://server.example.com", bearerScheme.getAuthorization().get());
    assertTrue(bearerScheme.getTokenName().isPresent());
    assertEquals("Authorization", bearerScheme.getTokenName().get());
  }

  @Test
  public void testBearerSecuritySchemeDefaultValues() {
    String testTD = PREFIXES +
      "\n" +
      "<http://example.org/#thing> a td:Thing ;\n" +
      "    dct:title \"My Thing\" ;\n" +
      "    td:hasSecurityConfiguration [ a wotsec:BearerSecurityScheme ] .";

    TDGraphReader reader = new TDGraphReader(RDFFormat.TURTLE, testTD);

    Map<String, SecurityScheme> schemes = reader.readSecuritySchemes();
    assertEquals(1, schemes.size());

    List<String> digestSchemes = getSecurityNamesforSchemeName(SecurityScheme.BEARER, schemes);
    assertEquals(1, digestSchemes.size());

    SecurityScheme scheme = schemes.get(digestSchemes.get(0));
    assertTrue(scheme instanceof BearerSecurityScheme);
    assertEquals(1, scheme.getSemanticTypes().size());
    assertTrue(scheme.getSemanticTypes().contains(WoTSec.BearerSecurityScheme));


    BearerSecurityScheme bearerScheme = (BearerSecurityScheme) scheme;
    assertEquals(TokenLocation.HEADER, bearerScheme.getTokenLocation());
    assertEquals("ES256", bearerScheme.getAlg());
    assertEquals("jwt", bearerScheme.getFormat());
    assertFalse(bearerScheme.getAuthorization().isPresent());
    assertFalse(bearerScheme.getTokenName().isPresent());
  }

  @Test
  public void testBearerSecuritySchemeInvalidAuth() {
    String testTD =
      "@prefix td: <https://www.w3.org/2019/wot/td#> .\n" +
        "@prefix wotsec: <https://www.w3.org/2019/wot/security#> .\n" +
        "@prefix dct: <http://purl.org/dc/terms/> .\n" +
        "\n" +
        "<http://example.org/#thing> a td:Thing ;\n" +
        "    dct:title \"My Thing\" ;\n" +
        "    td:hasSecurityConfiguration [ a wotsec:BearerSecurityScheme ;\n" +
        "        wotsec:authorization \"invalidIRI\" ;\n" +
        "  ] .";

    TDGraphReader reader = new TDGraphReader(RDFFormat.TURTLE, testTD);
    assertEquals(1, reader.readSecuritySchemes().size());
    SecurityScheme scheme = reader.readSecuritySchemes().values().iterator().next();
    assertTrue(scheme.getSemanticTypes().contains(WoTSec.BearerSecurityScheme));
    assertEquals(TokenLocation.HEADER,
      ((BearerSecurityScheme) scheme).getTokenLocation());
    assertFalse(((BearerSecurityScheme) scheme).getAuthorization().isPresent());
  }

  @Test
  public void testReadPSKSecurityScheme() {
    String testTD = PREFIXES +
      "\n" +
      "<http://example.org/#thing> a td:Thing ;\n" +
      "    dct:title \"My Thing\" ;\n" +
      "    td:hasSecurityConfiguration [ a wotsec:PSKSecurityScheme, ex:Type ;\n" +
      "        wotsec:identity \"192.0.2.1\" ;\n" +
      "  ] .";

    TDGraphReader reader = new TDGraphReader(RDFFormat.TURTLE, testTD);

    Map<String, SecurityScheme> schemes = reader.readSecuritySchemes();
    assertEquals(1, schemes.size());

    List<String> apikeySchemes = getSecurityNamesforSchemeName(SecurityScheme.PSK, schemes);
    assertEquals(1, apikeySchemes.size());

    SecurityScheme scheme = schemes.get(apikeySchemes.get(0));
    assertTrue(scheme instanceof PSKSecurityScheme);
    assertEquals(2, scheme.getSemanticTypes().size());
    assertTrue(scheme.getSemanticTypes().contains(WoTSec.PSKSecurityScheme));
    assertTrue(scheme.getSemanticTypes().contains("https://example.org#Type"));
    assertTrue(((PSKSecurityScheme) scheme).getIdentity().isPresent());
    assertEquals("192.0.2.1", ((PSKSecurityScheme) scheme).getIdentity().get());
  }

  @Test
  public void testReadOAuth2SecurityScheme() {
    String testTD =
      "@prefix td: <https://www.w3.org/2019/wot/td#> .\n" +
        "@prefix wotsec: <https://www.w3.org/2019/wot/security#> .\n" +
        "@prefix dct: <http://purl.org/dc/terms/> .\n" +
        "\n" +
        "<http://example.org/#thing> a td:Thing ;\n" +
        "    dct:title \"My Thing\" ;\n" +
        "    td:hasSecurityConfiguration [ a wotsec:OAuth2SecurityScheme ;\n" +
        "        wotsec:authorization <https://example.com/authorization> ;\n" +
        "        wotsec:token <https://example.com/token/1> ;\n" +
        "        wotsec:refresh <https://example.com/token/2> ;\n" +
        "        wotsec:scopes \"limited\", \"special\", \"c\" ;\n" +
        "        wotsec:flow  \"code\";\n" +
        "  ] .";

    TDGraphReader reader = new TDGraphReader(RDFFormat.TURTLE, testTD);

    assertEquals(1, reader.readSecuritySchemes().size());

    SecurityScheme scheme = reader.readSecuritySchemes().values().iterator().next();
    assertTrue(scheme instanceof OAuth2SecurityScheme);
    assertTrue(scheme.getSemanticTypes().contains(WoTSec.OAuth2SecurityScheme));
    assertEquals(scheme.getSchemeName(), "oauth2");
    assertEquals("https://example.com/authorization", ((OAuth2SecurityScheme) scheme).getAuthorization().get());
    assertEquals("https://example.com/token/1", ((OAuth2SecurityScheme) scheme).getToken().get());
    assertEquals("https://example.com/token/2", ((OAuth2SecurityScheme) scheme).getRefresh().get());
    assertEquals(3, ((OAuth2SecurityScheme) scheme).getScopes().get().size());
    assertTrue(((OAuth2SecurityScheme) scheme).getScopes().get().contains("special"));
    assertTrue(((OAuth2SecurityScheme) scheme).getScopes().get().contains("limited"));
    assertTrue(((OAuth2SecurityScheme) scheme).getScopes().get().contains("c"));
    assertEquals("code", ((OAuth2SecurityScheme) scheme).getFlow());

  }

  @Test
  public void testReadOAuth2SecuritySchemeMissingFlow() {
    String testTD =
      "@prefix td: <https://www.w3.org/2019/wot/td#> .\n" +
        "@prefix wotsec: <https://www.w3.org/2019/wot/security#> .\n" +
        "@prefix dct: <http://purl.org/dc/terms/> .\n" +
        "\n" +
        "<http://example.org/#thing> a td:Thing ;\n" +
        "    dct:title \"My Thing\" ;\n" +
        "    td:hasSecurityConfiguration [ a wotsec:OAuth2SecurityScheme ;\n" +
        "  ] .";


    TDGraphReader reader = new TDGraphReader(RDFFormat.TURTLE, testTD);
    Exception exception = assertThrows(InvalidTDException.class, () -> {
      reader.readSecuritySchemes();
    });

    String expectedMessage = "Missing or invalid configuration value of type " + WoTSec.flow +
      " on defining security scheme";

    String actualMessage = String.valueOf(exception.getCause());
    assertTrue(actualMessage.contains(expectedMessage));
  }

  @Test
  public void testOAuthSecuritySchemeInvalidAuth() {
    String testTD =
      "@prefix td: <https://www.w3.org/2019/wot/td#> .\n" +
        "@prefix wotsec: <https://www.w3.org/2019/wot/security#> .\n" +
        "@prefix dct: <http://purl.org/dc/terms/> .\n" +
        "\n" +
        "<http://example.org/#thing> a td:Thing ;\n" +
        "    dct:title \"My Thing\" ;\n" +
        "    td:hasSecurityConfiguration [ a wotsec:OAuth2SecurityScheme ;\n" +
        "        wotsec:authorization \"invalidIRI\" ;\n" +
        "        wotsec:flow  \"code\";\n" +
        "  ] .";

    TDGraphReader reader = new TDGraphReader(RDFFormat.TURTLE, testTD);
    assertEquals(1, reader.readSecuritySchemes().size());
    SecurityScheme scheme = reader.readSecuritySchemes().values().iterator().next();
    assertTrue(scheme.getSemanticTypes().contains(WoTSec.OAuth2SecurityScheme));
    assertEquals("code", ((OAuth2SecurityScheme) scheme).getFlow());
    assertFalse(((OAuth2SecurityScheme) scheme).getAuthorization().isPresent());
  }

  @Test
  public void testOAuthSecuritySchemeInvalidToken() {
    String testTD =
      "@prefix td: <https://www.w3.org/2019/wot/td#> .\n" +
        "@prefix wotsec: <https://www.w3.org/2019/wot/security#> .\n" +
        "@prefix dct: <http://purl.org/dc/terms/> .\n" +
        "\n" +
        "<http://example.org/#thing> a td:Thing ;\n" +
        "    dct:title \"My Thing\" ;\n" +
        "    td:hasSecurityConfiguration [ a wotsec:OAuth2SecurityScheme ;\n" +
        "        wotsec:token \"invalidIRI\" ;\n" +
        "        wotsec:flow  \"code\";\n" +
        "  ] .";

    TDGraphReader reader = new TDGraphReader(RDFFormat.TURTLE, testTD);
    assertEquals(1, reader.readSecuritySchemes().size());
    SecurityScheme scheme = reader.readSecuritySchemes().values().iterator().next();
    assertTrue(scheme.getSemanticTypes().contains(WoTSec.OAuth2SecurityScheme));
    assertEquals("code", ((OAuth2SecurityScheme) scheme).getFlow());
    assertFalse(((OAuth2SecurityScheme) scheme).getToken().isPresent());
  }

  @Test
  public void testOAuthSecuritySchemeInvalidRefresh() {
    String testTD =
      "@prefix td: <https://www.w3.org/2019/wot/td#> .\n" +
        "@prefix wotsec: <https://www.w3.org/2019/wot/security#> .\n" +
        "@prefix dct: <http://purl.org/dc/terms/> .\n" +
        "\n" +
        "<http://example.org/#thing> a td:Thing ;\n" +
        "    dct:title \"My Thing\" ;\n" +
        "    td:hasSecurityConfiguration [ a wotsec:OAuth2SecurityScheme ;\n" +
        "        wotsec:refresh \"invalidIRI\" ;\n" +
        "        wotsec:flow  \"code\";\n" +
        "  ] .";

    TDGraphReader reader = new TDGraphReader(RDFFormat.TURTLE, testTD);
    assertEquals(1, reader.readSecuritySchemes().size());
    SecurityScheme scheme = reader.readSecuritySchemes().values().iterator().next();
    assertTrue(scheme.getSemanticTypes().contains(WoTSec.OAuth2SecurityScheme));
    assertEquals("code", ((OAuth2SecurityScheme) scheme).getFlow());
    assertFalse(((OAuth2SecurityScheme) scheme).getRefresh().isPresent());
  }

  @Test
  public void testReadUnknownSecurityScheme() {
    String testTD = PREFIXES +
      "\n" +
      "<http://example.org/#thing> a td:Thing ;\n" +
      "    dct:title \"My Thing\" ;\n" +
      "    td:hasSecurityConfiguration [ a ex:UnknownSecurityScheme ] ;\n" +
      "    td:hasBase <http://example.org/> .";

    TDGraphReader reader = new TDGraphReader(RDFFormat.TURTLE, testTD);
    Exception exception = assertThrows(InvalidTDException.class, () -> {
      reader.readSecuritySchemes();
    });

    String expectedMessage = "Unknown type of security scheme";

    String actualMessage = String.valueOf(exception.getCause());
    assertTrue(actualMessage.contains(expectedMessage));
  }

  @Test
  public void testReadMissingSecurityScheme() {
    String testTD = PREFIXES +
      "\n" +
      "<http://example.org/#thing> a td:Thing ;\n" +
      "    dct:title \"My Thing\" ;\n" +
      "    td:hasBase <http://example.org/> .";

    Exception exception = assertThrows(InvalidTDException.class, () -> {
      TDGraphReader.readFromString(TDFormat.RDF_TURTLE, testTD);
    });

    String expectedMessage = "Missing mandatory security definitions.";

    String actualMessage = String.valueOf(exception.getMessage());
    assertTrue(actualMessage.contains(expectedMessage));
  }

  @Test
  public void testReadMultipleSecuritySchemes() {
    String testTD = PREFIXES +
      "\n" +
      "<http://example.org/#thing> a td:Thing ;\n" +
      "    dct:title \"My Thing\" ;\n" +
      "    td:hasSecurityConfiguration [ a wotsec:NoSecurityScheme ] ;\n" +
      "    td:hasSecurityConfiguration [ a wotsec:APIKeySecurityScheme ] ;\n" +
      "    td:hasBase <http://example.org/> .";

    TDGraphReader reader = new TDGraphReader(RDFFormat.TURTLE, testTD);

    assertEquals(2, reader.readSecuritySchemes().size());
    assertTrue(reader.readSecuritySchemes().values().stream().anyMatch(scheme -> scheme
      .getSemanticTypes().contains(WoTSec.NoSecurityScheme)));
    assertTrue(reader.readSecuritySchemes().values().stream().anyMatch(scheme -> scheme
      .getSemanticTypes().contains(WoTSec.APIKeySecurityScheme)));
  }

  @Test
  public void testReadOneSimpleProperty() {
    TDGraphReader reader = new TDGraphReader(RDFFormat.TURTLE, TEST_SIMPLE_TD);

    List<PropertyAffordance> properties = reader.readProperties();
    assertEquals(1, properties.size());

    PropertyAffordance property = properties.get(0);
    assertEquals("my_property", property.getName());
    assertEquals("My Property", property.getTitle().get());
    assertTrue(property.isObservable());
    assertEquals(2, property.getSemanticTypes().size());
    assertEquals(2, property.getForms().size());

    Optional<Form> form = property.getFirstFormForOperationType(TD.readProperty);
    assertTrue(form.isPresent());
    assertEquals("websub", form.get().getSubProtocol().get());
  }

  @Test
  public void testReadFormWithHttpAndCoapBindings() {
    String testTD =
      "@prefix td: <https://www.w3.org/2019/wot/td#> .\n" +
        "@prefix htv: <http://www.w3.org/2011/http#> .\n" +
        "@prefix cov: <http://www.example.org/coap-binding#> .\n" +
        "@prefix hctl: <https://www.w3.org/2019/wot/hypermedia#> . \n" +
        "@prefix wotsec: <https://www.w3.org/2019/wot/security#> .\n" +
        "@prefix dct: <http://purl.org/dc/terms/> .\n" +
        "@prefix js: <https://www.w3.org/2019/wot/json-schema#> .\n" +
        "\n" +
        "<http://example.org/#thing> a td:Thing ;\n" +
        "    td:title \"My Thing\" ;\n" +
        "    td:hasSecurityConfiguration [ a wotsec:NoSecurityScheme ] ;\n" +
        "    td:hasPropertyAffordance [\n" +
        "        a td:PropertyAffordance, js:IntegerSchema ;\n" +
        "        td:name \"my_property\" ;\n" +
        "        td:isObservable true ;\n" +
        "        td:hasForm [\n" +
        "            htv:methodName \"GET\" ;\n" +
        "            hctl:hasTarget <http://example.org/property> ;\n" +
        "            hctl:forContentType \"application/json\";\n" +
        "            hctl:hasOperationType td:readProperty;\n" +
        "        ] ;\n" +
        "        td:hasForm [\n" +
        "            cov:methodName \"PUT\" ;\n" +
        "            hctl:hasTarget <coap://example.org/property> ;\n" +
        "            hctl:forContentType \"application/json\";\n" +
        "            hctl:hasOperationType td:writeProperty;\n" +
        "        ] ;\n" +
        "    ] .";

    TDGraphReader reader = new TDGraphReader(RDFFormat.TURTLE, testTD);

    List<PropertyAffordance> properties = reader.readProperties();
    assertEquals(1, properties.size());

    PropertyAffordance property = properties.get(0);
    assertEquals(2, property.getForms().size());

    Form formHTTP = property.getFirstFormForOperationType(TD.readProperty).get();
    Form formCoAP = property.getFirstFormForOperationType(TD.writeProperty).get();

    assertEquals("GET", formHTTP.getMethodName().get());
    assertEquals("http://example.org/property", formHTTP.getTarget());

    assertEquals("PUT", formCoAP.getMethodName().get());
    assertEquals("coap://example.org/property", formCoAP.getTarget());
  }

  @Test
  public void testReadReadPropertyDefaultMethodValues() {
    String testTD = PREFIXES +
      "<http://example.org/#thing> a td:Thing ;\n" +
      "    td:title \"My Thing\" ;\n" +
      "    td:hasSecurityConfiguration [ a wotsec:NoSecurityScheme ] ;\n" +
      "    td:hasPropertyAffordance [\n" +
      "        a td:PropertyAffordance, js:IntegerSchema ;\n" +
      "        td:name \"my_property\" ;\n" +
      "        td:isObservable true ;\n" +
      "        td:hasForm [\n" +
      "            hctl:hasTarget <http://example.org/count> ;\n" +
      "            hctl:forContentType \"application/json\";\n" +
      "            hctl:hasOperationType td:readProperty;\n" +
      "        ] , [\n" +
      "            hctl:hasTarget <coap://example.org/count> ;\n" +
      "            hctl:forContentType \"application/json\";\n" +
      "            hctl:hasOperationType td:readProperty;\n" +
      "        ] ;\n" +
      "    ] .";

    TDGraphReader reader = new TDGraphReader(RDFFormat.TURTLE, testTD);

    List<PropertyAffordance> properties = reader.readProperties();
    assertEquals(1, properties.size());

    PropertyAffordance property = properties.get(0);

    assertEquals(2, property.getForms().size());

    List<Form> forms = property.getForms();
    assertFalse(forms.get(0).getMethodName().isPresent());
    assertFalse(forms.get(1).getMethodName().isPresent());

    assertTrue(forms.get(0).getMethodName(TD.readProperty).isPresent());
    assertTrue(forms.get(1).getMethodName(TD.readProperty).isPresent());

    assertEquals("GET", forms.get(0).getMethodName(TD.readProperty).get());
    assertEquals("GET", forms.get(1).getMethodName(TD.readProperty).get());

    assertFalse(forms.get(0).getSubProtocol().isPresent());
    assertFalse(forms.get(1).getSubProtocol().isPresent());

    assertFalse(forms.get(0).getSubProtocol(TD.readProperty).isPresent());
    assertFalse(forms.get(1).getSubProtocol(TD.readProperty).isPresent());
  }

  @Test
  public void testReadWritePropertyDefaultMethodValues() {
    String testTD = PREFIXES +
      "<http://example.org/#thing> a td:Thing ;\n" +
      "    td:title \"My Thing\" ;\n" +
      "    td:hasSecurityConfiguration [ a wotsec:NoSecurityScheme ] ;\n" +
      "    td:hasPropertyAffordance [\n" +
      "        a td:PropertyAffordance, js:IntegerSchema ;\n" +
      "        td:name \"my_property\" ;\n" +
      "        td:isObservable true ;\n" +
      "        td:hasForm [\n" +
      "            hctl:hasTarget <http://example.org/count> ;\n" +
      "            hctl:forContentType \"application/json\";\n" +
      "            hctl:hasOperationType td:writeProperty;\n" +
      "        ] , [\n" +
      "            hctl:hasTarget <coap://example.org/count> ;\n" +
      "            hctl:forContentType \"application/json\";\n" +
      "            hctl:hasOperationType td:writeProperty;\n" +
      "        ] ;\n" +
      "    ] .";

    TDGraphReader reader = new TDGraphReader(RDFFormat.TURTLE, testTD);

    List<PropertyAffordance> properties = reader.readProperties();
    assertEquals(1, properties.size());

    PropertyAffordance property = properties.get(0);

    assertEquals(2, property.getForms().size());

    List<Form> forms = property.getForms();
    assertFalse(forms.get(0).getMethodName().isPresent());
    assertFalse(forms.get(1).getMethodName().isPresent());

    assertTrue(forms.get(0).getMethodName(TD.writeProperty).isPresent());
    assertTrue(forms.get(1).getMethodName(TD.writeProperty).isPresent());

    assertEquals("PUT", forms.get(0).getMethodName(TD.writeProperty).get());
    assertEquals("PUT", forms.get(1).getMethodName(TD.writeProperty).get());

    assertFalse(forms.get(0).getSubProtocol().isPresent());
    assertFalse(forms.get(1).getSubProtocol().isPresent());

    assertFalse(forms.get(0).getSubProtocol(TD.writeProperty).isPresent());
    assertFalse(forms.get(1).getSubProtocol(TD.writeProperty).isPresent());
  }

  @Test
  public void testReadObservePropertyDefaultMethodValues() {
    String testTD = PREFIXES +
      "<http://example.org/#thing> a td:Thing ;\n" +
      "    td:title \"My Thing\" ;\n" +
      "    td:hasSecurityConfiguration [ a wotsec:NoSecurityScheme ] ;\n" +
      "    td:hasPropertyAffordance [\n" +
      "        a td:PropertyAffordance, js:IntegerSchema ;\n" +
      "        td:name \"my_property\" ;\n" +
      "        td:isObservable true ;\n" +
      "        td:hasForm [\n" +
      "            hctl:hasTarget <coap://example.org/count> ;\n" +
      "            hctl:forContentType \"application/json\";\n" +
      "            hctl:hasOperationType td:observeProperty;\n" +
      "        ] ;\n" +
      "    ] .";

    TDGraphReader reader = new TDGraphReader(RDFFormat.TURTLE, testTD);

    List<PropertyAffordance> properties = reader.readProperties();
    assertEquals(1, properties.size());

    PropertyAffordance property = properties.get(0);

    assertEquals(1, property.getForms().size());

    Optional<Form> form = property.getFirstFormForOperationType(TD.observeProperty);
    assertTrue(form.isPresent());

    assertFalse(form.get().getMethodName().isPresent());
    assertTrue(form.get().getMethodName(TD.observeProperty).isPresent());
    assertEquals("GET", form.get().getMethodName(TD.observeProperty).get());

    assertFalse(form.get().getSubProtocol().isPresent());
    assertTrue(form.get().getSubProtocol(TD.observeProperty).isPresent());
    assertEquals(COV.observe, form.get().getSubProtocol(TD.observeProperty).get());
  }

  @Test
  public void testReadUnobservePropertyDefaultMethodValues() {
    String testTD = PREFIXES +
      "<http://example.org/#thing> a td:Thing ;\n" +
      "    td:title \"My Thing\" ;\n" +
      "    td:hasSecurityConfiguration [ a wotsec:NoSecurityScheme ] ;\n" +
      "    td:hasPropertyAffordance [\n" +
      "        a td:PropertyAffordance, js:IntegerSchema ;\n" +
      "        td:name \"my_property\" ;\n" +
      "        td:isObservable true ;\n" +
      "        td:hasForm [\n" +
      "            hctl:hasTarget <coap://example.org/count> ;\n" +
      "            hctl:forContentType \"application/json\";\n" +
      "            hctl:hasOperationType td:unobserveProperty;\n" +
      "        ] ;\n" +
      "    ] .";

    TDGraphReader reader = new TDGraphReader(RDFFormat.TURTLE, testTD);

    List<PropertyAffordance> properties = reader.readProperties();
    assertEquals(1, properties.size());

    PropertyAffordance property = properties.get(0);

    assertEquals(1, property.getForms().size());

    Optional<Form> form = property.getFirstFormForOperationType(TD.unobserveProperty);
    assertTrue(form.isPresent());

    assertFalse(form.get().getMethodName().isPresent());
    assertTrue(form.get().getMethodName(TD.unobserveProperty).isPresent());
    assertEquals("GET", form.get().getMethodName(TD.unobserveProperty).get());

    assertFalse(form.get().getSubProtocol().isPresent());
    assertTrue(form.get().getSubProtocol(TD.unobserveProperty).isPresent());
    assertEquals(COV.observe, form.get().getSubProtocol(TD.unobserveProperty).get());
  }

  @Test
  public void testFormWithUnknownProtocolBinding() {
    String testTD =
      "@prefix td: <https://www.w3.org/2019/wot/td#> .\n" +
        "@prefix hctl: <https://www.w3.org/2019/wot/hypermedia#> . \n" +
        "@prefix wotsec: <https://www.w3.org/2019/wot/security#> .\n" +
        "@prefix dct: <http://purl.org/dc/terms/> .\n" +
        "@prefix js: <https://www.w3.org/2019/wot/json-schema#> .\n" +
        "\n" +
        "<http://example.org/#thing> a td:Thing ;\n" +
        "    td:title \"My Thing\" ;\n" +
        "    td:hasSecurityConfiguration [ a wotsec:NoSecurityScheme ] ;\n" +
        "    td:hasPropertyAffordance [\n" +
        "        a td:PropertyAffordance, js:IntegerSchema ;\n" +
        "        td:name \"my_property\" ;\n" +
        "        td:hasForm [\n" +
        "            hctl:hasTarget <x://example.org/property> ;\n" +
        "            hctl:forContentType \"application/json\";\n" +
        "            hctl:hasOperationType td:readProperty;\n" +
        "        ] ;\n" +
        "    ] .";

    TDGraphReader reader = new TDGraphReader(RDFFormat.TURTLE, testTD);

    PropertyAffordance property = reader.readProperties().get(0);

    assertTrue(property.getFirstFormForOperationType(TD.readProperty).isPresent());
    Form form = property.getFirstFormForOperationType(TD.readProperty).get();

    assertEquals("x://example.org/property", form.getTarget());
    assertEquals("application/json", form.getContentType());
    assertTrue(form.getOperationTypes().contains(TD.readProperty));
    assertFalse(form.getMethodName().isPresent());
  }

  @Test
  public void testReadSubProtocolStringAndIRI() {
    String testTD =
      "@prefix td: <https://www.w3.org/2019/wot/td#> .\n" +
        "@prefix htv: <http://www.w3.org/2011/http#> .\n" +
        "@prefix cov: <http://www.example.org/coap-binding#> .\n" +
        "@prefix hctl: <https://www.w3.org/2019/wot/hypermedia#> . \n" +
        "@prefix wotsec: <https://www.w3.org/2019/wot/security#> .\n" +
        "@prefix dct: <http://purl.org/dc/terms/> .\n" +
        "@prefix js: <https://www.w3.org/2019/wot/json-schema#> .\n" +
        "\n" +
        "<http://example.org/#thing> a td:Thing ;\n" +
        "    td:title \"My Thing\" ;\n" +
        "    td:hasSecurityConfiguration [ a wotsec:NoSecurityScheme ] ;\n" +
        "    td:hasPropertyAffordance [\n" +
        "        a td:PropertyAffordance, js:IntegerSchema ;\n" +
        "        td:name \"my_property\" ;\n" +
        "        td:isObservable true ;\n" +
        "        td:hasForm [\n" +
        "            htv:methodName \"GET\" ;\n" +
        "            hctl:hasTarget <http://example.org/property> ;\n" +
        "            hctl:forContentType \"application/json\";\n" +
        "            hctl:hasOperationType td:readProperty;\n" +
        "            hctl:forSubProtocol \"websub\";\n" +
        "        ] ;\n" +
        "        td:hasForm [\n" +
        "            cov:methodName \"GET\" ;\n" +
        "            hctl:hasTarget <coap://example.org/property> ;\n" +
        "            hctl:forContentType \"application/json\";\n" +
        "            hctl:hasOperationType td:observeProperty;\n" +
        "            hctl:forSubProtocol cov:observe;\n" +
        "        ] ;\n" +
        "    ] .";
    TDGraphReader reader = new TDGraphReader(RDFFormat.TURTLE, testTD);

    PropertyAffordance property = reader.readProperties().get(0);
    Form formHTTP = property.getFirstFormForOperationType(TD.readProperty).get();
    Form formCoAP = property.getFirstFormForOperationType(TD.observeProperty).get();

    assertEquals(COV.observe, formCoAP.getSubProtocol().get());
    assertEquals("websub", formHTTP.getSubProtocol().get());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testReadSubProtocolUnknownOperationType() {
    String testTD = PREFIXES +
      "<http://example.org/#thing> a td:Thing ;\n" +
      "    td:title \"My Thing\" ;\n" +
      "    td:hasSecurityConfiguration [ a wotsec:NoSecurityScheme ] ;\n" +
      "    td:hasPropertyAffordance [\n" +
      "        a td:PropertyAffordance, js:IntegerSchema ;\n" +
      "        td:name \"my_property\" ;\n" +
      "        td:isObservable true ;\n" +
      "        td:hasForm [\n" +
      "            hctl:hasTarget <coap://example.org/count> ;\n" +
      "            hctl:forContentType \"application/json\";\n" +
      "            hctl:hasOperationType td:writeProperty;\n" +
      "        ] ;\n" +
      "    ] .";

    TDGraphReader reader = new TDGraphReader(RDFFormat.TURTLE, testTD);

    List<PropertyAffordance> properties = reader.readProperties();
    assertEquals(1, properties.size());

    PropertyAffordance property = properties.get(0);

    Optional<Form> form = property.getFirstFormForOperationType(TD.writeProperty);
    assertTrue(form.isPresent());

    form.get().getSubProtocol(TD.observeProperty);
  }

  @Test
  public void testReadOnePropertyNoSchema() {
    String testTD =
        "@prefix td: <https://www.w3.org/2019/wot/td#> .\n" +
        "@prefix htv: <http://www.w3.org/2011/http#> .\n" +
        "@prefix hctl: <https://www.w3.org/2019/wot/hypermedia#> .\n" +
        "@prefix dct: <http://purl.org/dc/terms/> .\n" +
        "@prefix wotsec: <https://www.w3.org/2019/wot/security#> .\n" +
        "@prefix js: <https://www.w3.org/2019/wot/json-schema#> .\n" +
        "<http://example.org/#thing> a td:Thing ;\n" +
        "    td:title \"My Thing\" ;\n" +
        "    td:hasSecurityConfiguration [ a wotsec:NoSecurityScheme ] ;\n" +
        "    td:hasPropertyAffordance [\n" +
        "        a td:PropertyAffordance ;\n" +
        "        td:name \"my_property\" ;\n" +
        "        td:isObservable false ;\n" +
        "        td:hasForm [\n" +
        "            hctl:hasTarget <http://example.org/count> ;\n" +
        "            hctl:forContentType \"video/mpeg\";\n" +
        "            hctl:hasOperationType td:readProperty;\n" +
        "        ] ;\n" +
        "    ] .";

    TDGraphReader reader = new TDGraphReader(RDFFormat.TURTLE, testTD);

    assertEquals(1, reader.readProperties().size());

    PropertyAffordance property = reader.readProperties().get(0);

    DataSchema schema = property.getDataSchema();
    assertEquals(DataSchema.DATA, schema.getDatatype());
    assertTrue(schema.getSemanticTypes().isEmpty());
    assertTrue(schema.getEnumeration().isEmpty());
  }

  @Test
  public void testReadOneSimpleAction() {
    TDGraphReader reader = new TDGraphReader(RDFFormat.TURTLE, TEST_SIMPLE_TD);

    assertEquals(1, reader.readActions().size());
    ActionAffordance action = reader.readActions().get(0);

    assertEquals("my_action", action.getName());
    assertEquals("My Action", action.getTitle().get());
    assertEquals(1, action.getSemanticTypes().size());
    assertEquals(TD.ActionAffordance, action.getSemanticTypes().get(0));

    assertEquals(1, action.getForms().size());
    Form form = action.getForms().get(0);

    assertForm(form, "PUT", "http://example.org/action", "application/json", TD.invokeAction);
  }

  @Test
  public void testReadMultipleSimpleActions() {
    String testTD =
      "@prefix td: <https://www.w3.org/2019/wot/td#> .\n" +
        "@prefix htv: <http://www.w3.org/2011/http#> .\n" +
        "@prefix hctl: <https://www.w3.org/2019/wot/hypermedia#> .\n" +
        "@prefix dct: <http://purl.org/dc/terms/> .\n" +
        "@prefix wotsec: <https://www.w3.org/2019/wot/security#> .\n" +
        "@prefix js: <https://www.w3.org/2019/wot/json-schema#> .\n" +
        "\n" +
        "<http://example.org/#thing> a td:Thing ;\n" +
        "    td:title \"My Thing\" ;\n" +
        "    td:hasSecurityConfiguration [ a wotsec:NoSecurityScheme ] ;\n" +
        "    td:baseURI <http://example.org/> ;\n" +
        "    td:hasActionAffordance [\n" +
        "        a td:ActionAffordance ;\n" +
        "        td:name \"first_action\" ;\n" +
        "        td:title \"First Action\" ;\n" +
        "        td:hasForm [\n" +
        "            htv:methodName \"PUT\" ;\n" +
        "            hctl:hasTarget <http://example.org/action1> ;\n" +
        "            hctl:forContentType \"application/json\";\n" +
        "            hctl:hasOperationType td:invokeAction;\n" +
        "        ] ;\n" +
        "    ] ;\n" +
        "    td:hasActionAffordance [\n" +
        "        a td:ActionAffordance ;\n" +
        "        td:name \"second_action\" ;\n" +
        "        td:title \"Second Action\" ;\n" +
        "        td:hasForm [\n" +
        "            htv:methodName \"PUT\" ;\n" +
        "            hctl:hasTarget <http://example.org/action2> ;\n" +
        "            hctl:forContentType \"application/json\";\n" +
        "            hctl:hasOperationType td:invokeAction;\n" +
        "        ] ;\n" +
        "    ] ;\n" +
        "    td:hasActionAffordance [\n" +
        "        a td:ActionAffordance ;\n" +
        "        td:name \"third_action\" ;\n" +
        "        td:title \"Third Action\" ;\n" +
        "        td:hasForm [\n" +
        "            htv:methodName \"PUT\" ;\n" +
        "            hctl:hasTarget <http://example.org/action3> ;\n" +
        "            hctl:forContentType \"application/json\";\n" +
        "            hctl:hasOperationType td:invokeAction;\n" +
        "        ] ;\n" +
        "    ] .";

    TDGraphReader reader = new TDGraphReader(RDFFormat.TURTLE, testTD);

    assertEquals(3, reader.readActions().size());

    List<String> actionTitles = reader.readActions().stream().map(action -> action.getTitle().get())
        .collect(Collectors.toList());

    assertTrue(actionTitles.contains("First Action"));
    assertTrue(actionTitles.contains("Second Action"));
    assertTrue(actionTitles.contains("Third Action"));
  }

  @Test
  public void testReadOneActionOneObjectInput() {
    String testSimpleObject =
        "        td:hasInputSchema [\n" +
        "            a js:ObjectSchema ;\n" +
        "            js:properties [\n" +
        "                a js:BooleanSchema ;\n" +
        "                js:propertyName \"boolean_value\";\n" +
        "            ] ;\n" +
        "            js:properties [\n" +
        "                a js:NumberSchema ;\n" +
        "                js:propertyName \"number_value\";\n" +
        "                js:maximum 100.05 ;\n" +
        "                js:minimum -100.05 ;\n" +
        "            ] ;\n" +
        "            js:properties [\n" +
        "                a js:IntegerSchema ;\n" +
        "                js:propertyName \"integer_value\";\n" +
        "                js:maximum 100 ;\n" +
        "                js:minimum -100 ;\n" +
        "            ] ;\n" +
        "            js:properties [\n" +
        "                a js:StringSchema ;\n" +
        "                js:propertyName \"string_value\";\n" +
        "            ] ;\n" +
        "            js:properties [\n" +
        "                a js:NullSchema ;\n" +
        "                js:propertyName \"null_value\";\n" +
        "            ] ;\n" +
        "            js:required \"integer_value\", \"number_value\" ;\n" +
        "        ]\n";

    TDGraphReader reader = new TDGraphReader(RDFFormat.TURTLE, TEST_IO_HEAD + testSimpleObject
        + TEST_IO_TAIL);

    ActionAffordance action = reader.readActions().get(0);

    Optional<DataSchema> input = action.getInputSchema();
    assertTrue(input.isPresent());
    assertEquals(DataSchema.OBJECT, input.get().getDatatype());

    ObjectSchema schema = (ObjectSchema) input.get();
    assertEquals(5, schema.getProperties().size());

    DataSchema booleanProperty = schema.getProperties().get("boolean_value");
    assertEquals(DataSchema.BOOLEAN, booleanProperty.getDatatype());

    DataSchema integerProperty = schema.getProperties().get("integer_value");
    assertEquals(DataSchema.INTEGER, integerProperty.getDatatype());
    assertEquals(-100, ((IntegerSchema) integerProperty).getMinimum().get().intValue());
    assertEquals(100, ((IntegerSchema) integerProperty).getMaximum().get().intValue());

    DataSchema numberProperty = schema.getProperties().get("number_value");
    assertEquals(DataSchema.NUMBER, numberProperty.getDatatype());
    assertEquals(-100.05, ((NumberSchema) numberProperty).getMinimum().get().doubleValue(), 0.001);
    assertEquals(100.05, ((NumberSchema) numberProperty).getMaximum().get().doubleValue(), 0.001);

    DataSchema stringProperty = schema.getProperties().get("string_value");
    assertEquals(DataSchema.STRING, stringProperty.getDatatype());

    DataSchema nullProperty = schema.getProperties().get("null_value");
    assertEquals(DataSchema.NULL, nullProperty.getDatatype());

    assertEquals(2, schema.getRequiredProperties().size());
    assertTrue(schema.getRequiredProperties().contains("integer_value"));
    assertTrue(schema.getRequiredProperties().contains("number_value"));
  }

  @Test
  public void testReadOneEvent() {
    TDGraphReader reader = new TDGraphReader(RDFFormat.TURTLE, TEST_SIMPLE_TD);

    List<EventAffordance> events = reader.readEvents();
    assertEquals(1, events.size());

    EventAffordance event = events.get(0);
    assertEquals("my_event", event.getName());
    assertEquals("My Event", event.getTitle().get());
    assertEquals(1, event.getSemanticTypes().size());
    assertEquals(1, event.getForms().size());

    Optional<Form> form = event.getFirstFormForOperationType(TD.subscribeEvent);
    assertTrue(form.isPresent());

    /* Test subscription schema */
    Optional<DataSchema> subscription = event.getSubscriptionSchema();
    assertTrue(subscription.isPresent());
    assertEquals(DataSchema.OBJECT, subscription.get().getDatatype());

    ObjectSchema subscriptionSchema = (ObjectSchema) subscription.get();
    assertEquals(1, subscriptionSchema.getProperties().size());

    DataSchema stringProperty = subscriptionSchema.getProperties().get("string_value");
    assertEquals(DataSchema.STRING, stringProperty.getDatatype());

    /* Test notification schema */
    Optional<DataSchema> notification = event.getNotificationSchema();
    assertTrue(notification.isPresent());
    assertEquals(DataSchema.OBJECT, notification.get().getDatatype());

    ObjectSchema notificationSchema = (ObjectSchema) notification.get();
    assertEquals(1, notificationSchema.getProperties().size());

    DataSchema integerProperty = notificationSchema.getProperties().get("integer_value");
    assertEquals(DataSchema.INTEGER, integerProperty.getDatatype());

    /* Test cancellation schema */
    Optional<DataSchema> cancellation = event.getCancellationSchema();
    assertTrue(cancellation.isPresent());
    assertEquals(DataSchema.OBJECT, cancellation.get().getDatatype());

    ObjectSchema cancellationSchema = (ObjectSchema) cancellation.get();
    assertEquals(1, cancellationSchema.getProperties().size());

    DataSchema booleanProperty = cancellationSchema.getProperties().get("boolean_value");
    assertEquals(DataSchema.BOOLEAN, booleanProperty.getDatatype());
  }

  @Test
  public void testReadEventDefaultValues() {
    String testTD = PREFIXES +
      "<http://example.org/#thing> a td:Thing ;\n" +
      "    td:title \"My Thing\" ;\n" +
      "    td:hasSecurityConfiguration [ a wotsec:NoSecurityScheme ] ;\n" +
      "    td:hasEventAffordance [\n" +
      "        a td:EventAffordance ;\n" +
      "        td:name \"my_event\" ;\n" +
      "        td:hasForm [\n" +
      "            hctl:hasTarget <coap://example.org/event> ;\n" +
      "            hctl:forContentType \"application/json\";\n" +
      "        ] ;\n" +
      "    ] .";

    TDGraphReader reader = new TDGraphReader(RDFFormat.TURTLE, testTD);

    List<EventAffordance> events = reader.readEvents();
    assertEquals(1, events.size());

    EventAffordance event = events.get(0);

    assertEquals(1, event.getForms().size());

    Optional<Form> subscribeForm = event.getFirstFormForOperationType(TD.subscribeEvent);
    Optional<Form> unsubscribeForm = event.getFirstFormForOperationType(TD.unsubscribeEvent);

    assertTrue(subscribeForm.isPresent());
    assertTrue(unsubscribeForm.isPresent());

    assertFalse(subscribeForm.get().getMethodName().isPresent());
    assertFalse(unsubscribeForm.get().getMethodName().isPresent());

    assertTrue(subscribeForm.get().getMethodName(TD.subscribeEvent).isPresent());
    assertTrue(unsubscribeForm.get().getMethodName(TD.unsubscribeEvent).isPresent());

    assertEquals("GET", subscribeForm.get().getMethodName(TD.subscribeEvent).get());
    assertEquals("GET", unsubscribeForm.get().getMethodName(TD.unsubscribeEvent).get());

    assertFalse(subscribeForm.get().getSubProtocol().isPresent());
    assertFalse(unsubscribeForm.get().getSubProtocol().isPresent());

    assertTrue(subscribeForm.get().getSubProtocol(TD.subscribeEvent).isPresent());
    assertTrue(unsubscribeForm.get().getSubProtocol(TD.unsubscribeEvent).isPresent());

    assertEquals(COV.observe, subscribeForm.get().getSubProtocol(TD.subscribeEvent).get());
    assertEquals(COV.observe, unsubscribeForm.get().getSubProtocol(TD.unsubscribeEvent).get());
  }

  @Test
  public void testReadEventNoEventAffordanceType() {
    String testTD = PREFIXES +
      "<http://example.org/#thing> a td:Thing ;\n" +
      "    td:title \"My Thing\" ;\n" +
      "    td:hasSecurityConfiguration [ a wotsec:NoSecurityScheme ] ;\n" +
      "    td:hasEventAffordance [\n" +
      "        td:name \"my_event\" ;\n" +
      "        td:hasForm [\n" +
      "            hctl:hasTarget <coap://example.org/event> ;\n" +
      "            hctl:forContentType \"application/json\";\n" +
      "        ] ;\n" +
      "    ] .";

    TDGraphReader reader = new TDGraphReader(RDFFormat.TURTLE, testTD);

    List<EventAffordance> events = reader.readEvents();
    assertEquals(0, events.size());
  }

  @Test
  public void testReadEventInvalidEventDefinitionNoForm() {
    String testTD = PREFIXES +
      "<http://example.org/#thing> a td:Thing ;\n" +
      "    td:title \"My Thing\" ;\n" +
      "    td:hasSecurityConfiguration [ a wotsec:NoSecurityScheme ] ;\n" +
      "    td:hasEventAffordance [\n" +
      "        a td:EventAffordance ;\n" +
      "        td:name \"my_event\" \n" +
      "    ] .";

    TDGraphReader reader = new TDGraphReader(RDFFormat.TURTLE, testTD);

    Exception exception = assertThrows(InvalidTDException.class, () -> {
      reader.readEvents();
    });

    String expectedMessage = "Invalid event definition.";
    String actualMessage = exception.getMessage();

    assertTrue(actualMessage.contains(expectedMessage));
  }

  @Test
  public void testReadEventInvalidEventDefinitionInvalidNotificationSchema() {
    String testTD = PREFIXES +
      "<http://example.org/#thing> a td:Thing ;\n" +
      "    td:title \"My Thing\" ;\n" +
      "    td:hasSecurityConfiguration [ a wotsec:NoSecurityScheme ] ;\n" +
      "    td:hasEventAffordance [\n" +
      "        a td:EventAffordance ;\n" +
      "        td:name \"my_event\" ;\n" +
      "        td:hasForm [\n" +
      "            hctl:hasTarget <http://example.org/event> ;\n" +
      "        ] ;\n" +
      "        td:hasSubscriptionSchema [\n" +
      "            a js:ObjectSchema ;\n" +
      "            js:properties [\n" +
      "                a js:StringSchema ;\n" +
      "                js:propertyName \"string_value\";\n" +
      "            ] ;\n" +
      "            js:required \"invalid_value\" ;\n" +
      "        ] ;\n" +
      "    ] .";

    TDGraphReader reader = new TDGraphReader(RDFFormat.TURTLE, testTD);

    Exception exception = assertThrows(InvalidTDException.class, () -> {
      reader.readEvents();
    });

    String expectedMessage = "Invalid event definition.";
    String actualMessage = exception.getMessage();

    assertTrue(actualMessage.contains(expectedMessage));
  }

  @Test
  public void testReadTDFromFile() throws IOException {
    // Read a TD from a File by passing its path as parameter
    ThingDescription simple = TDGraphReader.readFromFile(TDFormat.RDF_TURTLE, "samples/simple_td.ttl");
    ThingDescription forklift = TDGraphReader.readFromFile(TDFormat.RDF_TURTLE, "samples/forkliftRobot.ttl");

    // Check if a TD was created from the file by checking its title
    assertEquals("My Thing", simple.getTitle());
    assertEquals("forkliftRobot", forklift.getTitle());
  }

  @Test
  public void testReadSimpleFullTD() {
    ThingDescription td = TDGraphReader.readFromString(TDFormat.RDF_TURTLE, TEST_SIMPLE_TD);

    // Check metadata
    assertEquals("My Thing", td.getTitle());
    assertEquals("http://example.org/#thing", td.getThingURI().get());
    assertEquals(1, td.getSemanticTypes().size());
    assertTrue(td.getSemanticTypes().contains("https://www.w3.org/2019/wot/td#Thing"));
    assertTrue(td.getSecuritySchemes().stream().anyMatch(scheme -> scheme.getSemanticTypes()
      .contains(WoTSec.NoSecurityScheme)));
    assertEquals(1, td.getActions().size());

    // Check action metadata
    ActionAffordance action = td.getActions().get(0);
    assertEquals("My Action", action.getTitle().get());
    assertEquals(1, action.getForms().size());

    // Check action form
    Form form = action.getForms().get(0);
    assertForm(form, "PUT", "http://example.org/action", "application/json", TD.invokeAction);

    // Check action input data schema
    ObjectSchema input = (ObjectSchema) action.getInputSchema().get();
    assertEquals(DataSchema.OBJECT, input.getDatatype());
    assertEquals(1, input.getProperties().size());
    assertEquals(1, input.getRequiredProperties().size());

    assertEquals(DataSchema.NUMBER, input.getProperties().get("number_value").getDatatype());
    assertTrue(input.getRequiredProperties().contains("number_value"));

    // Check action output data schema
    ObjectSchema output = (ObjectSchema) action.getOutputSchema().get();
    assertEquals(DataSchema.OBJECT, output.getDatatype());
    assertEquals(1, output.getProperties().size());
    assertEquals(1, output.getRequiredProperties().size());

    assertEquals(DataSchema.BOOLEAN, output.getProperties().get("boolean_value").getDatatype());
    assertTrue(output.getRequiredProperties().contains("boolean_value"));
  }

  @Test
  public void testMissingMandatoryTitle() {
    String testTDWithMissingTitle =
      "@prefix td: <https://www.w3.org/2019/wot/td#> .\n" +
        "@prefix wotsec: <https://www.w3.org/2019/wot/security#> .\n" +
        "\n" +
        "<http://example.org/#thing> a td:Thing ;\n" +
        "    td:hasSecurityConfiguration [ a wotsec:NoSecurityScheme ] ;\n" +
        "    td:baseURI <http://example.org/> .\n";

    Exception exception = assertThrows(InvalidTDException.class, () -> {
      TDGraphReader.readFromString(TDFormat.RDF_TURTLE, testTDWithMissingTitle);
    });

    String expectedMessage = "Missing mandatory title";
    String actualMessage = exception.getMessage();

    assertTrue(actualMessage.contains(expectedMessage));
  }

  @Test
  public void testMissingMandatoryPropertyAffordanceName() {
    String testTDWithMissingAffordanceName =
      "@prefix td: <https://www.w3.org/2019/wot/td#> .\n" +
        "@prefix htv: <http://www.w3.org/2011/http#> .\n" +
        "@prefix hctl: <https://www.w3.org/2019/wot/hypermedia#> .\n" +
        "@prefix dct: <http://purl.org/dc/terms/> .\n" +
        "@prefix wotsec: <https://www.w3.org/2019/wot/security#> .\n" +
        "@prefix js: <https://www.w3.org/2019/wot/json-schema#> .\n" +
        "\n" +
        "<http://example.org/#thing> a td:Thing ;\n" +
        "    td:title \"My Thing\" ;\n" +
        "    td:hasSecurityConfiguration [ a wotsec:NoSecurityScheme ] ;\n" +
        "    td:hasPropertyAffordance [\n" +
        "        a td:PropertyAffordance, js:NumberSchema ;\n" +
        "        td:hasForm [\n" +
        "            htv:methodName \"PUT\" ;\n" +
        "            hctl:hasTarget <http://example.org/property> ;\n" +
        "            hctl:forContentType \"application/json\";\n" +
        "            hctl:hasOperationType td:writeProperty;\n" +
        "        ] ;\n" +
        "    ] .";

    Exception exception = assertThrows(InvalidTDException.class, () -> {
      TDGraphReader.readFromString(TDFormat.RDF_TURTLE, testTDWithMissingAffordanceName);
    });

    StringWriter writer = new StringWriter();
    exception.printStackTrace(new PrintWriter(writer));
    String expectedMessage = "Invalid property definition";
    String expectedRootMessage = "Missing mandatory affordance name";
    String actualMessage = writer.toString();

    assertTrue(actualMessage.contains(expectedMessage));
    assertTrue(actualMessage.contains(expectedRootMessage));
  }

  @Test
  public void testMissingMandatoryActionAffordanceName() {
    String testTDWithMissingAffordanceName =
      "@prefix td: <https://www.w3.org/2019/wot/td#> .\n" +
        "@prefix htv: <http://www.w3.org/2011/http#> .\n" +
        "@prefix hctl: <https://www.w3.org/2019/wot/hypermedia#> .\n" +
        "@prefix dct: <http://purl.org/dc/terms/> .\n" +
        "@prefix wotsec: <https://www.w3.org/2019/wot/security#> .\n" +
        "@prefix js: <https://www.w3.org/2019/wot/json-schema#> .\n" +
        "\n" +
        "<http://example.org/#thing> a td:Thing ;\n" +
        "    td:title \"My Thing\" ;\n" +
        "    td:hasSecurityConfiguration [ a wotsec:NoSecurityScheme ] ;\n" +
        "    td:hasActionAffordance [\n" +
        "        a td:ActionAffordance ;\n" +
        "        td:hasForm [\n" +
        "            htv:methodName \"PUT\" ;\n" +
        "            hctl:hasTarget <http://example.org/action> ;\n" +
        "            hctl:forContentType \"application/json\";\n" +
        "            hctl:hasOperationType td:invokeAction;\n" +
        "        ] ;\n" +
        "    ] .";

    Exception exception = assertThrows(InvalidTDException.class, () -> {
      TDGraphReader.readFromString(TDFormat.RDF_TURTLE, testTDWithMissingAffordanceName);
    });

    StringWriter writer = new StringWriter();
    exception.printStackTrace(new PrintWriter(writer));
    String expectedMessage = "Invalid action definition";
    String expectedRootMessage = "Missing mandatory affordance name";
    String actualMessage = writer.toString();

    assertTrue(actualMessage.contains(expectedMessage));
    assertTrue(actualMessage.contains(expectedRootMessage));
  }

  @Test
  public void testUriVariable(){
    String TDDescription = "@prefix td: <https://www.w3.org/2019/wot/td#> .\n" +
      "@prefix htv: <http://www.w3.org/2011/http#> .\n" +
      "@prefix hctl: <https://www.w3.org/2019/wot/hypermedia#> .\n" +
      "@prefix dct: <http://purl.org/dc/terms/> .\n" +
      "@prefix wotsec: <https://www.w3.org/2019/wot/security#> .\n" +
      "@prefix js: <https://www.w3.org/2019/wot/json-schema#> .\n" +
      "<http://example.org/lamp123> a td:Thing, <https://saref.etsi.org/core/LightSwitch>;\n" +
      "  td:title \"My Lamp Thing\";\n" +
      "  td:hasSecurityConfiguration [ a wotsec:NoSecurityScheme\n" +
      "    ];\n" +
      "  td:hasActionAffordance [ a td:ActionAffordance,\n" +
      "        <https://saref.etsi.org/core/ToggleCommand>;\n" +
      "  td:name   \"toggleAffordance\"; "+
      "      td:hasUriTemplateSchema [ a js:StringSchema;\n"+
      "      td:name \"token\"    ];"+
      "      td:title \"Toggle\";\n" +
      "      td:hasForm [\n" +
      "          htv:methodName \"PUT\";\n" +
      "          hctl:hasTarget <http://mylamp.example.org/toggle/{token}>;\n" +
      "          hctl:forContentType \"application/json\";\n" +
      "          hctl:hasOperationType td:invokeAction\n" +
      "        ];\n" +
      "      td:hasInputSchema [ a js:ObjectSchema,\n" +
      "            <https://saref.etsi.org/core/OnOffState>;\n" +
      "          js:properties [ a js:BooleanSchema;\n" +
      "              js:propertyName \"status\"\n" +
      "            ];\n" +
      "          js:required \"status\"\n" +
      "        ]\n" +
      "    ] .\n";
    ThingDescription td = TDGraphReader.readFromString(TDFormat.RDF_TURTLE, TDDescription);
    String s = td.getActions().get(0).getUriVariables().get().get("token").getDatatype();
    assertEquals(DataSchema.STRING,s);
  }

  @Test
  public void testManyUriVariables(){
    String TDDescription = "@prefix td: <https://www.w3.org/2019/wot/td#> .\n" +
      "@prefix htv: <http://www.w3.org/2011/http#> .\n" +
      "@prefix hctl: <https://www.w3.org/2019/wot/hypermedia#> .\n" +
      "@prefix dct: <http://purl.org/dc/terms/> .\n" +
      "@prefix wotsec: <https://www.w3.org/2019/wot/security#> .\n" +
      "@prefix js: <https://www.w3.org/2019/wot/json-schema#> .\n" +
      "<http://example.org/lamp123> a td:Thing, <https://saref.etsi.org/core/LightSwitch>;\n" +
      "  td:title \"My Lamp Thing\";\n" +
      "  td:hasSecurityConfiguration [ a wotsec:NoSecurityScheme\n" +
      "    ];\n" +
      "  td:hasActionAffordance [ a td:ActionAffordance,\n" +
      "        <https://saref.etsi.org/core/ToggleCommand>;\n" +
      "td:name    \"toggleAffordance\";  "+
      "      td:hasUriTemplateSchema [ a js:StringSchema;\n"+
      "      td:name     \"name\" ];   "+
      "      td:hasUriTemplateSchema [ a js:NumberSchema;\n"+
      "      td:name     \"number\" ];   "+
      "      td:title \"Toggle\";\n" +
      "      td:hasForm [\n" +
      "          htv:methodName \"PUT\";\n" +
      "          hctl:hasTarget <http://mylamp.example.org/{name}/{number}/toggle>;\n" +
      "          hctl:forContentType \"application/json\";\n" +
      "          hctl:hasOperationType td:invokeAction\n" +
      "        ];\n" +
      "      td:hasInputSchema [ a js:ObjectSchema,\n" +
      "            <https://saref.etsi.org/core/OnOffState>;\n" +
      "          js:properties [ a js:BooleanSchema;\n" +
      "              js:propertyName \"status\"\n" +
      "            ];\n" +
      "          js:required \"status\"\n" +
      "        ]\n" +
      "    ] .\n";
    ThingDescription td = TDGraphReader.readFromString(TDFormat.RDF_TURTLE, TDDescription);
    DataSchema uriVariableSchema1 = td.getActions().get(0).getUriVariables().get().get("name");
    assertEquals(DataSchema.STRING,uriVariableSchema1.getDatatype());
    DataSchema uriVariableSchema2=td.getActions().get(0).getUriVariables().get().get("number");
    assertEquals(DataSchema.NUMBER,uriVariableSchema2.getDatatype());
  }

  @Test
  public void testUriVariablePropertyAffordance(){
    String TDDescription =
      "@prefix td: <https://www.w3.org/2019/wot/td#> .\n" +
        "@prefix htv: <http://www.w3.org/2011/http#> .\n" +
        "@prefix hctl: <https://www.w3.org/2019/wot/hypermedia#> .\n" +
        "@prefix dct: <http://purl.org/dc/terms/> .\n" +
        "@prefix wotsec: <https://www.w3.org/2019/wot/security#> .\n" +
        "@prefix js: <https://www.w3.org/2019/wot/json-schema#> .\n" +
        "\n" +
        "<http://example.org/#thing> a td:Thing ;\n" +
        "    td:title \"My Thing\" ;\n" +
        "    td:hasSecurityConfiguration [ a wotsec:NoSecurityScheme ] ;\n" +
        "    td:baseURI <http://example.org/> ;\n" +
        "    td:hasPropertyAffordance [\n" +
        "        a td:PropertyAffordance, js:NumberSchema ;\n" +
        "        td:name \"my_property\" ;\n" +
        "        td:title \"My Property\" ;\n" +
        "        td:isObservable true ;\n" +
        "        td:hasForm [\n" +
        "            htv:methodName \"PUT\" ;\n" +
        "            hctl:hasTarget <http://example.org/property/{name}> ;\n" +
        "            hctl:forContentType \"application/json\";\n" +
        "            hctl:hasOperationType td:writeProperty;\n" +
        "        ] ;\n" +
        "        td:hasForm [\n" +
        "            htv:methodName \"GET\" ;\n" +
        "            hctl:hasTarget <http://example.org/property/{name}> ;\n" +
        "            hctl:forContentType \"application/json\";\n" +
        "            hctl:hasOperationType td:readProperty;\n" +
        "            hctl:forSubProtocol \"websub\";\n" +
        "        ] ;\n" +
        "      td:hasUriTemplateSchema [ a js:StringSchema;\n"+
        "      td:name    \"name\"  ]; ]; "+
        "    td:hasActionAffordance [\n" +
        "        a td:ActionAffordance ;\n" +
        "        td:name \"my_action\" ;\n" +
        "        td:title \"My Action\" ;\n" +
        "        td:hasForm [\n" +
        "            htv:methodName \"PUT\" ;\n" +
        "            hctl:hasTarget <http://example.org/action> ;\n" +
        "            hctl:forContentType \"application/json\";\n" +
        "            hctl:hasOperationType td:invokeAction;\n" +
        "        ] ;\n" +
        "        td:hasInputSchema [\n" +
        "            a js:ObjectSchema ;\n" +
        "            js:properties [\n" +
        "                a js:NumberSchema ;\n" +
        "                js:propertyName \"number_value\";\n" +
        "                js:maximum 100.05 ;\n" +
        "                js:minimum -100.05 ;\n" +
        "            ] ;\n" +
        "            js:required \"number_value\" ;\n" +
        "        ] ;\n" +
        "        td:hasOutputSchema [\n" +
        "            a js:ObjectSchema ;\n" +
        "            js:properties [\n" +
        "                a js:BooleanSchema ;\n" +
        "                js:propertyName \"boolean_value\";\n" +
        "            ] ;\n" +
        "            js:required \"boolean_value\" ;\n" +
        "        ]\n" +
        "    ] .";
    ThingDescription td = TDGraphReader.readFromString(TDFormat.RDF_TURTLE, TDDescription);
    DataSchema uriVariableSchema1 = td.getProperties().get(0).getUriVariables().get().get("name");
    assertEquals(DataSchema.STRING, uriVariableSchema1.getDatatype());
  }

  @Test
  public void testReadFormsRelativeURIs() {
    String TDDescription = PREFIXES +
      "\n" +
      "<http://example.org/#thing> a td:Thing ;\n" +
      "    dct:title \"My Thing\" ;\n" +
      "    td:hasSecurityConfiguration [ a wotsec:NoSecurityScheme ] ;\n" +
      "    td:hasActionAffordance [\n" +
      "        a td:ActionAffordance ;\n" +
      "        td:name \"my_action\" ;\n" +
      "        td:hasForm [\n" +
      "            hctl:hasTarget <http://example-2.org/action> ;\n" +
      "            hctl:forSubProtocol \"prA\";\n" +
      "        ] , [\n" +
      "            hctl:hasTarget <action> ;\n" +
      "            hctl:forSubProtocol \"prB\";\n" +
      "        ] ;\n" +
      "    ];\n" +
      "    td:hasPropertyAffordance [\n" +
      "        a td:PropertyAffordance ;\n" +
      "        td:name \"my_property\" ;\n" +
      "        td:hasForm [\n" +
      "            hctl:hasTarget <property> ;\n" +
      "            hctl:hasOperationType td:writeProperty;\n" +
      "        ] , [\n" +
      "            hctl:hasTarget <http://example.org/property-2> ;\n" +
      "            hctl:hasOperationType td:readProperty;\n" +
      "        ] ;\n" +
      "    ];\n" +
      "    td:baseURI <http://example.org/>.\n";
    ThingDescription td = TDGraphReader.readFromString(TDFormat.RDF_TURTLE, TDDescription);

    // Actions
    List<ActionAffordance> actions = td.getActions();
    assertEquals(1, actions.size());

    ActionAffordance action = actions.get(0);
    assertEquals(2, action.getForms().size());

    Optional<Form> actionForm1 = action.getFirstFormForSubProtocol(TD.invokeAction, "prA");
    Optional<Form> actionForm2 = action.getFirstFormForSubProtocol(TD.invokeAction, "prB");
    assertTrue(actionForm1.isPresent());
    assertTrue(actionForm2.isPresent());

    assertEquals("http://example-2.org/action", actionForm1.get().getTarget());
    assertEquals("http://example.org/action", actionForm2.get().getTarget());

    // Properties
    List<PropertyAffordance> properties = td.getProperties();
    assertEquals(1, properties.size());

    PropertyAffordance prop = properties.get(0);
    assertEquals(2, prop.getForms().size());

    Optional<Form> propForm1 = prop.getFirstFormForOperationType(TD.writeProperty);
    Optional<Form> propForm2 = prop.getFirstFormForOperationType(TD.readProperty);
    assertTrue(propForm1.isPresent());
    assertTrue(propForm2.isPresent());

    assertEquals("http://example.org/property", propForm1.get().getTarget());
    assertEquals("http://example.org/property-2", propForm2.get().getTarget());
  }

  @Test
  public void testReadFormsRelativeWithNoTDBase() {
    String TDDescription = PREFIXES +
      "@base <http://example.org/file-base/>." +
      "\n" +
      "<http://example.org/#thing> a td:Thing ;\n" +
      "    dct:title \"My Thing\" ;\n" +
      "    td:hasSecurityConfiguration [ a wotsec:NoSecurityScheme ] ;\n" +
      "    td:hasActionAffordance [\n" +
      "        a td:ActionAffordance ;\n" +
      "        td:name \"my_action\" ;\n" +
      "        td:hasForm [\n" +
      "            hctl:hasTarget <action> ;\n" +
      "        ] ;\n" +
      "    ].\n";

    Exception exception = assertThrows(InvalidTDException.class, () -> {
      TDGraphReader.readFromString(TDFormat.RDF_TURTLE, TDDescription);
    });

    String expectedMessage = "RDF Syntax Error";
    String actualMessage = exception.getMessage();

    assertTrue(actualMessage.contains(expectedMessage));
  }

  @Test
  public void testReadRelativeURIsWithBaseAndTDBase() {
    String TDDescription = PREFIXES +
      "@base <http://example.org/file-base/>." +
      "\n" +
      "<http://example.org/#thing> a td:Thing, <not-an-affordance> ;\n" +
      "    dct:title \"My Thing\" ;\n" +
      "    td:hasSecurityConfiguration [ a wotsec:NoSecurityScheme ] ;\n" +
      "    td:hasActionAffordance [\n" +
      "        a td:ActionAffordance ;\n" +
      "        td:name \"my_action\" ;\n" +
      "        td:hasForm [\n" +
      "            hctl:hasTarget <action> ;\n" +
      "        ] ;\n" +
      "    ];\n" +
      "    td:baseURI <http://example.org/td-base/>.\n";
    ThingDescription td = TDGraphReader.readFromString(TDFormat.RDF_TURTLE, TDDescription);

    //TD base
    assertTrue(td.getBaseURI().isPresent());
    assertEquals("http://example.org/td-base/", td.getBaseURI().get());

    // Thing type
    Set<String> types = td.getSemanticTypes();
    assertTrue(types.contains("http://example.org/file-base/not-an-affordance"));

    //Action
    List<ActionAffordance> actions = td.getActions();
    assertEquals(1, actions.size());
    ActionAffordance action = actions.get(0);
    assertEquals(1, action.getForms().size());
    assertTrue(action.getFirstForm().isPresent());
    assertEquals("http://example.org/td-base/action", action.getFirstForm().get().getTarget());
  }

  private void assertForm(Form form, String methodName, String target,
                          String contentType, String operationType) {
    assertEquals(methodName, form.getMethodName().get());
    assertEquals(target, form.getTarget());
    assertEquals(contentType, form.getContentType());
    assertTrue(form.hasOperationType(operationType));
  }

  private List<String> getSecurityNamesforSchemeName(String schemeName, Map<String, SecurityScheme> schemes) {
    return schemes.keySet()
      .stream()
      .filter(s -> s.startsWith(schemeName))
      .collect(Collectors.toList());
  }
}
